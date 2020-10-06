package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.lexerold.CrescentLexer
import java.io.File

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        CrescentLexer(File("src/main/crescent/StructureExample.cr").readText()).invoke()

        /*
        CrescentExpression()
        CrescentValue(null, "10", CrescentDataType.I16)
        CrescentValue(null, "10", CrescentDataType.I16)
        */
    }

}