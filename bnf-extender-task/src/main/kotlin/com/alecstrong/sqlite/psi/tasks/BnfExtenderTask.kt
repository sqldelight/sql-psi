package com.alecstrong.sqlite.psi.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import java.io.File

open class BnfExtenderTask: SourceTask() {
  @get:OutputDirectory lateinit var outputDirectory: File

  @get:Input lateinit var outputPackage: String

  @TaskAction
  fun execute(inputs: IncrementalTaskInputs) {
    inputs.outOfDate { input ->
      val rules = LinkedHashMap<String, String>()
      var currentRule = ""
      var currentRuleDefinition = ""
      var firstRule = ""
      var header = ""
      input.file.forEachLine { line ->
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

      rules.put(currentRule, currentRuleDefinition)

      val unextendableRules = unextendableRules(header, rules.keys)
      val rulesToExtend = rules.filterNot { it.key in unextendableRules }

      header = "{\n  parserUtilClass=\"$outputPackage.${input.file.parserUtilName()}\"\n" +
          "elementTypeHolderClass=\"$outputPackage.psi.${input.file.elementTypeHolderName()}\"\n" +
          header.lines().drop(2).joinToString("\n")

      val keyFinder = Regex("([^a-zA-Z_]|^)(${unextendableSubclasses(header, rules.keys).joinToString("|")})([^a-zA-Z_]|$)")
      val unextendableRuleDefinitions = rules.filterKeys { it in unextendableRules }
          .map { "${it.key} ::= ${it.value.subclassReplacements(keyFinder)}" }
          .joinToString("\n")

      File("${outputDirectory().path}/grammars", input.file.name)
          .createIfAbsent()
          .writeText("$header\n${generateRules(firstRule, rulesToExtend)}\n$unextendableRuleDefinitions")

      File("${outputDirectory().path}/parser", "${input.file.parserUtilName()}.kt")
          .createIfAbsent()
          .writeText(generateParserUtil(rulesToExtend, input.file))

      File("${outputDirectory().path}/parser", "${input.file.customParserName()}.kt")
          .createIfAbsent()
          .writeText(generateCustomParser(rulesToExtend, input.file))
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
    val builder = StringBuilder("root ::= ${firstRule}_real\n")

    val keyFinder = Regex("([^a-zA-Z_]|^)(${rules.keys.joinToString("|")})([^a-zA-Z_]|$)")
    for ((rule, definition) in rules) {
      builder.append("fake $rule ::= $definition\n")
          .append("${rule}_real ::= ${definition.extensionReplacements(keyFinder)} { elementType = $rule }\n")
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
    return keyFinder.findAll(headerText).map { it.groupValues[2] }.asSequence()
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

  // TODO(AlecStrong): Use kotlinpoet for these
  private fun generateParserUtil(rules: Map<String, String>, inputFile: File): String {
    val builder = StringBuilder("package $outputPackage\n\n")

    builder.append("import com.intellij.lang.PsiBuilder\n")
        .append("import com.intellij.lang.parser.GeneratedParserUtilBase\n\n")

    val parserVar = inputFile.customParserName().decapitalize()

    builder.append("internal object ${inputFile.parserUtilName()}: GeneratedParserUtilBase() {\n")
        .append("  internal var $parserVar: ${inputFile.customParserName()} = ${inputFile.customParserName()}()\n\n")

    for (rule in rules.keys) {
      builder.append("  @JvmStatic fun ${rule.toFunctionName()}(builder: PsiBuilder, level: Int, $rule: Parser): Boolean {\n")
          .append("    return $parserVar.${rule.toCustomFunction()}(builder, level, $rule)\n")
          .append("  }\n\n")
    }

    return builder.append("}").toString()
  }

  private fun generateCustomParser(rules: Map<String, String>, inputFile: File): String {
    val builder = StringBuilder("package $outputPackage\n\n")

    builder.append("import ${outputPackage}.psi.${inputFile.elementTypeHolderName()}\n")
        .append("import com.intellij.lang.ASTNode\n")
        .append("import com.intellij.lang.PsiBuilder\n")
        .append("import com.intellij.lang.parser.GeneratedParserUtilBase.Parser\n")
        .append("import com.intellij.psi.PsiElement\n\n")

    builder.append("open class ${inputFile.customParserName()} {\n")
        .append("  open fun createElement(node: ASTNode): PsiElement {\n")
        .append("    return ${inputFile.elementTypeHolderName()}.Factory.createElement(node)\n")
        .append("  }\n\n")

    for (rule in rules.keys) {
        builder.append("  open fun ${rule.toCustomFunction()}(builder: PsiBuilder, level: Int, $rule: Parser): Boolean {\n")
            .append("    return $rule.parse(builder, level)\n")
            .append("  }\n\n")
    }

    return builder.append("}").toString()
  }

  companion object {
    private val snakeCaseRegex = Regex("_\\w")
  }
}
