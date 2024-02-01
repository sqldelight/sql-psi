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
import com.intellij.openapi.diagnostic.DefaultLogger
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.impl.CustomEntityProjectModelInfoProvider
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
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.smartPointers.SmartPointerAnchorProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndex
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndexContributor
import com.intellij.workspaceModel.core.fileIndex.impl.WorkspaceFileIndexImpl
import java.io.File
import kotlin.reflect.KClass

private class ApplicationEnvironment {
  private val logger = object : DefaultLogger("") {
    override fun warn(message: String?, t: Throwable?) = Unit
    override fun error(message: Any?) = Unit
  }

  val disposable = Disposer.newDisposable()

  val coreApplicationEnvironment: CoreApplicationEnvironment = CoreApplicationEnvironment(disposable).apply {
    Logger.setFactory { logger }

    CoreApplicationEnvironment.registerApplicationExtensionPoint(
      MetaLanguage.EP_NAME,
      MetaLanguage::class.java,
    )
    CoreApplicationEnvironment.registerApplicationExtensionPoint(
      SmartPointerAnchorProvider.EP_NAME,
      SmartPointerAnchorProvider::class.java,
    )
    CoreApplicationEnvironment.registerApplicationExtensionPoint(
      WorkspaceFileIndexImpl.EP_NAME,
      WorkspaceFileIndexContributor::class.java,
    )
    CoreApplicationEnvironment.registerApplicationExtensionPoint(
      CustomEntityProjectModelInfoProvider.EP,
      CustomEntityProjectModelInfoProvider::class.java,
    )
  }
}

open class SqlCoreEnvironment(
  sourceFolders: List<File>,
  dependencies: List<File>,
) : AutoCloseable {
  private val fileIndex: CoreFileIndex

  private val env = ApplicationEnvironment()
  protected val projectEnvironment = CoreProjectEnvironment(
    env.disposable,
    env.coreApplicationEnvironment,
  )

  protected val localFileSystem: VirtualFileSystem = VirtualFileManager.getInstance().getFileSystem(
    StandardFileSystems.FILE_PROTOCOL,
  )

  init {
    projectEnvironment.registerProjectComponent(
      ProjectRootManager::class.java,
      ProjectRootManagerImpl(projectEnvironment.project),
    )
    projectEnvironment.project.registerService(
      WorkspaceFileIndex::class.java,
      WorkspaceFileIndexImpl(projectEnvironment.project),
    )
    projectEnvironment.project.registerService(
      DirectoryIndex::class.java,
      DirectoryIndexImpl(projectEnvironment.project),
    )

    fileIndex = CoreFileIndex(sourceFolders, localFileSystem, projectEnvironment.project)
    projectEnvironment.project.registerService(ProjectFileIndex::class.java, fileIndex)

    val contributorIndex = CoreFileIndex(sourceFolders + dependencies, localFileSystem, projectEnvironment.project)
    projectEnvironment.project.registerService(
      SchemaContributorIndex::class.java,
      object : SchemaContributorIndex {
        private val contributors by lazy {
          val manager = PsiManager.getInstance(projectEnvironment.project)
          val map = linkedMapOf<VirtualFile, Collection<SchemaContributor>>()
          contributorIndex.iterateContent { file ->
            map[file] = (manager.findFile(file) as? SqlFileBase)?.sqlStmtList?.stmtList
              ?.mapNotNull { it.firstChild as? SchemaContributor } ?: emptyList()
            return@iterateContent true
          }
          map
        }

        override fun getKey() = SchemaContributorIndex.KEY

        override fun get(
          key: String,
          project: Project,
          scope: GlobalSearchScope,
        ): Collection<SchemaContributor> {
          return contributors.filterKeys { scope.contains(it) }.flatMap { (_, values) -> values }
        }
      },
    )
  }

  fun annotate(
    extraAnnotators: Collection<SqlCompilerAnnotator> = emptyList(),
    annotationHolder: SqlAnnotationHolder,
  ) {
    val otherFailures = mutableListOf<() -> Unit>()
    val myHolder = SqlAnnotationHolder { element, s ->
      if (PsiTreeUtil.getNonStrictParentOfType(
          element,
          SqlCreateTableStmt::class.java,
          SqlCreateVirtualTableStmt::class.java,
          SqlCreateViewStmt::class.java,
        ) != null
      ) {
        annotationHolder.createErrorAnnotation(element, s)
      } else {
        otherFailures.add {
          annotationHolder.createErrorAnnotation(element, s)
        }
      }
    }
    forSourceFiles<SqlFileBase> {
      PsiTreeUtil.findChildOfType(it, PsiErrorElement::class.java)?.let { error ->
        myHolder.createErrorAnnotation(error, error.errorDescription)
        return@forSourceFiles
      }
      it.annotateRecursively(myHolder, extraAnnotators)
    }
    otherFailures.forEach { it.invoke() }
  }

  inline fun <reified T : PsiFile> forSourceFiles(noinline action: (T) -> Unit) {
    forSourceFiles(T::class, action)
  }

  open fun <T : PsiFile> forSourceFiles(klass: KClass<T>, action: (T) -> Unit) {
    val psiManager = PsiManager.getInstance(projectEnvironment.project)
    fileIndex.iterateContent { file ->
      val psiFile = psiManager.findFile(file) ?: return@iterateContent true
      if (klass.isInstance(psiFile)) {
        action(psiFile as T)
      }
      return@iterateContent true
    }
  }

  private fun PsiElement.annotateRecursively(
    annotationHolder: SqlAnnotationHolder,
    extraAnnotators: Collection<SqlCompilerAnnotator>,
  ) {
    if (this is SqlAnnotatedElement) {
      try {
        annotate(annotationHolder)
        extraAnnotators.forEach { it.annotate(this, annotationHolder) }
      } catch (e: AnnotationException) {
        annotationHolder.createErrorAnnotation(e.element ?: this, e.msg)
      } catch (e: Throwable) {
        throw IllegalStateException(
          """
        |Failed to compile ${containingFile.virtualFile.path}:${node.startOffset}:
        |  $text
        |
          """.trimMargin(),
          e,
        )
      }
    }
    children.forEach { it.annotateRecursively(annotationHolder, extraAnnotators) }
  }

  protected fun initializeApplication(block: CoreApplicationEnvironment.() -> Unit) {
    env.coreApplicationEnvironment.block()
  }

  override fun close() {
    Disposer.dispose(env.disposable)
  }
}

private class CoreFileIndex(
  val sourceFolders: List<File>,
  private val localFileSystem: VirtualFileSystem,
  project: Project,
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
