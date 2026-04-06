package me.blzr.apex

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

class OraDumpGrammar() : Grammar<List<Ora>>(ignoreCase = true, debugMode = true) {
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

    val comment = literalToken("comment on column")

    val quote = literalToken("\"")
    val name = regexToken("[\\w$]+")
    val lazyRemaining = regexToken(Regex(".*?", RegexOption.DOT_MATCHES_ALL))
    val remaining = regexToken(Regex(".*", RegexOption.DOT_MATCHES_ALL))
    val exceptSemicolon = regexToken(Regex("[^;]*", RegexOption.DOT_MATCHES_ALL))

    val semicolon = literalToken(";")
    val slash = regexToken(Regex("^/$", RegexOption.DOT_MATCHES_ALL))

    val semicolonOrSlash by semicolon or slash

    val dot = literalToken(".")

    val quoted by -quote * ref(::name) * -quote map { it.text }
    val unquoted by ref(::name) map { it.text }
    val identifier by quoted or unquoted

    val createTable by -create * -maybe(orReplace) * -table * identifier * -remaining * -maybe(semicolonOrSlash) map { Ora.CreateTable(it) }

    val alterTable by -alter * -table * identifier * -remaining map { Ora.AlterTable(it) }
    val alterTableAddConstraint by -alter * -table * identifier * -add * -constraint * identifier * -remaining map { (a, b) ->
        Ora.AlterTableAddConstraint(a, b)
    }

    val createView by -create * -maybe(orReplace) * -maybe(force) * -maybe(editionable) * -view * identifier * -remaining map {
        Ora.CreateView(
            it
        )
    }

    val createIndex by -create * -maybe(orReplace) * -maybe(unique) * -maybe(bitmap) * -index * identifier * -remaining map
            { Ora.CreateIndex(it) }
    val createSequence by -create * -maybe(orReplace) * -sequence * identifier * -remaining map
            { Ora.CreateSequence(it) }

    val createFunction by -create * -maybe(orReplace) * -maybe(editionable) * -function * identifier * -remaining map
            { Ora.CreateFunction(it) }
    val createProcedure by -create * -maybe(orReplace) * -maybe(editionable) * -procedure * identifier * -remaining map
            { Ora.CreateProcedure(it) }
    val createTrigger by -create * -maybe(orReplace) * -maybe(editionable) * -trigger * identifier * -remaining map
            { Ora.CreateTrigger(it) }
    val alterTrigger by -alter * -trigger * identifier * -remaining map
            { Ora.AlterTrigger(it) }

    val createPackage by -create * -maybe(orReplace) * -maybe(editionable) * -pkg * identifier * -remaining map {
        Ora.CreatePackage(it)
    }
    val createPackageBody by -create * -maybe(orReplace) * -maybe(editionable) * -pkg * -body * identifier * -remaining map {
        Ora.CreatePackageBody(it)
    }

    val commentColumn by -comment * identifier * -dot * identifier * -remaining map { (table, column) ->
        Ora.Comment(table, column)
    }

    val obj by parser {
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
            createPackageBody,  // must precede createPackage — BODY is an unquoted word that
            createPackage,      // createPackage's identifier would otherwise swallow
            commentColumn,
        )
    }

    override val root by zeroOrMore(obj)
}
