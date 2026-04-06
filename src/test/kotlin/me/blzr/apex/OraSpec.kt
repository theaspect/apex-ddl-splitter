package me.blzr.apex

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import me.alllex.parsus.parser.getOrThrow

private fun parse(resourceName: String, format: Main.Format): List<Ora> {
    val text = OraSpec::class.java.getResourceAsStream(resourceName)!!
        .bufferedReader().readText()
    return Main.splitStatements(text, format).flatMap { node ->
        OraDumpGrammar().parse(node).getOrThrow()
    }
}

class OraSpec : StringSpec({
    /**
     * Old format: pure DDL (CREATE TABLE), each statement terminated with ';'.
     */
    "parse old format - DDL only" {
        parse("/dump-semicolon.sql", Main.Format.OLD).shouldNotBeEmpty()
    }

    /**
     * New format: mixed dump with DDL tables and PL/SQL packages.
     * Tables use ';' inside a slash-chunk; PL/SQL units are slash-terminated.
     * The dump-semicolon-1.sql file has a compound chunk pattern:
     *   CREATE TABLE ... ; CREATE TABLE ... ; CREATE PACKAGE ... END;
     * all between two '/' lines.
     */
    "parse new format - mixed DDL and PL/SQL" {
        parse("/dump-semicolon-1.sql", Main.Format.NEW).shouldNotBeEmpty()
    }

    /**
     * New format: slash-only dump where every statement is slash-terminated.
     */
    "parse new format - slash only" {
        parse("/dump-slash-1.sql", Main.Format.NEW).shouldNotBeEmpty()
    }

    /**
     * Old format: COMMENT ON COLUMN statements.
     */
    "parse old format - comment on column" {
        parse("/dump-comma.sql", Main.Format.OLD).shouldNotBeEmpty()
    }

    /**
     * New format: slash-chunk containing two statements packed together:
     *   CREATE TABLE ... ;
     *   CREATE UNIQUE INDEX ...
     * Reproduces the schema.sql pattern where the exporter packed a table
     * and its index into a single slash-delimited block.
     */
    "parse new format - multi-statement slash chunk" {
        val results = parse("/dump-mixed-slash.sql", Main.Format.NEW)
        results.shouldNotBeEmpty()
        assert(results.any { it is Ora.CreateTable }) { "expected CreateTable" }
        assert(results.any { it is Ora.CreateIndex }) { "expected CreateIndex" }
        assert(results.any { it is Ora.AlterTableAddConstraint }) { "expected AlterTableAddConstraint" }
        assert(results.any { it is Ora.CreatePackage }) { "expected CreatePackage" }
        assert(results.any { it is Ora.CreatePackageBody }) { "expected CreatePackageBody" }
    }

    /**
     * New format with CRLF line endings — the regex patterns handle \r\n natively
     * without any text normalization.
     */
    "parse new format - CRLF line endings" {
        val results = parse("/dump-mixed-slash-crlf.sql", Main.Format.NEW)
        results.shouldNotBeEmpty()
        assert(results.any { it is Ora.CreateTable }) { "expected CreateTable" }
        assert(results.any { it is Ora.CreatePackageBody }) { "expected CreatePackageBody" }
    }
})
