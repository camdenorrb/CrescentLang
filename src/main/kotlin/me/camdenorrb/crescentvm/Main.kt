package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.lexer.CrescentLexer
import java.io.File

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        CrescentLexer(File("src/main/crescent/StructureExample.cr").readText()).invoke()
    }

}