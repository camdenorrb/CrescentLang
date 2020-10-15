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

                    mainFunction = readFunction(false, false, tokenIterator)
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

            // TODO: Account for visibility by peeking back
            variables += readVariable(CrescentAST.Visibility.PUBLIC, isFinal, tokenIterator)
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

                is CrescentToken.Modifier -> {
                    /*NOOP*/
                }

                CrescentToken.Statement.FUN -> {

                    val modifiers = tokenIterator.peekBackUntil { it !is CrescentToken.Modifier }

                    // TODO: Add isOperator
                    val isInline   = modifiers.contains(CrescentToken.Modifier.INLINE)
                    val isOverride = modifiers.contains(CrescentToken.Modifier.OVERRIDE)

                    // TODO: Maybe just take in modifiers into Function
                    functions += readFunction(isInline, isOverride, tokenIterator)
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

                is CrescentToken.Modifier -> {
                    /*NOOP*/
                }

                CrescentToken.Statement.FUN -> {

                    val modifiers = tokenIterator.peekBackUntil { it !is CrescentToken.Modifier }

                    // TODO: Add isOperator
                    val isInline   = modifiers.contains(CrescentToken.Modifier.INLINE)
                    val isOverride = modifiers.contains(CrescentToken.Modifier.OVERRIDE)

                    // TODO: Maybe just take in modifiers into Function
                    functions += readFunction(isInline, isOverride, tokenIterator)
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

    // TODO: Maybe just take a list of modifier tokens
    fun readFunction(isInline: Boolean, isOverride: Boolean, tokenIterator: PeekingTokenIterator): CrescentAST.Node.Function {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val parameters = readFunctionParameters(tokenIterator)

        val type = if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
            tokenIterator.next()
            readType(tokenIterator)
        }
        else {
            CrescentAST.Node.Type.Unit
        }

        // Skip rest until fully implemented
        tokenIterator.nextUntil { it == CrescentToken.Bracket.CLOSE }

        // TODO: Support visibility
        // TODO: Read parameters
        return CrescentAST.Node.Function(
            name,
            isOverride,
            CrescentAST.Visibility.PUBLIC,
            parameters,
            type,
            readExpression(tokenIterator)
        )
    }

    fun readFunctionTrait(tokenIterator: PeekingTokenIterator): CrescentAST.Node.FunctionTrait {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val parameters = readFunctionParameters(tokenIterator)

        val type = if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
            tokenIterator.next()
            readType(tokenIterator)
        }
        else {
            CrescentAST.Node.Type.Unit
        }

        return CrescentAST.Node.FunctionTrait(name, parameters, type)
    }

    fun readVariable(visibility: CrescentAST.Visibility, isFinal: Boolean, tokenIterator: PeekingTokenIterator): CrescentAST.Node.Variable {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        val type = if (tokenIterator.peekNext() == CrescentToken.Operator.VARIABLE_TYPE_PREFIX) {
            tokenIterator.next()
            readType(tokenIterator)
        }
        else {
            CrescentAST.Node.Type.Implicit
        }

        // Actually maybe they should support result types
        /*
        check(type !is CrescentAST.Node.Type.Result) {
            "Variables can't be result type... Yet"
        }
        */


        // TODO: Add visibility
        // TODO: Add expression
        // TODO: Add Type to below
        return CrescentAST.Node.Variable(name, isFinal, visibility, type, readExpression(tokenIterator))
    }

    fun readExpression(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Expression {
        // TODO: Implement
        return CrescentAST.Node.Expression(emptyList())
    }

    fun readFunctionParameters(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Parameter> {

        if (tokenIterator.peekNext() != CrescentToken.Bracket.OPEN) {
            return emptyList()
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)

        val parameters = mutableListOf<CrescentAST.Node.Parameter>()

        // TODO: Count opens and closes
        // TODO: Support default values
        while (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {

            val name = (tokenIterator.next() as CrescentToken.Key).string
            checkEquals(tokenIterator.next(), CrescentToken.Operator.VARIABLE_TYPE_PREFIX)
            val type = readType(tokenIterator)

            parameters += CrescentAST.Node.Parameter.Basic(name, type)
        }

        return parameters
    }

    fun readFunctionCall(tokenIterator: PeekingTokenIterator): CrescentAST.Node.FunctionCall {
        TODO()
    }

    fun readType(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Type {

        // TODO: Support Array type
        var type: CrescentAST.Node.Type

        when (val peekNext = tokenIterator.peekNext()) {

            CrescentToken.ArrayDeclaration.OPEN -> {

                tokenIterator.next()
                type = CrescentAST.Node.Type.Array(CrescentAST.Node.Type.Basic((tokenIterator.next() as CrescentToken.Key).string))

                // TODO: Maybe support array of results

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

        if (tokenIterator.peekNext() == CrescentToken.Operator.RESULT) {
            tokenIterator.next()
            type = CrescentAST.Node.Type.Result(type)
        }

        return type
    }

}