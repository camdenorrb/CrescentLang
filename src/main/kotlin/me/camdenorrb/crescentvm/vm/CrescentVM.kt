package me.camdenorrb.crescentvm.vm

import java.io.File

class CrescentVM {

    fun lex(input: String): List<CrescentToken> {
        return CrescentLexer.invoke(input)
    }

    fun parse(file: File, input: List<CrescentToken>): CrescentAST.Node.File {
        return CrescentParser.invoke(file, input)
    }

    fun invoke(input: List<CrescentAST.Node.File>) {
        println("${input.size} assemblies specified!")
    }

}