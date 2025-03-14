package com.blzr

import me.alllex.parsus.parser.Grammar
import me.alllex.parsus.parser.choose
import me.alllex.parsus.parser.map
import me.alllex.parsus.parser.maybe
import me.alllex.parsus.parser.parser
import me.alllex.parsus.parser.ref
import me.alllex.parsus.parser.times
import me.alllex.parsus.parser.unaryMinus
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

class OraDumpGrammar() : Grammar<Ora>(ignoreCase = true, debugMode = true) {
    init {
        regexToken("[\\s\\r\\n]+", ignored = true)
    }

    val create = literalToken("create")

    val or = literalToken("or")
    val replace = literalToken("replace")
    val orReplace by or * replace

    val force by literalToken("force")

    val alter = literalToken("alter")

    val table = literalToken("table")
    val view = literalToken("view")

    val add = literalToken("add")
    val constraint = literalToken("constraint")

    val editionable = literalToken("editionable")
    val function = literalToken("function")
    val procedure = literalToken("procedure")
    val trigger = literalToken("trigger")
    val pkg = literalToken("package")
    val body = literalToken("body")

    val unique = literalToken("unique")
    val bitmap = literalToken("bitmap")
    val index = literalToken("index")

    val sequence = literalToken("sequence")

    val quote = literalToken("\"")
    val name = regexToken("\\w+")
    val remaining = regexToken(Regex(".*", RegexOption.DOT_MATCHES_ALL))

    val quoted by -quote * ref(::name) * -quote map { it.text }

    val createTable by -create * -maybe(orReplace) * -table * quoted * -remaining map { Ora.CreateTable(it) }
    val alterTable by -alter * -table * quoted * -remaining map { Ora.AlterTable(it) }
    val alterTableAddConstraint by -alter * -table * quoted * -add * -constraint * quoted * -remaining map { (a,b) ->
        Ora.AlterTableAddConstraint(a, b)
    }

    val createView by -create * -maybe(orReplace) * -maybe(force) * -maybe(editionable) * -view * quoted * -remaining map {
        Ora.CreateView(
            it
        )
    }

    val createIndex by -create * -maybe(orReplace) * -maybe(unique) * -maybe(bitmap) * -index * quoted * -remaining map
            { Ora.CreateIndex(it) }
    val createSequence by -create * -maybe(orReplace) * -sequence * quoted * -remaining map
            { Ora.CreateSequence(it) }

    val createFunction by -create * -maybe(orReplace) * -maybe(editionable) * -function * quoted * -remaining map
            { Ora.CreateFunction(it) }
    val createProcedure by -create * -maybe(orReplace) * -maybe(editionable) * -procedure * quoted * -remaining map
            { Ora.CreateProcedure(it) }
    val createTrigger by -create * -maybe(orReplace) * -maybe(editionable) * -trigger * quoted * -remaining map
            { Ora.CreateTrigger(it) }
    val alterTrigger by -alter * -trigger * quoted * -remaining map
            { Ora.AlterTrigger(it) }

    val createPackage by -create * -maybe(orReplace) * -maybe(editionable) * -pkg * quoted * -remaining map {
        Ora.CreatePackage(
            it
        )
    }
    val createPackageBody by -create * -maybe(orReplace) * -maybe(editionable) * -pkg * -body * quoted * -remaining map {
        Ora.CreatePackageBody(
            it
        )
    }

    override val root by parser {
        choose(
            createTable,
            alterTableAddConstraint,
            alterTable,
            createView,
            createIndex,
            createSequence,
            createFunction,
            createProcedure,
            createTrigger,
            alterTrigger,
            createPackage,
            createPackageBody,
        )
    }
}
