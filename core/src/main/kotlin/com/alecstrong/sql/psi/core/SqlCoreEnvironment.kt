package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SchemaContributorIndex
import com.alecstrong.sql.psi.core.psi.SqlAnnotatedElement
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateVirtualTableStmt
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.CoreProjectEnvironment
import com.intellij.lang.MetaLanguage
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileTypes.FileTypeRegistry
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
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private object ApplicationEnvironment {
  var initialized = AtomicBoolean(false)

  val coreApplicationEnvironment: CoreApplicationEnvironment by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    CoreApplicationEnvironment(Disposer.newDisposable()).apply {
      CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(),
          MetaLanguage.EP_NAME, MetaLanguage::class.java)

      val fileRegistry = FileTypeRegistry.ourInstanceGetter
      FileTypeRegistry.ourInstanceGetter = fileRegistry
    }
  }
}

open class SqlCoreEnvironment(
  sourceFolders: List<File>,
  dependencies: List<File>
) {
  private val fileIndex: CoreFileIndex

  protected val projectEnvironment = CoreProjectEnvironment(
      ApplicationEnvironment.coreApplicationEnvironment.parentDisposable,
      ApplicationEnvironment.coreApplicationEnvironment
  )

  protected val localFileSystem: VirtualFileSystem

  init {
    localFileSystem = VirtualFileManager.getInstance().getFileSystem(
        StandardFileSystems.FILE_PROTOCOL)

    projectEnvironment.registerProjectComponent(ProjectRootManager::class.java,
        ProjectRootManagerImpl(projectEnvironment.project))

    projectEnvironment.project.registerService(DirectoryIndex::class.java,
        DirectoryIndexImpl(projectEnvironment.project))

    fileIndex = CoreFileIndex(sourceFolders, localFileSystem, projectEnvironment.project)
    projectEnvironment.project.registerService(ProjectFileIndex::class.java, fileIndex)

    val contributorIndex = CoreFileIndex(sourceFolders + dependencies, localFileSystem, projectEnvironment.project)
    projectEnvironment.project.picoContainer
        .registerComponentInstance(SchemaContributorIndex::class.java.name, object : SchemaContributorIndex {
          override fun getKey() = SchemaContributorIndex.KEY

          override fun get(
            key: String,
            project: Project,
            scope: GlobalSearchScope
          ): List<SchemaContributor> {
            val manager = PsiManager.getInstance(project)
            val contributors = mutableListOf<SchemaContributor>()
            contributorIndex.iterateContent { file ->
              if (!scope.contains(file)) return@iterateContent true

              (manager.findFile(file) as? SqlFileBase)?.sqlStmtList?.stmtList
                  ?.mapNotNull { it.firstChild as? SchemaContributor }
                  ?.let(contributors::addAll)
              return@iterateContent true
            }
            return contributors
          }
        })
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

  protected fun initializeApplication(block: CoreApplicationEnvironment.() -> Unit) {
    if (!ApplicationEnvironment.initialized.getAndSet(true)) {
      ApplicationEnvironment.coreApplicationEnvironment.block()
    }
  }
}

private class CoreFileIndex(
  val sourceFolders: List<File>,
  private val localFileSystem: VirtualFileSystem,
  private val project: Project
) : ProjectFileIndexImpl(project) {
  override fun iterateContent(iterator: ContentIterator): Boolean {
    return sourceFolders.all {
      val file = localFileSystem.findFileByPath(it.absolutePath)
          ?: throw NullPointerException("File ${it.absolutePath} not found")
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
