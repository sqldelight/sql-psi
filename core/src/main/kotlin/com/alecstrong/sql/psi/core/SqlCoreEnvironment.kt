package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.SqlAnnotatedElement
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateVirtualTableStmt
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.CoreProjectEnvironment
import com.intellij.lang.MetaLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.impl.DirectoryIndex
import com.intellij.openapi.roots.impl.DirectoryIndexImpl
import com.intellij.openapi.roots.impl.ProjectFileIndexImpl
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import java.io.File

open class SqlCoreEnvironment(
  parserDefinition: SqlParserDefinition,
  private val fileType: LanguageFileType,
  sourceFolders: List<File>,
  disposable: Disposable = Disposer.newDisposable()
) {
  private val fileIndex: CoreFileIndex

  protected val applicationEnvironment = CoreApplicationEnvironment(disposable)
  protected val projectEnvironment = CoreProjectEnvironment(disposable, applicationEnvironment)

  init {
    val directoryIndex = DirectoryIndexImpl(projectEnvironment.project)
    fileIndex = CoreFileIndex(sourceFolders, projectEnvironment.project,
            directoryIndex, FileTypeRegistry.getInstance())
    CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(),
        MetaLanguage.EP_NAME, MetaLanguage::class.java)
    projectEnvironment.registerProjectComponent(ProjectRootManager::class.java,
        ProjectRootManagerImpl(projectEnvironment.project))

    with(applicationEnvironment) {
      val fileRegistry = FileTypeRegistry.ourInstanceGetter
      FileTypeRegistry.ourInstanceGetter = fileRegistry

      registerApplicationService(ProjectFileIndex::class.java, fileIndex)
      registerFileType(fileType, fileType.defaultExtension)
      registerParserDefinition(parserDefinition)
    }
  }

  fun annotate(annotationHolder: SqlAnnotationHolder) {
    val otherFailures = mutableListOf<() -> Unit>()
    val myHolder = object : SqlAnnotationHolder {
      override fun createErrorAnnotation(element: PsiElement, s: String) {
        if (PsiTreeUtil.getNonStrictParentOfType(element, SqlCreateTableStmt::class.java,
                SqlCreateVirtualTableStmt::class.java, SqlCreateViewStmt::class.java) != null) {
          annotationHolder.createErrorAnnotation(element, s)
        } else {
          otherFailures.add {
            annotationHolder.createErrorAnnotation(element, s)
          }
        }
      }
    }
    forSourceFiles {
      PsiTreeUtil.findChildOfType(it, PsiErrorElement::class.java)?.let { error ->
        myHolder.createErrorAnnotation(error, error.errorDescription)
        return@forSourceFiles
      }
      it.annotateRecursively(myHolder)
    }
    otherFailures.forEach { it.invoke() }
  }

  open fun forSourceFiles(action: (SqlFileBase) -> Unit) {
    val psiManager = PsiManager.getInstance(projectEnvironment.project)
    fileIndex.iterateContent { file ->
      val psiFile = psiManager.findFile(file) as? SqlFileBase ?: return@iterateContent true
      action(psiFile)
      return@iterateContent true
    }
  }

  private fun PsiElement.annotateRecursively(annotationHolder: SqlAnnotationHolder) {
    if (this is SqlAnnotatedElement) try {
      annotate(annotationHolder)
    } catch (e: AnnotationException) {
      annotationHolder.createErrorAnnotation(e.element ?: this, e.msg)
    }
    children.forEach { it.annotateRecursively(annotationHolder) }
  }
}

private class CoreFileIndex(
  val sourceFolders: List<File>,
  project: Project,
  directoryIndex: DirectoryIndex,
  fileTypeRegistry: FileTypeRegistry
) : ProjectFileIndexImpl(project, directoryIndex, fileTypeRegistry) {
  override fun iterateContent(iterator: ContentIterator): Boolean {
    val localFileSystem =
        VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
    return sourceFolders.all {
      val file = localFileSystem.findFileByPath(it.absolutePath)
      if (file == null) throw NullPointerException("File ${it.absolutePath} not found")
      iterateContentUnderDirectory(file, iterator)
    }
  }

  override fun iterateContentUnderDirectory(file: VirtualFile, iterator: ContentIterator): Boolean {
    if (file.isDirectory) {
      file.children.forEach { if (!iterateContentUnderDirectory(it, iterator)) return false }
      return true
    }
    return iterator.processFile(file)
  }
}
