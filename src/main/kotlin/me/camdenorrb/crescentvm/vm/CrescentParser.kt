package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.extensions.nextUntil
import java.io.File

object CrescentParser {

    fun invoke(file: File, tokens: List<CrescentToken>): CrescentAST.Node.File {

        val imports = mutableListOf<String>()
        val structs = mutableListOf<CrescentAST.Node.Struct>()
        val traits  = mutableListOf<CrescentAST.Node.Trait>()
        var mainFunction: CrescentAST.Node.Function? = null

        val tokenIterator = tokens.iterator()

        while (tokenIterator.hasNext()) {

            val token = tokenIterator.next()

            // Should read top level tokens
            when (token) {

                is CrescentToken.Comment -> {
                    /*NOOP*/
                }

                CrescentToken.Type.STRUCT -> {
                    readStruct(tokenIterator)
                }

                CrescentToken.Type.TRAIT  -> {
                    readTrait(tokenIterator)
                }

                CrescentToken.Type.OBJECT -> {
                    readObject(tokenIterator)
                }

                CrescentToken.Statement.IMPORT -> {
                    val key = tokenIterator.next() as CrescentToken.Key
                    imports += key.string
                }

                CrescentToken.Statement.FUN -> {

                    check(mainFunction == null) {
                        "More than one main function???"
                    }

                    mainFunction = readFunction(tokenIterator)
                }

                else -> error()
            }
        }

        return CrescentAST.Node.File(
            file.nameWithoutExtension,
            file.path,
            imports,
            structs,
            traits,
            mainFunction
        )
    }

    fun readStruct(tokenIterator: Iterator<CrescentToken>): CrescentAST.Node.Struct {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        // Skip open bracket
        check(tokenIterator.next() == CrescentToken.Bracket.OPEN) {
            "Expected an open bracket"
        }

        // TODO: Figure out how to handle default lambdas
        val variables = tokenIterator.nextUntil { it == CrescentToken.Bracket.CLOSE }.map {
            readVariable(tokenIterator)
        }

        return CrescentAST.Node.Struct(name, variables)
    }

    fun readTrait(tokenIterator: Iterator<CrescentToken>): CrescentAST.Node.Trait {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        // Skip open bracket
        check(tokenIterator.next() == CrescentToken.Bracket.OPEN) {
            "Expected an open bracket"
        }

        return CrescentAST.Node.Trait(name, functionTraits)
    }

    fun readObject(tokenIterator: Iterator<CrescentToken>): CrescentAST.Node.Object {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        var openCount = 0
        var closeCount = 0

        while (true) {

            val token = tokenIterator.next()

            when (token) {

                is CrescentToken. -> {
                    readVariable(tokenIterator)
                }

                CrescentToken.Bracket.OPEN -> {
                    openCount++
                }

                CrescentToken.Bracket.CLOSE -> {

                    closeCount--

                    if (openCount == closeCount) {
                        break
                    }
                }

                else -> error()
            }
        }
        // Skip open bracket
        check(tokenIterator.next() == CrescentToken.Bracket.OPEN) {
            "Expected an open bracket"
        }



    }

    fun readFunction(tokenIterator: Iterator<CrescentToken>): CrescentAST.Node.Function {

    }

    fun readVariable(tokenIterator: Iterator<CrescentToken>): CrescentAST.Node.Variable {

    }

}