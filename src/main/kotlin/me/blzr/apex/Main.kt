package me.blzr.apex

import me.alllex.parsus.parser.getOrElse
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.system.exitProcess

fun main(vararg args: String) {
    val (input, output) = when (args.size) {
        1 -> args[0] to "${args[0].let { if (it.endsWith(".sql")) it.dropLast(4) else it }}/out/"
        2 -> args[0] to args[1]
        else -> {
            println("apex-ddl-splitter input.sql [out folder]")
            exitProcess(0)
        }
    }

    val ins = FileInputStream(input)

    val text = ins.bufferedReader().readText()

    val nodes = text.split(Regex("^/$", RegexOption.MULTILINE)).filter { it.isNotBlank() }
    println("We have ${nodes.size} nodes")

    val parser = OraDumpGrammar()

    val parsed: Map<String, Ora> = nodes.associateWith { node ->
        parser.parse(node).getOrElse {
            println(node)
            throw IllegalArgumentException(it.toString())
        }
    }

    val outputPath = Path(output).createDirectories()
    println("Create $outputPath")

    outputPath.listDirectoryEntries("*.sql").forEach { entry ->
        println("Delete $entry")
        entry.deleteIfExists()
    }

    parsed.entries.forEachIndexed { index, (text, type) ->
        println("Writing ${type.fileName}")
        val outputFile = File(output, type.fileName)

        if (outputFile.exists()) {
            throw IllegalArgumentException("File already exists $outputFile")
        }

        FileOutputStream(outputFile).bufferedWriter().use {
            it.write("-- Order $index\n" + text.trim())
        }
    }
}


