package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.vm.jvm.JVMGenerator
import java.io.File

class CrescentVM(val mode: VMModes = VMModes.INTERPRETED) {

    fun lex(input: String): List<CrescentToken> {
        return CrescentLexer.invoke(input)
    }

    fun parse(file: File, input: List<CrescentToken>): CrescentAST.Node.File {
        return CrescentParser.invoke(file, input)
    }

    fun invoke(input: List<CrescentAST.Node.File>) {
        println("${input.size} assemblies specified!")
        when (mode) {
            //VMModes.INTERPRETED -> invokeInterpreted(input)
            VMModes.JVM_BYTECODE -> JVMGenerator().generate(input)
            else -> TODO("Crescent VM $mode")
        }
    }

    /*private fun invokeInterpreted(input: List<CrescentAST.Node.File>) {
        input.forEach { assembly ->

        }
    }*/


}