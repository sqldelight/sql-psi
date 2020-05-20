package com.alecstrong.sql.psi.core

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class FixturesTest(val dialect: DialectPreset, val name: String, val fixtureRoot: File) {
  @Test fun execute() {
    dialect.setup()
    val parser = TestHeadlessParser()
    val errors = ArrayList<String>()
    val environment = parser.build(fixtureRoot.path, object : SqlAnnotationHolder {
      override fun createErrorAnnotation(element: PsiElement, s: String) {
        val documentManager = PsiDocumentManager.getInstance(element.project)
        val name = element.containingFile.name
        val document = documentManager.getDocument(element.containingFile)!!
        val lineNum = document.getLineNumber(element.textOffset)
        val offsetInLine = element.textOffset - document.getLineStartOffset(lineNum)
        errors.add("$name line ${lineNum + 1}:$offsetInLine - $s")
      }
    })

    val sourceFiles = StringBuilder()
    environment.forSourceFiles {
      sourceFiles.append("${it.name}:\n")
      it.printTree {
        sourceFiles.append("  ")
        sourceFiles.append(it)
      }
    }

    println(sourceFiles)

    val expectedFailures = ArrayList<String>()
    val expectedFailuresFile = File(fixtureRoot, "failure.txt")
    if (expectedFailuresFile.exists()) {
      expectedFailures += expectedFailuresFile.readText().splitLines().filter { it.isNotEmpty() }
    }

    environment.forSourceFiles { file ->
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
  }

  fun PsiElement.printTree(printer: (String) -> Unit) {
    printer("$this\n")
    children.forEach { child ->
      child.printTree { printer("  $it") }
    }
  }

  companion object {
    private val dialects = mapOf(
        DialectPreset.SQLITE_3_18 to arrayOf("src/test/fixtures", "src/test/fixtures_upsert_not_supported"),
        DialectPreset.SQLITE_3_24 to arrayOf("src/test/fixtures", "src/test/fixtures_sqlite_3_24"),
        DialectPreset.MYSQL to arrayOf("src/test/fixtures_mysql")
    )

    @Suppress("unused") // Used by Parameterized JUnit runner reflectively.
    @Parameters(name = "{0}: {1}")
    @JvmStatic fun parameters() = dialects.flatMap { (dialect, fixtureFolders) ->
      fixtureFolders.flatMap { fixtureFolder ->
        File(fixtureFolder).listFiles()
            .filter { it.isDirectory }
            .map { arrayOf(dialect, it.name, it) }
      }
    }
  }
}

private fun formatErrorList(errors: List<String>): String {
  if (errors.isEmpty()) {
    return "no errors"
  }
  return errors.joinToString("\n", prefix = "the errors <[\n", postfix = "\n]>") { "    $it" }
}

private val inlineErrorRegex = "^--\\s*error\\[col (\\d+)]:(.+)$".toRegex(RegexOption.MULTILINE)

private fun String.splitLines() = split("\\r?\\n".toRegex())
