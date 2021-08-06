package me.camdenorrb.crescentvm.vm.machines

import me.camdenorrb.crescentvm.vm.*
//import me.camdenorrb.crescentvm.vm.jvm.JVMGenerator
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.readBytes

class CrescentVM(val mode: VM = VM.INTERPRETED) {

    fun lex(input: String): List<CrescentToken> {
        return CrescentLexer.invoke(input)
    }

    fun parse(file: File, input: List<CrescentToken>, imported: MutableSet<CrescentAST.Node.File>) {
        val tmp = CrescentParser.invoke(file, input)
        imported.add(tmp)
        tmp.imports.forEach {
            val path = if (it.path.isEmpty()) {
                Paths.get(file.parent, "${it.typeName}.moon")
            } else {
                Paths.get(file.parent, it.path, "${it.typeName}.moon")
            }
            parse(path.toFile(), lex(path.readBytes().decodeToString()), imported)
        }
    }

    fun invoke(input: Set<CrescentAST.Node.File>) {
        println("${input.size} assemblies specified!")
        when (mode) {
            //VMModes.INTERPRETED -> invokeInterpreted(input)
            VM.JVM_BYTECODE -> {}//JVMGenerator().generate(input)
            else -> TODO("Crescent VM $mode")
        }
    }

    /*private fun invokeInterpreted(input: List<CrescentAST.Node.File>) {
        input.forEach { assembly ->

        }
    }*/


}