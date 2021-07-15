package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentVM
import me.camdenorrb.crescentvm.vm.VMModes
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
            file.forEachDirectoryEntry {
                if (!it.isDirectory() && it.endsWith(".moon")) {
                    files.add(it)
                }
            }
        } else {
            files.add(file)
        }

        val assemblies = mutableListOf<CrescentAST.Node.File>()
        val vm = CrescentVM(VMModes.JVM_BYTECODE)

        files.forEach { path ->
            val code = path.readBytes().decodeToString()
            val tokens = vm.lex(code)
            println(tokens)
            val assembly = vm.parse(path.toFile(), tokens)
            println(assembly)
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