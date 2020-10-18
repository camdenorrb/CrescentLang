package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import java.io.File

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val file = File("/crescent/examples/math.moon")
        val code = this::class.java.getResourceAsStream(file.path).readBytes().decodeToString()

        println(CrescentLexer.invoke(code))
        println(CrescentParser.invoke(file, CrescentLexer.invoke(code)))
        /*
        repeat(100000) {
            println(measureNanoTime {
                CrescentParser.invoke(file, CrescentLexer.invoke(code))
            })
        }
        */


        //println(CrescentLexer.invoke(code))
        //CrescentLexer(File("src/main/crescent/StructureExample.cr").readText()).invoke()

        /*
        CrescentExpression()
        CrescentValue(null, "10", CrescentDataType.I16)
        CrescentValue(null, "10", CrescentDataType.I16)
        */
    }

}