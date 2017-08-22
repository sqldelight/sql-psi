package com.alecstrong.sqlite.psi.core

import com.google.common.truth.Truth.assertThat
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File

@RunWith(Parameterized::class)
class FixturesTest(val fixtureRoot: File, val name: String) {
  @Test fun execute() {
    val parser = TestHeadlessParser()
    val errors = ArrayList<String>()
    parser.build(fixtureRoot.path, object : SqliteAnnotationHolder {
      override fun createErrorAnnotation(element: PsiElement, s: String?) {
        val documentManager = PsiDocumentManager.getInstance(element.project)
        val name = element.containingFile.name
        val document = documentManager.getDocument(element.containingFile)!!
        val lineNum = document.getLineNumber(element.textOffset)
        val offsetInLine = element.textOffset - document.getLineStartOffset(lineNum)
        errors.add("$name line ${lineNum+1}:$offsetInLine - $s")
      }
    })

    val expectedFailure = File(fixtureRoot, "failure.txt")
    if (expectedFailure.exists()) {
      assertThat(errors).containsExactlyElementsIn(
          expectedFailure.readText().split("\n").filterNot { it.isEmpty() }
      )
    } else {
      assertThat(errors).isEmpty()
    }
  }

  companion object {
    @Suppress("unused") // Used by Parameterized JUnit runner reflectively.
    @Parameters(name = "{1}")
    @JvmStatic fun parameters() = File("src/test/fixtures").listFiles()
        .filter { it.isDirectory }
        .map { arrayOf(it, it.name) }
  }
}
