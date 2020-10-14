package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.extensions.nextUntil
import me.camdenorrb.crescentvm.iterator.PeekingTokenIterator
import me.camdenorrb.crescentvm.project.checkEquals
import java.io.File

// TODO: Maybe support comments
object CrescentParser {

    fun invoke(file: File, tokens: List<CrescentToken>): CrescentAST.Node.File {

        val imports = mutableListOf<String>()
        val structs = mutableListOf<CrescentAST.Node.Struct>()
        val impls   = mutableListOf<CrescentAST.Node.Impl>()
        val traits  = mutableListOf<CrescentAST.Node.Trait>()
        val objects = mutableListOf<CrescentAST.Node.Object>()

        var mainFunction: CrescentAST.Node.Function? = null
        val tokenIterator = PeekingTokenIterator(tokens)

        while (tokenIterator.hasNext()) {

            val token = tokenIterator.next()

            // Should read top level tokens
            when (token) {

                is CrescentToken.Comment -> {
                    /*NOOP*/
                }

                CrescentToken.Type.STRUCT -> {
                    structs += readStruct(tokenIterator)
                }

                CrescentToken.Type.IMPL -> {
                    impls += readImpl(tokenIterator)
                }

                CrescentToken.Type.TRAIT  -> {
                    traits += readTrait(tokenIterator)
                }

                CrescentToken.Type.OBJECT -> {
                    objects += readObject(tokenIterator)
                }

                CrescentToken.Statement.IMPORT -> {
                    // TODO: Add format verification
                    val key = tokenIterator.next() as CrescentToken.Key
                    imports += key.string
                }

                CrescentToken.Statement.FUN -> {

                    check(mainFunction == null) {
                        "More than one main function???"
                    }

                    mainFunction = readFunction(false, tokenIterator)
                }

                else -> error("Unexpected token: $token")
            }
        }

        return CrescentAST.Node.File(
            file.nameWithoutExtension,
            file.path,
            imports,
            structs,
            impls,
            traits,
            objects,
            mainFunction
        )
    }

    fun readStruct(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Struct {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        // Skip open bracket
        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)

        // TODO: Figure out how to handle default lambdas
        val variables = mutableListOf<CrescentAST.Node.Variable>()

        while (tokenIterator.hasNext()) {

            val nextToken = tokenIterator.next()

            if (nextToken == CrescentToken.Parenthesis.CLOSE) {
                break
            }

            val variableType = nextToken as CrescentToken.Variable
            val isFinal = variableType == CrescentToken.Variable.VAL

            // TODO: Account for visibility
            variables += readVariable(isFinal, tokenIterator)
        }

        return CrescentAST.Node.Struct(name, variables)
    }

    fun readTrait(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Trait {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val functionTraits = mutableListOf<CrescentAST.Node.FunctionTrait>()

        var openCount  = 0
        var closeCount = 0

        while (true) {

            val token = tokenIterator.next()

            when (token) {

                CrescentToken.Statement.FUN -> {
                    functionTraits += readFunctionTrait(tokenIterator)
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

                else -> error("Unexpected token $token")
            }

            check(openCount > 0) {
                "Did not open bracket?"
            }

        }

        return CrescentAST.Node.Trait(name, functionTraits)
    }

    fun readImpl(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Impl {

        val type = readType(tokenIterator)
        val functions = mutableListOf<CrescentAST.Node.Function>()

        var openCount  = 0
        var closeCount = 0

        while (true) {

            val token = tokenIterator.next()

            when (token) {

                CrescentToken.Statement.OVERRIDE -> {

                    // Skip Fun token
                    checkEquals(tokenIterator.next(), CrescentToken.Statement.FUN)

                    functions += readFunction(true, tokenIterator)
                }

                CrescentToken.Statement.FUN -> {
                    functions += readFunction(false, tokenIterator)
                }

                CrescentToken.Bracket.OPEN -> {
                    openCount++
                }

                CrescentToken.Bracket.CLOSE -> {

                    closeCount++

                    if (openCount == closeCount) {
                        break
                    }
                }

                else -> error("Unexpected token $token")
            }

            check(openCount > 0) {
                "Did not open bracket?"
            }

        }

        return CrescentAST.Node.Impl(type, functions)
    }

    fun readObject(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Object {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val functions = mutableListOf<CrescentAST.Node.Function>()

        var openCount  = 0
        var closeCount = 0

        while (true) {

            val token = tokenIterator.next()

            when (token) {

                CrescentToken.Statement.OVERRIDE -> {

                    // Skip Fun token
                    check(tokenIterator.next() == CrescentToken.Statement.FUN)

                    functions += readFunction(true, tokenIterator)
                }

                CrescentToken.Statement.FUN -> {
                    functions += readFunction(false, tokenIterator)
                }

                CrescentToken.Bracket.OPEN -> {
                    openCount++
                }

                CrescentToken.Bracket.CLOSE -> {

                    closeCount++

                    if (openCount == closeCount) {
                        break
                    }
                }

                else -> error("Unexpected token $token")
            }

            check(openCount > 0) {
                "Did not open bracket?"
            }

        }

        return CrescentAST.Node.Object(name, functions)

    }

    fun readFunction(isOverride: Boolean, tokenIterator: PeekingTokenIterator): CrescentAST.Node.Function {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val parameters = readFunctionParameters(tokenIterator)

        // Skip rest until fully implemented
        tokenIterator.nextUntil { it == CrescentToken.Bracket.CLOSE }

        // TODO: Support visibility
        // TODO: Read parameters
        return CrescentAST.Node.Function(name, isOverride, CrescentAST.Visibility.PUBLIC, parameters, readExpression(tokenIterator))
    }

    fun readFunctionTrait(tokenIterator: PeekingTokenIterator): CrescentAST.Node.FunctionTrait {
        val name = (tokenIterator.next() as CrescentToken.Key).string
        return CrescentAST.Node.FunctionTrait(name, readFunctionParameters(tokenIterator))
    }

    fun readVariable(isFinal: Boolean, tokenIterator: PeekingTokenIterator): CrescentAST.Node.Variable {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        // Skip type prefix
        checkEquals(tokenIterator.next(), CrescentToken.Operator.TYPE_PREFIX)

        // Could be absent
        val type = readType(tokenIterator)

        // TODO: Add visibility
        // TODO: Add expression
        // TODO: Add Type to below
        return CrescentAST.Node.Variable(name, isFinal, CrescentAST.Visibility.PUBLIC, type, readExpression(tokenIterator))
    }

    fun readExpression(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Expression {
        // TODO: Implement
        return CrescentAST.Node.Expression(emptyList())
    }

    fun readFunctionParameters(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Parameter> {

        if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
            return emptyList()
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)

        val parameters = mutableListOf<CrescentAST.Node.Parameter>()

        // TODO: Count opens and closes
        // TODO: Support default values
        while (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {

            val name = (tokenIterator.next() as CrescentToken.Key).string
            checkEquals(tokenIterator.next(), CrescentToken.Operator.TYPE_PREFIX)
            val type = readType(tokenIterator)

            parameters += CrescentAST.Node.Parameter(name, type)
        }

        return parameters
    }

    fun readFunctionCall(tokenIterator: PeekingTokenIterator): CrescentAST.Node.FunctionCall {
        TODO()
    }

    fun readType(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Type {

        // TODO: Support Array type
        val type: CrescentAST.Node.Type

        when (val peekNext = tokenIterator.peekNext()) {

            CrescentToken.ArrayDeclaration.OPEN -> {

                tokenIterator.next()
                type = CrescentAST.Node.Type.Array(CrescentAST.Node.Type.Basic((tokenIterator.next() as CrescentToken.Key).string))

                // Skip Array close
                checkEquals(tokenIterator.next(), CrescentToken.ArrayDeclaration.CLOSE)
            }

            is CrescentToken.Key -> {
                tokenIterator.next()
                type = CrescentAST.Node.Type.Basic(peekNext.string)
            }

            else -> {
                type = CrescentAST.Node.Type.Implicit
            }

        }

        return type
    }

}