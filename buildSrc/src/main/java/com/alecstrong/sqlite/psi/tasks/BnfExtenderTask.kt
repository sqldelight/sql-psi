package com.alecstrong.sqlite.psi.tasks

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OPEN
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import java.io.File

open class BnfExtenderTask : SourceTask() {
  @get:OutputDirectory lateinit var outputDirectory: File

  @get:Input lateinit var outputPackage: String

  @TaskAction
  fun execute(inputs: IncrementalTaskInputs) {
    inputs.outOfDate {
      val rules = LinkedHashMap<String, String>()
      var currentRule = ""
      var currentRuleDefinition = ""
      var firstRule = ""
      var header = ""
      var generatedUtilSuperclass = ClassName("com.intellij.lang.parser", "GeneratedParserUtilBase")
      file.forEachLine { line ->
        val ruleSeparatorIndex = line.indexOf("::=")
        if (ruleSeparatorIndex >= 0) {
          val ruleName = line.substring(0 until ruleSeparatorIndex).trim()
          if (currentRule.isNotEmpty()) {
            // End the old rule if there was one.
            rules.put(currentRule, currentRuleDefinition)
          } else {
            firstRule = ruleName
            header = currentRuleDefinition
          }
          currentRule = ruleName
          currentRuleDefinition = line.substring((ruleSeparatorIndex + 3) until line.length)
        } else {
          currentRuleDefinition += "\n$line"
        }
      }

      val regex = Regex("[\\s\\S]+parserUtilClass=\"([a-zA-Z.]*)\"[\\s\\S]+")
      regex.matchEntire(header)?.groupValues?.getOrNull(1)?.let {
        generatedUtilSuperclass = ClassName.bestGuess(it)
      }

      rules.put(currentRule, currentRuleDefinition)

      val unextendableRules = unextendableRules(header, rules.keys)
      val rulesToExtend = rules.filterNot { it.key in unextendableRules }

      header = "{\n  parserUtilClass=\"$outputPackage.${file.parserUtilName()}\"\n" +
          "elementTypeHolderClass=\"$outputPackage.psi.${file.elementTypeHolderName()}\"\n" +
          header.lines().drop(2).joinToString("\n")

      val keyFinder = Regex("([^a-zA-Z_]|^)(${unextendableSubclasses(header, rules.keys).joinToString("|")})([^a-zA-Z_]|$)")
      val unextendableRuleDefinitions = rules.filterKeys { it in unextendableRules }
          .map { "${it.key} ::= ${it.value.subclassReplacements(keyFinder)}" }
          .joinToString("\n")

      File("${outputDirectory().path}/grammars", file.name)
          .createIfAbsent()
          .writeText("$header\n${generateRules(firstRule, rulesToExtend)}\n$unextendableRuleDefinitions")

      File("${outputDirectory().path}/parser", "${file.parserUtilName()}.kt")
          .createIfAbsent()
          .writeText(generateParserUtil(rulesToExtend, file, generatedUtilSuperclass))

      File("${outputDirectory().path}/parser", "${file.customParserName()}.kt")
          .createIfAbsent()
          .writeText(generateCustomParser(rulesToExtend, file))
    }
  }

  private fun outputDirectory(): File = File(outputDirectory, outputPackage.replace('.', '/'))

  private fun File.createIfAbsent() = apply {
    if (!exists()) {
      parentFile.mkdirs()
      createNewFile()
    }
  }

  private fun generateRules(firstRule: String, rules: Map<String, String>): String {
    val keyFinder = Regex("([^a-zA-Z_]|^)(${rules.keys.joinToString("|")})([^a-zA-Z_]|$)")
    val pinFinder = Regex("[\\s\\S]+pin *= *([0-9]*)[\\s\\S]+")

    val builder = StringBuilder("root ::= ${firstRule.extensionReplacements(keyFinder)}\n")
    for ((rule, definition) in rules) {
      builder.append("fake $rule ::= $definition\n")
          .append("${rule}_real ::= ${definition.extensionReplacements(keyFinder)} {\n" +
              "  elementType = $rule\n")
      pinFinder.matchEntire(definition)?.groupValues?.getOrNull(1)?.let {
        builder.append("  pin=$it\n")
      }
      builder.append("}\n")
    }
    return builder.toString()
  }

  private fun String.extensionReplacements(keysRegex: Regex): String {
    fun String.matcher() = replace(keysRegex) { match ->
      "${match.groupValues[1]}<<${match.groupValues[2].toFunctionName()} ${match.groupValues[2]}_real>>${match.groupValues[3]}"
    }
    // We have to do it twice because the matcher doesn't catch three adjacent rules.
    if (endsWith("}")) {
      return substring(0, indexOf("{") - 1).matcher().matcher()
    }
    return matcher().matcher()
  }

  private fun String.subclassReplacements(keysRegex: Regex): String {
    fun String.matcher() = replace(keysRegex) { match ->
      "${match.groupValues[1]}${match.groupValues[2]}_real${match.groupValues[3]}"
    }
    // We have to do it twice because the matcher doesn't catch three adjacent rules.
    if (endsWith("}")) {
      return substring(0, indexOf("{") - 1).matcher().matcher()
    }
    return matcher().matcher()
  }

  private fun String.toFunctionName(): String {
    return "${toCustomFunction()}Ext"
  }

  private fun String.toCustomFunction(): String {
    return replace(snakeCaseRegex) { matchResult -> matchResult.value.trim('_').capitalize() }
  }

  private fun unextendableRules(headerText: String, rules: Collection<String>): Sequence<String> {
    val keyFinder = Regex("extends\\(\"([^)\"]+)\"\\)=([a-zA-Z_]+)\n")
    return keyFinder.findAll(headerText).map { it.groupValues[2] }.asSequence() + rules.filter { it.startsWith("private ") }
  }

  private fun unextendableSubclasses(headerText: String, rules: Collection<String>): Sequence<String> {
    val keyFinder = Regex("extends\\(\"([^)\"]+)\"\\)=([a-zA-Z_]+)\n")
    return keyFinder.findAll(headerText).flatMap {
      val pattern = Regex(it.groupValues[1])
      return@flatMap (rules.filter { it.matches(pattern) }).asSequence()
    }.distinct()
  }

  private fun File.parserUtilName() = "${nameWithoutExtension.capitalize()}ParserUtil"

  private fun File.customParserName() = "Custom${nameWithoutExtension.capitalize()}Parser"

  private fun File.elementTypeHolderName() = "${nameWithoutExtension.capitalize()}Types"

  private fun generateParserUtil(
    rules: Map<String, String>,
    inputFile: File,
    superclass: ClassName
  ): String {
    val parserVar = inputFile.customParserName().decapitalize()
    val customParserType = ClassName("", inputFile.customParserName())
    return FileSpec.builder(outputPackage, inputFile.parserUtilName())
        .addType(TypeSpec.objectBuilder(inputFile.parserUtilName())
            .addModifiers(INTERNAL)
            .superclass(superclass)
            .addProperty(PropertySpec.varBuilder(parserVar, customParserType)
                .addModifiers(INTERNAL)
                .initializer("%T()", customParserType)
                .build())
            .apply {
              rules.keys.forEach {
                addFunction(FunSpec.builder(it.toFunctionName())
                    .addAnnotation(JvmStatic::class)
                    .addParameter("builder", ClassName("com.intellij.lang", "PsiBuilder"))
                    .addParameter("level", Int::class)
                    .addParameter(it, ClassName("", "Parser"))
                    .returns(Boolean::class)
                    .addStatement("return $parserVar.${it.toCustomFunction()}(builder, level, $it)")
                    .build())
              }
            }
            .build())
        .build()
        .toString()
  }

  private fun generateCustomParser(rules: Map<String, String>, inputFile: File): String {
    val factoryType = ClassName("$outputPackage.psi", inputFile.elementTypeHolderName(), "Factory")
    val parserType = ClassName("com.intellij.lang.parser", "GeneratedParserUtilBase", "Parser")
    return FileSpec.builder(outputPackage, inputFile.customParserName())
        .addType(TypeSpec.classBuilder(inputFile.customParserName())
            .addModifiers(OPEN)
            .addFunction(FunSpec.builder("createElement")
                .addModifiers(OPEN)
                .addParameter("node", ClassName("com.intellij.lang", "ASTNode"))
                .returns(ClassName("com.intellij.psi", "PsiElement"))
                .addStatement("return %T.createElement(node)", factoryType)
                .build())
            .apply {
              rules.keys.forEach {
                addFunction(FunSpec.builder(it.toCustomFunction())
                    .addModifiers(OPEN)
                    .addParameter("builder", ClassName("com.intellij.lang", "PsiBuilder"))
                    .addParameter("level", Int::class)
                    .addParameter(it, parserType)
                    .returns(Boolean::class)
                    .addStatement("return $it.parse(builder, level)")
                    .build())
              }
            }
            .build())
        .build()
        .toString()
  }

  companion object {
    private val snakeCaseRegex = Regex("_\\w")
  }
}
