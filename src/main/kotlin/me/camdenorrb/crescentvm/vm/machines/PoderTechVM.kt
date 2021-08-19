package me.camdenorrb.crescentvm.vm.machines

import me.camdenorrb.crescentvm.vm.*
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechIR
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.pathString
import kotlin.io.path.readBytes
import kotlin.io.path.readText

class PoderTechVM(val mode: VM = VM.INTERPRETED) {

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

    /*
    fun load(classPaths: PoderTechIR) {

    }

    fun packageLib() {

    }

    fun invoke(clazzName: String) {
        packageLib()

    }*/
}