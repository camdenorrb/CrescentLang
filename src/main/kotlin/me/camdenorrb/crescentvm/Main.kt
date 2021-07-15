package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.machines.CrescentVM
import me.camdenorrb.crescentvm.vm.VMModes
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

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
        val files = mutableListOf<Path>()
        if (file.isDirectory()) {
            Files.walk(file).forEach {
                if (!it.isDirectory() && it.extension == "moon") {
                    files.add(it)
                }
            }
        } else {
            files.add(file)
        }

        val assemblies = mutableSetOf<CrescentAST.Node.File>()
        val vm = CrescentVM(VMModes.JVM_BYTECODE)

        files.forEach { path ->
            val code = path.readBytes().decodeToString()
            val tokens = vm.lex(code)
            println(tokens)
            vm.parse(path.toFile(), tokens, assemblies)
        }

        vm.invoke(assemblies)
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