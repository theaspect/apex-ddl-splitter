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
    @Option(names = ["-i", "--input"], description = ["Input SQL file"])
    lateinit var input: String

    @Option(names = ["-o", "--output"], description = ["Output folder. By default, './out'"])
    var output: String? = null

    @Option(names = ["-s", "--split"], description = ["Split. By default, '\\s*;$'", "Previous Oracle used '^/$'"])
    var split: String = "\\s*;$"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode: Int = CommandLine(Main()).execute(*args)
            exitProcess(exitCode)
        }
    }

    override fun run() {
        output = output ?: ((if (input.endsWith(".sql")) input.dropLast(4) else input) + "/out/")

        val ins = FileInputStream(input)

        val text = ins.bufferedReader().readText()

        val nodes = text.split(Regex(split, RegexOption.MULTILINE)).filter { it.isNotBlank() }
        println("We have ${nodes.size} nodes")

        val parser = OraDumpGrammar()

        val parsed: Map<String, Ora> = nodes.associateWith { node ->
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
                println("Writing ${type.fileName}")
                var outputFile = File(output, type.fileName)

                if (outputFile.exists()) {
                    for (i in 1..10) {
                        val anotherName = File(output, type.fileName.dropLast(".sql".length) + "-duplicate-$i.sql")

                        if (i == 10) {
                            throw IllegalArgumentException("File already exists $outputFile")
                        } else if (!anotherName.exists()) {
                            outputFile = anotherName
                            break
                        }
                    }
                }

                dict.write("-- ${type.fileName}\n")

                FileOutputStream(outputFile).bufferedWriter().use {
                    it.write(text.trim())
                }
            }
        }
    }
}

