package com.alecstrong.sql.psi.test.fixtures

import com.alecstrong.sql.psi.core.PredefinedTable
import com.alecstrong.sql.psi.core.SqlFileBase
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.junit.Test
import java.io.File
import java.util.Enumeration
import java.util.jar.JarEntry
import java.util.jar.JarFile

abstract class FixturesTest(
  val name: String,
  val fixtureRoot: File,
  val predefinedTables: List<PredefinedTable> = emptyList(),
) {
  protected open val replaceRules: Array<Pair<String, String>> = emptyArray()

  abstract fun setupDialect()

  @Test fun execute() {
    setupDialect()

    val parser = TestHeadlessParser()
    val errors = ArrayList<String>()

    val newRoot = File("build/fixtureCopies/${fixtureRoot.name}Copy")
    fixtureRoot.copyRecursively(newRoot, overwrite = true)
    if (name in ansiFixtures.map { it.first() }) {
      newRoot.replaceKeywords()
    }

    val environment = parser.build(
      root = newRoot.path,
      annotator = { element, s ->
        val documentManager = PsiDocumentManager.getInstance(element.project)
        val name = element.containingFile.name
        val document = documentManager.getDocument(element.containingFile)!!
        val lineNum = document.getLineNumber(element.textOffset)
        val offsetInLine = element.textOffset - document.getLineStartOffset(lineNum)
        errors.add("$name line ${lineNum + 1}:$offsetInLine - $s")
      },
      predefinedTables = (
        newRoot.listFiles { _, name ->
          name.endsWith(".predefined")
        }?.map {
          PredefinedTable("", it.nameWithoutExtension, it.readText())
        } ?: emptyList()
        ) + predefinedTables,
    )

    val sourceFiles = StringBuilder()
    environment.forSourceFiles<SqlFileBase> {
      sourceFiles.append("${it.name}:\n")
      it.printTree {
        sourceFiles.append("  ")
        sourceFiles.append(it)
      }
    }

    println(sourceFiles)

    val expectedFailures = ArrayList<String>()
    val expectedFailuresFile = File(newRoot, "failure.txt")
    if (expectedFailuresFile.exists()) {
      expectedFailures += expectedFailuresFile.readText().splitLines().filter { it.isNotEmpty() }
    }

    environment.forSourceFiles<SqlFileBase> { file ->
      val inlineErrors = inlineErrorRegex.findAll(file.text)
      val document = PsiDocumentManager.getInstance(file.project).getDocument(file.containingFile)!!

      for (errorMatch in inlineErrors) {
        // Add 1 to make it 1-based, and another 1 because the line where the error should happen is the next line
        // after the error comment line
        val lineNum = document.getLineNumber(errorMatch.range.first) + 1 + 1
        val (offsetInLine, errMsg) = errorMatch.destructured
        expectedFailures += "${file.name} line $lineNum:$offsetInLine - ${errMsg.trim()}"
      }
    }

    val errorsStr = formatErrorList(errors)
    val expectedFailuresStr = formatErrorList(expectedFailures)
    val missingList = expectedFailures.filter { it !in errors }
    val missingStr = formatErrorList(missingList)
    val extrasList = errors.filter { it !in expectedFailures }
    val extrasStr = formatErrorList(extrasList)

    val assertionMsgEnd = "\nOverall we expected to see $expectedFailuresStr but got $errorsStr"
    when {
      missingList.isNotEmpty() && extrasList.isNotEmpty() -> {
        throw AssertionError("Test failed because the compile output is missing $missingStr and unexpectedly has $extrasStr. $assertionMsgEnd")
      }
      missingList.isNotEmpty() -> {
        throw AssertionError("Test failed because the compile output is missing $missingStr. $assertionMsgEnd")
      }
      extrasList.isNotEmpty() -> {
        throw AssertionError("Test failed because the compile output unexpectedly has $extrasStr. $assertionMsgEnd")
      }
    }

    newRoot.deleteRecursively()
  }

  private fun PsiElement.printTree(printer: (String) -> Unit) {
    printer("$this\n")
    children.forEach { child ->
      child.printTree { printer("  $it") }
    }
  }

  private fun File.replaceKeywords() {
    if (isDirectory) {
      listFiles()?.forEach { it.replaceKeywords() }
      return
    }
    replaceRules.forEach { (from, to) ->
      writeText(readText().replace(from, to))
    }
  }

  companion object {
    init {
      loadFolderFromResources("fixtures")
    }

    @JvmStatic
    protected val ansiFixtures = File("build/fixtures").listFiles()
      .filter { it.isDirectory }
      .map { arrayOf(it.name, it) }
  }
}

private fun Any.loadFolderFromResources(path: String) {
  val jarFile = File(javaClass.protectionDomain.codeSource.location.path)
  val parentFile = File("build")
  File(parentFile, path).apply { if (exists()) deleteRecursively() }

  assert(jarFile.isFile)

  val jar = JarFile(jarFile)
  val entries: Enumeration<JarEntry> = jar.entries() // gives ALL entries in jar
  while (entries.hasMoreElements()) {
    val entry = entries.nextElement()
    val name: String = entry.name
    if (name.startsWith("$path/")) { // filter according to the path
      if (entry.isDirectory) {
        File(parentFile, entry.name).mkdir()
      } else {
        File(parentFile, entry.name).apply {
          createNewFile()
          jar.getInputStream(entry).use {
            it.copyTo(outputStream())
          }
        }
      }
    }
  }
  jar.close()
}

private fun formatErrorList(errors: List<String>): String {
  if (errors.isEmpty()) {
    return "no errors"
  }
  return errors.joinToString("\n", prefix = "the errors <[\n", postfix = "\n]>") { "    $it" }
}

private val inlineErrorRegex = "^--\\s*error\\[col (\\d+)]:(.+)$".toRegex(RegexOption.MULTILINE)

private fun String.splitLines() = split("\\r?\\n".toRegex())
