package me.camdenorrb.crescentvm.vm

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
        val enums   = mutableListOf<CrescentAST.Node.Enum>()

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

                CrescentToken.Type.ENUM -> {
                    enums += readEnum(tokenIterator)
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

                    mainFunction = readFunction(emptyList(), tokenIterator)
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
            enums,
            mainFunction
        )
    }

    fun readStruct(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Struct {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        // Skip open bracket
        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)

        // TODO: Figure out how to handle default lambdas, by counting open/close or checking for = sign
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

        readNextUntilClosed(tokenIterator) { token ->

            when (token) {

                CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE -> {
                    /*NOOP*/
                }

                CrescentToken.Statement.FUN -> {
                    functionTraits += readFunctionTrait(tokenIterator)
                }

                else -> error("Unexpected token $token")
            }

        }

        return CrescentAST.Node.Trait(name, functionTraits)
    }

    fun readImpl(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Impl {

        val type = readType(tokenIterator)
        val functions = mutableListOf<CrescentAST.Node.Function>()

        readNextUntilClosed(tokenIterator) { token ->

            when (token) {

                CrescentToken.Bracket.OPEN, is CrescentToken.Comment, CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE, is CrescentToken.Modifier -> {
                    /*NOOP*/
                }


                CrescentToken.Statement.FUN -> {

                    val modifiers = tokenIterator
                        .peekBackUntil { it !is CrescentToken.Modifier && it != CrescentToken.Statement.FUN }
                        .mapNotNull { it as? CrescentToken.Modifier }

                    functions += readFunction(modifiers, tokenIterator)
                }

                else -> error("Unexpected token $token")
            }
        }

        return CrescentAST.Node.Impl(type, functions, emptyList())
    }

    fun readObject(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Object {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        val variables = mutableListOf<CrescentAST.Node.Variable>()
        val functions = mutableListOf<CrescentAST.Node.Function>()

        readNextUntilClosed(tokenIterator) { token ->

            when (token) {

                CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE, is CrescentToken.Modifier -> {
                    /*NOOP*/
                }

                CrescentToken.Variable.VAL -> {
                    val visibility = tokenIterator.peekBack() as? CrescentAST.Visibility ?: CrescentAST.Visibility.PUBLIC
                    variables += readVariable(visibility, isFinal = true, tokenIterator)
                }

                CrescentToken.Statement.FUN -> {

                    val modifiers = tokenIterator
                        .peekBackUntil { it !is CrescentToken.Modifier && it != CrescentToken.Statement.FUN }
                        .mapNotNull { it as? CrescentToken.Modifier }

                    functions += readFunction(modifiers, tokenIterator)
                }

                else -> error("Unexpected token $token")
            }
        }

        return CrescentAST.Node.Object(name, variables, functions)

    }

    fun readEnum(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Enum {
        TODO()
    }

    // TODO: Maybe just take a list of modifier tokens
    fun readFunction(modifiers: List<CrescentToken.Modifier>, tokenIterator: PeekingTokenIterator): CrescentAST.Node.Function {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val parameters = readFunctionParameters(tokenIterator)

        val type = if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
            tokenIterator.next()
            readType(tokenIterator)
        }
        else {
            CrescentAST.Node.Type.Unit
        }

        val expression = readExpression(tokenIterator)
        checkEquals(tokenIterator.next(), CrescentToken.Bracket.CLOSE)
        //println(expression)

        // Skip rest until fully implemented
        //tokenIterator.nextUntil { it == CrescentToken.Bracket.CLOSE }

        //println(tokenIterator.peekNext().javaClass.canonicalName)

        // TODO: Support visibility
        // TODO: Read parameters
        return CrescentAST.Node.Function(
            name,
            modifiers,
            CrescentAST.Visibility.PUBLIC,
            parameters,
            type,
            expression
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

        val expression = if (tokenIterator.peekNext() == CrescentToken.Operator.ASSIGN) {
            tokenIterator.next()
            readExpression(tokenIterator)
        }
        else {
            CrescentAST.Node.Expression(emptyList())
        }

        // TODO: Add visibility
        // TODO: Add expression
        // TODO: Add Type to below
        return CrescentAST.Node.Variable(name, isFinal, visibility, type, expression)
    }

    fun readFunctionParameters(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Parameter> {

        if (tokenIterator.peekNext() != CrescentToken.Parenthesis.OPEN) {
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

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)

        return parameters
    }

    fun readFunctionArguments(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Argument> {

        if (tokenIterator.peekNext() != CrescentToken.Parenthesis.OPEN) {
            return emptyList()
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)
        val arguments = mutableListOf<CrescentAST.Node.Argument>()

        // TODO: Count opens and closes
        while (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {
            arguments += CrescentAST.Node.Argument(readExpression(tokenIterator))
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)

        return arguments
    }

    fun readType(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Type {

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

    fun readExpression(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Expression {

        val nodes = mutableListOf<CrescentAST.Node>()
        var operator: CrescentToken.Operator? = null

        if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
            tokenIterator.next()
        }

        while (true) {

            val node = when (val next = tokenIterator.next()) {

                CrescentToken.Operator.RETURN -> {
                    nodes += CrescentAST.Node.Return(readExpression(tokenIterator))
                    break
                }

                is CrescentToken.Comment -> {
                    /*NOOP*/
                    continue
                }

                is CrescentToken.String -> {
                    CrescentAST.Node.String(next.kotlinString)
                }

                CrescentToken.Variable.VAL -> {
                    readVariable(CrescentAST.Visibility.LOCAL_SCOPE, isFinal = true, tokenIterator)
                }

                CrescentToken.Variable.VAR -> {
                    readVariable(CrescentAST.Visibility.LOCAL_SCOPE, isFinal = false, tokenIterator)
                }

                is CrescentToken.Operator -> {
                    operator = next
                    continue
                }

                is CrescentToken.Number -> {
                    CrescentAST.Node.Number(next.number)
                }

                is CrescentToken.Key -> {
                    if (tokenIterator.peekNext() == CrescentToken.Parenthesis.OPEN) {
                        CrescentAST.Node.FunctionCall(next.string, readFunctionArguments(tokenIterator))
                    }
                    else {
                        CrescentAST.Node.VariableCall(next.string)
                    }
                }

                // TODO: This should be done differently
                CrescentToken.Parenthesis.OPEN -> {
                    readExpression(tokenIterator).also {
                        tokenIterator.next()
                    }
                }

                else -> {
                    tokenIterator.back()
                    break
                }
            }

            // If an inlined operator was the previous node
            if (operator != null) {

                nodes += CrescentAST.Node.Operation(
                    operator,
                    nodes.removeLast(),
                    node
                )

                operator = null
            }
            else {
                nodes += node
            }

        }

        return CrescentAST.Node.Expression(nodes)
    }


    inline fun readNextUntilClosed(tokenIterator: PeekingTokenIterator, block: (token: CrescentToken) -> Unit) {

        var openCount = 0
        var closeCount = 0

        while (true) {

            val token = tokenIterator.next()

            when (token) {

                CrescentToken.Bracket.OPEN -> {
                    openCount++
                    block(token)
                }

                CrescentToken.Bracket.CLOSE -> {

                    closeCount++

                    if (openCount == closeCount) {
                        break
                    }

                    block(token)
                }

                else -> block(token)
            }
        }

        check(openCount > 0) {
            "Did not open bracket?"
        }
    }

}