package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SchemaContributorIndex
import com.alecstrong.sql.psi.core.psi.SqlAnnotatedElement
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateVirtualTableStmt
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.CoreProjectEnvironment
import com.intellij.lang.Language
import com.intellij.lang.MetaLanguage
import com.intellij.openapi.diagnostic.DefaultLogger
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.Extensions
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
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.smartPointers.SmartPointerAnchorProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

private object ApplicationEnvironment {
  private val logger = object : DefaultLogger("") {
    override fun warn(message: String?, t: Throwable?) = Unit
    override fun error(message: Any?) = Unit
  }
  var initialized = AtomicBoolean(false)

  val coreApplicationEnvironment: CoreApplicationEnvironment by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    CoreApplicationEnvironment(Disposer.newDisposable()).apply {
      Logger.setFactory { logger }

      CoreApplicationEnvironment.registerExtensionPoint(
        Extensions.getRootArea(),
        MetaLanguage.EP_NAME,
        MetaLanguage::class.java,
      )
      CoreApplicationEnvironment.registerExtensionPoint(
        Extensions.getRootArea(),
        SmartPointerAnchorProvider.EP_NAME,
        SmartPointerAnchorProvider::class.java,
      )
    }
  }
}

data class PredefinedTable(val packageName: String, val simpleFileName: String, val content: String) {
  val fileName = "$packageName.$simpleFileName"
}

open class SqlCoreEnvironment(
  sourceFolders: List<File>,
  dependencies: List<File>,
  predefinedTables: List<PredefinedTable>,
  language: Language,
) {
  private val fileIndex: CoreFileIndex

  protected val projectEnvironment = CoreProjectEnvironment(
    ApplicationEnvironment.coreApplicationEnvironment.parentDisposable,
    ApplicationEnvironment.coreApplicationEnvironment,
  )

  protected val localFileSystem: VirtualFileSystem

  protected val predefinedTablesVirtualFiles: Lazy<List<VirtualFile>>

  init {
    localFileSystem = VirtualFileManager.getInstance().getFileSystem(
      StandardFileSystems.FILE_PROTOCOL,
    )

    projectEnvironment.registerProjectComponent(
      ProjectRootManager::class.java,
      ProjectRootManagerImpl(projectEnvironment.project),
    )

    projectEnvironment.project.registerService(
      DirectoryIndex::class.java,
      DirectoryIndexImpl(projectEnvironment.project),
    )

    fileIndex = CoreFileIndex(sourceFolders, lazyOf(emptyList()), localFileSystem, projectEnvironment.project)
    projectEnvironment.project.registerService(ProjectFileIndex::class.java, fileIndex)

    predefinedTablesVirtualFiles = lazy {
      val psiFactory = PsiFileFactory.getInstance(projectEnvironment.project)
      predefinedTables.map { content ->
        val sqlFile = psiFactory.createFileFromText(content.fileName, language, content.content) as SqlFileBase
        sqlFile.virtualFile
      }
    }

    val contributorIndex = CoreFileIndex(sourceFolders + dependencies, predefinedTablesVirtualFiles, localFileSystem, projectEnvironment.project)
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

  inline fun<reified T : PsiFile> forSourceFiles(noinline action: (T) -> Unit) {
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
    if (this is SqlAnnotatedElement) try {
      annotate(annotationHolder)
      extraAnnotators.forEach { it.annotate(this, annotationHolder) }
    } catch (e: AnnotationException) {
      annotationHolder.createErrorAnnotation(e.element ?: this, e.msg)
    } catch (e: Throwable) {
      throw IllegalStateException(
        """
        |Failed to compile ${this.containingFile.virtualFile.path}:${this.node.startOffset}:
        |  ${this.text}
        |
        """.trimMargin(),
        e,
      )
    }
    children.forEach { it.annotateRecursively(annotationHolder, extraAnnotators) }
  }

  protected fun initializeApplication(block: CoreApplicationEnvironment.() -> Unit) {
    if (!ApplicationEnvironment.initialized.getAndSet(true)) {
      ApplicationEnvironment.coreApplicationEnvironment.block()
    }
  }
}

fun interface SqlCompilerAnnotator {
  fun annotate(element: PsiElement, annotationHolder: SqlAnnotationHolder)
}

private class CoreFileIndex(
  val sourceFolders: List<File>,
  val predefinedTables: Lazy<List<VirtualFile>>,
  private val localFileSystem: VirtualFileSystem,
  project: Project,
) : ProjectFileIndexImpl(project) {
  override fun iterateContent(iterator: ContentIterator): Boolean {
    return sourceFolders.all {
      val file = localFileSystem.findFileByPath(it.absolutePath)
        ?: throw NullPointerException("File ${it.absolutePath} not found")
      iterateContentUnderDirectory(file, iterator)
    } && predefinedTables.value.all {
      iterator.processFile(it)
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
