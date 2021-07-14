package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentVM
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.toPath

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val file = if (args.isNotEmpty()) {
            Paths.get(args[0]).toAbsolutePath().toUri()
        } else {
            this::class.java.getResource("/crescent/examples/math.moon")?.toURI()
        }?.toPath()?.toAbsolutePath()

        check(file != null && file.exists()) {
            "could not find: $file"
        }

        val code = file.readBytes().decodeToString()

        val vm = CrescentVM()
        val tokens = vm.lex(code)
        println(tokens)
        val assembly = vm.parse(file.toFile(), tokens)
        println(assembly)
        vm.invoke(listOf(assembly))
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