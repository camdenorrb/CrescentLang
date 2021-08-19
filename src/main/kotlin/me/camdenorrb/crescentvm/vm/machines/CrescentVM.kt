package me.camdenorrb.crescentvm.vm.machines

import me.camdenorrb.crescentvm.vm.*
import java.nio.file.Path
import java.util.*
import kotlin.io.path.pathString
import kotlin.io.path.readText

class CrescentVM(val mode: VM = VM.INTERPRETED) {

    fun lex(input: String): List<CrescentToken> {
        return CrescentLexer.invoke(input)
    }

    fun parse(filePath: Path, input: List<CrescentToken>): Set<CrescentAST.Node.File> {

        val queue = LinkedList<CrescentAST.Node.File>()
        val imported = mutableSetOf<CrescentAST.Node.File>()

        queue.add(CrescentParser.invoke(filePath, input))

        while (queue.isNotEmpty()) {
            queue.pop().imports.forEach { import ->

                val importPath = Path.of(filePath.parent.pathString, import.path, "${import.typeName}.moon")
                val importParsed = CrescentParser.invoke(importPath, lex(importPath.readText()))

                imported.add(importParsed)
                queue.push(importParsed)
            }
        }

        return imported
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