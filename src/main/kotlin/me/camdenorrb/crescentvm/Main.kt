package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.iterator.CrescentLexer

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        println(CrescentLexer.invoke(this::class.java.getResourceAsStream("crescent/examples/hello_world.moon").readBytes().contentToString()))
        //CrescentLexer(File("src/main/crescent/StructureExample.cr").readText()).invoke()

        /*
        CrescentExpression()
        CrescentValue(null, "10", CrescentDataType.I16)
        CrescentValue(null, "10", CrescentDataType.I16)
        */
    }

}