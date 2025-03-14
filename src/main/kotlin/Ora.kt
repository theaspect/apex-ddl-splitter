package com.blzr

sealed class Ora {
    data class CreateTable(override val name: String, override val fileName: String = "table ${name.lowercase()}.sql") : Ora()
    data class AlterTable(override val name: String, override val fileName: String = "table ${name.lowercase()} alter.sql") : Ora()
    data class AlterTableAddConstraint(
        val table: String,
        override val name: String,
        override val fileName: String = "table ${table.lowercase()} constraint ${name.lowercase()}.sql"
    ) : Ora()

    data class CreateView(override val name: String, override val fileName: String = "view ${name.lowercase()}.sql") : Ora()
    data class CreateIndex(override val name: String, override val fileName: String = "index ${name.lowercase()}.sql") : Ora()
    data class CreateSequence(override val name: String, override val fileName: String = "sequence ${name.lowercase()}.sql") : Ora()
    data class CreateFunction(override val name: String, override val fileName: String = "function ${name.lowercase()}.sql") : Ora()
    data class CreateProcedure(override val name: String, override val fileName: String = "procedure ${name.lowercase()}.sql") : Ora()
    data class CreateTrigger(override val name: String, override val fileName: String = "trigger ${name.lowercase()}.sql") : Ora()
    data class AlterTrigger(override val name: String, override val fileName: String = "trigger ${name.lowercase()} alter.sql") :
        Ora()

    data class CreatePackage(override val name: String, override val fileName: String = "package ${name.lowercase()}.sql") : Ora()
    data class CreatePackageBody(override val name: String, override val fileName: String = "package ${name.lowercase()} body.sql") :
        Ora()


    abstract val name: String
    abstract val fileName: String
}
