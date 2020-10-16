package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import java.io.File

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val file = File("/crescent/examples/hello_world.moon")
        val code = this::class.java.getResourceAsStream("/crescent/examples/hello_world.moon").readBytes().decodeToString()

        println(CrescentParser.invoke(file, CrescentLexer.invoke(code)))


        //println(CrescentLexer.invoke(code))
        //CrescentLexer(File("src/main/crescent/StructureExample.cr").readText()).invoke()

        /*
        CrescentExpression()
        CrescentValue(null, "10", CrescentDataType.I16)
        CrescentValue(null, "10", CrescentDataType.I16)
        */
    }

}