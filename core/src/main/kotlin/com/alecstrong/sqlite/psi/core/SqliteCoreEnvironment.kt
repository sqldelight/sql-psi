package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElement
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
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

class SqliteCoreEnvironment(
    parserDefinition: SqliteParserDefinition,
    private val fileType: LanguageFileType,
    module: String,
    disposable: Disposable = Disposer.newDisposable()
) {
  private val applicationEnvironment = CoreApplicationEnvironment(disposable)
  private val projectEnvironment = CoreProjectEnvironment(disposable, applicationEnvironment)

  init {
    CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), MetaLanguage.EP_NAME, MetaLanguage::class.java)
    projectEnvironment.registerProjectComponent(ProjectRootManager::class.java, ProjectRootManagerImpl(projectEnvironment.project))

    with(applicationEnvironment) {
      val fileRegistry = FileTypeRegistry.ourInstanceGetter
      val directoryIndex = DirectoryIndexImpl(projectEnvironment.project)
      FileTypeRegistry.ourInstanceGetter = fileRegistry

      registerApplicationService(ProjectFileIndex::class.java, CoreFileIndex(module, projectEnvironment.project,
          directoryIndex, FileTypeRegistry.getInstance()))
      registerFileType(fileType, fileType.defaultExtension)
      registerParserDefinition(parserDefinition)
    }
  }

  fun annotate(annotationHolder: SqliteAnnotationHolder) {
    forSourceFiles {
      PsiTreeUtil.findChildOfType(it, PsiErrorElement::class.java)?.let { error ->
        annotationHolder.createErrorAnnotation(error, error.errorDescription)
        return@forSourceFiles
      }
      it.annotateRecursively(annotationHolder)
    }
  }

  fun forSourceFiles(action: (PsiFile) -> Unit) {
    val psiManager = PsiManager.getInstance(projectEnvironment.project)
    ProjectRootManager.getInstance(projectEnvironment.project).fileIndex.iterateContent { file ->
      if (file.fileType != fileType) return@iterateContent true
      action(psiManager.findFile(file)!!)
      return@iterateContent true
    }
  }

  private fun PsiElement.annotateRecursively(annotationHolder: SqliteAnnotationHolder) {
    if (this is SqliteCompositeElement) annotate(annotationHolder)
    children.forEach { it.annotateRecursively(annotationHolder) }
  }
}

private class CoreFileIndex(
    val module: String,
    project: Project,
    directoryIndex: DirectoryIndex,
    fileTypeRegistry: FileTypeRegistry
) : ProjectFileIndexImpl(project, directoryIndex, fileTypeRegistry) {
  override fun iterateContent(iterator: ContentIterator): Boolean {
    val localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
    return iterateContentUnderDirectory(localFileSystem.findFileByPath(module)!!, iterator)
  }

  override fun iterateContentUnderDirectory(file: VirtualFile, iterator: ContentIterator): Boolean {
    if (file.isDirectory) {
      file.children.forEach { if (!iterateContentUnderDirectory(it, iterator)) return false }
      return true
    }
    return iterator.processFile(file)
  }
}
