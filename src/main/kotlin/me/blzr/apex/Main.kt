package me.blzr.apex

import me.alllex.parsus.parser.getOrElse
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.system.exitProcess

@Command(name = "apex-ddl-splitter", version = ["1.1"], mixinStandardHelpOptions = true)
class Main : Runnable {
    enum class Format {
        /** New Oracle APEX export format: PL/SQL units terminated by a bare '/' line. */
        NEW,
        /** Old SQL*Plus export format: every statement terminated by a line-ending ';'. */
        OLD
    }

    @Option(names = ["-i", "--input"], description = ["Input SQL file"])
    lateinit var input: String

    @Option(names = ["-o", "--output"], description = ["Output folder. By default, input name + '/out/'"])
    var output: String? = null

    @Option(
        names = ["-f", "--format"],
        description = ["Dump format: NEW (default) — slash-terminated PL/SQL; OLD — semicolon-only DDL"]
    )
    var format: Format = Format.NEW

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode: Int = CommandLine(Main()).execute(*args)
            exitProcess(exitCode)
        }

        /**
         * Split an Oracle SQL dump into individual statement chunks.
         *
         * Two formats are supported:
         *
         * **new** (Oracle APEX export) — PL/SQL units (PACKAGE, PACKAGE BODY, FUNCTION,
         * PROCEDURE, TRIGGER) are terminated by a standalone '/' on its own line. Pure DDL
         * (CREATE TABLE, ALTER TABLE, CREATE INDEX, …) is terminated by a line-ending ';'.
         * A single slash-chunk may contain multiple DDL statements separated by semicolons
         * when the exporter packed them together; these are split out individually. PL/SQL
         * bodies are never semicolon-split because they contain internal semicolons.
         *
         * **old** (SQL*Plus DDL-only export) — every statement ends with a line-ending ';'.
         * No PL/SQL units are expected; the file is split on ';' directly.
         */
        fun splitStatements(text: String, format: Format): List<String> {
            // Both patterns use \r? to handle CRLF and LF without touching the text.
            val slashPattern = Regex("""^\/$""", setOf(RegexOption.MULTILINE))
            val semicolonPattern = Regex("""\s*;\r?$""", setOf(RegexOption.MULTILINE))

            // PL/SQL units may contain internal semicolons — never split them on ';'.
            val plsqlStart = Regex(
                """^\s*CREATE\s+(?:OR\s+REPLACE\s+)?(?:EDITIONABLE\s+)?(?:PACKAGE\s+BODY|PACKAGE|FUNCTION|PROCEDURE|TRIGGER)\b""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
            )

            return when (format) {
                Format.NEW -> text.split(slashPattern)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .flatMap { chunk -> splitSlashChunk(chunk, semicolonPattern, plsqlStart) }

                Format.OLD -> text.split(semicolonPattern)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }
        }

        /**
         * Split a single slash-delimited chunk into individual statement chunks.
         *
         * Cases:
         * 1. Single statement — returned as-is.
         * 2. Multiple pure-DDL statements separated by semicolons — each split out.
         * 3. Mixed: DDL prefix followed by a PL/SQL unit — the DDL prefix is semicolon-split,
         *    the PL/SQL suffix is kept intact.
         */
        private fun splitSlashChunk(
            chunk: String,
            semicolonPattern: Regex,
            plsqlStart: Regex,
        ): List<String> {
            val plsqlMatch = plsqlStart.find(chunk)

            return when {
                plsqlMatch == null ->
                    // Pure DDL — split on semicolons
                    chunk.split(semicolonPattern).map { it.trim() }.filter { it.isNotBlank() }

                plsqlMatch.range.first == 0 ->
                    // Starts with PL/SQL — treat as one unit
                    listOf(chunk)

                else -> {
                    // DDL prefix + PL/SQL suffix
                    val ddlParts = chunk.substring(0, plsqlMatch.range.first)
                        .split(semicolonPattern).map { it.trim() }.filter { it.isNotBlank() }
                    val plsqlSuffix = chunk.substring(plsqlMatch.range.first).trim()
                    ddlParts + listOf(plsqlSuffix)
                }
            }
        }
    }

    override fun run() {
        output = output ?: ((if (input.endsWith(".sql")) input.dropLast(4) else input) + "/out/")

        val text = FileInputStream(input).bufferedReader().readText()

        val nodes = splitStatements(text, format)
        println("We have ${nodes.size} nodes")

        val parser = OraDumpGrammar()

        val parsed: Map<String, List<Ora>> = nodes.associateWith { node ->
            parser.parse(node).getOrElse {
                println(node)
                throw IllegalArgumentException(it.toString())
            }
        }

        val outputPath = Path(output!!).createDirectories()
        println("Create $outputPath")

        outputPath.listDirectoryEntries("*.sql").forEach { entry ->
            println("Delete $entry")
            entry.deleteIfExists()
        }

        FileOutputStream(File(output, "dict.sql")).bufferedWriter().use { dict ->

            parsed.entries.forEachIndexed { index, (text, type) ->
                println("Writing ${type.first().fileName}")
                var outputFile = File(output, type.first().fileName)

                if (outputFile.exists()) {
                    for (i in 1..10) {
                        val anotherName =
                            File(output, type.first().fileName.dropLast(".sql".length) + "-duplicate-$i.sql")

                        if (i == 10) {
                            throw IllegalArgumentException("File already exists $outputFile")
                        } else if (!anotherName.exists()) {
                            outputFile = anotherName
                            break
                        }
                    }
                }

                dict.write("-- ${type.first().fileName}\n")

                FileOutputStream(outputFile).bufferedWriter().use {
                    it.write(text.trim())
                }
            }
        }
    }
}
