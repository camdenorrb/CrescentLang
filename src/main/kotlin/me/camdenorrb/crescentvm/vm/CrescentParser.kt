package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.iterator.PeekingTokenIterator
import me.camdenorrb.crescentvm.project.checkEquals
import java.io.File
import kotlin.math.exp

// TODO: Maybe support comments
object CrescentParser {

    fun invoke(file: File, tokens: List<CrescentToken>): CrescentAST.Node.File {

        val impls = mutableListOf<CrescentAST.Node.Impl>()
        val enums = mutableListOf<CrescentAST.Node.Enum>()
        val traits = mutableListOf<CrescentAST.Node.Trait>()

        val imports = mutableListOf<CrescentAST.Node.Import>()
        val structs = mutableListOf<CrescentAST.Node.Struct>()
        val objects = mutableListOf<CrescentAST.Node.Object>()

        val functions = mutableListOf<CrescentAST.Node.Function>()
        val variables = mutableListOf<CrescentAST.Node.Variable>()
        val constants = mutableListOf<CrescentAST.Node.Constant>()

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

                CrescentToken.Type.TRAIT -> {
                    traits += readTrait(tokenIterator)
                }

                CrescentToken.Type.OBJECT -> {
                    objects += readObject(tokenIterator)
                }

                CrescentToken.Type.ENUM -> {
                    enums += readEnum(tokenIterator)
                }

                CrescentToken.Variable.VAL, CrescentToken.Variable.VAR -> {

                    val isFinal = token == CrescentToken.Variable.VAL

                    val modifiers = tokenIterator.peekBackUntil {
                        it !is CrescentToken.Modifier
                    }

                    val visibility = when (modifiers.find { (it as CrescentToken.Modifier).isVisibility() } ?: CrescentToken.Modifier.PUBLIC) {

                        CrescentToken.Modifier.PUBLIC   -> CrescentAST.Visibility.PUBLIC
                        CrescentToken.Modifier.INTERNAL -> CrescentAST.Visibility.INTERNAL
                        CrescentToken.Modifier.PRIVATE  -> CrescentAST.Visibility.PRIVATE

                        else -> error("Visibility isn't valid?")
                    }

                    variables += readVariable(tokenIterator, visibility, isFinal)
                }

                CrescentToken.Statement.FUN -> {

                    val function = readFunction(tokenIterator)

                    if (function.name == "main") {

                        check(mainFunction == null) {
                            "More than one main function???"
                        }

                        mainFunction = function
                    }

                    functions += function
                }

                CrescentToken.Statement.IMPORT -> {

                    // If it's a local import
                    if (tokenIterator.peekNext() == CrescentToken.Operator.IMPORT_SEPARATOR) {

                        // Skip import separator
                        tokenIterator.next()

                        imports += CrescentAST.Node.Import("", (tokenIterator.next() as CrescentToken.Key).string)

                        continue
                    }


                    val path = buildString {

                        append((tokenIterator.next() as CrescentToken.Key).string)

                        while (tokenIterator.peekNext() == CrescentToken.Operator.DOT) {

                            // Skip the dot
                            tokenIterator.next()

                            append('.').append((tokenIterator.next() as CrescentToken.Key).string)
                        }
                    }

                    check(tokenIterator.next() == CrescentToken.Operator.IMPORT_SEPARATOR) {
                        "Incorrect import format expected `{path}::{type}`"
                    }

                    val type = (tokenIterator.next() as CrescentToken.Key).string

                    imports += CrescentAST.Node.Import(path, type)
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
            variables,
            constants,
            functions,
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

            when(val nextToken = tokenIterator.next()) {

                CrescentToken.Parenthesis.CLOSE -> break
                is CrescentToken.Comment -> continue

                is CrescentToken.Variable -> {

                    val isFinal = nextToken == CrescentToken.Variable.VAL

                    val modifiers = tokenIterator.peekBackUntil {
                        it !is CrescentToken.Modifier
                    }

                    val visibility = when (modifiers.find { (it as CrescentToken.Modifier).isVisibility() } ?: CrescentToken.Modifier.PUBLIC) {

                        CrescentToken.Modifier.PUBLIC   -> CrescentAST.Visibility.PUBLIC
                        CrescentToken.Modifier.INTERNAL -> CrescentAST.Visibility.INTERNAL
                        CrescentToken.Modifier.PRIVATE  -> CrescentAST.Visibility.PRIVATE

                        else -> error("Visibility isn't valid?")
                    }

                    variables += readVariable(tokenIterator, visibility, isFinal)
                }

                else -> error("Unexpected token $nextToken")
            }

        }

        return CrescentAST.Node.Struct(name, variables)
    }

    fun readTrait(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Trait {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val functionTraits = mutableListOf<CrescentAST.Node.FunctionTrait>()

        readNextUntilClosed(tokenIterator) { token ->

            when (token) {

                CrescentToken.Bracket.OPEN, CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE -> {
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
                    functions += readFunction(tokenIterator)
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
        val constants = mutableListOf<CrescentAST.Node.Constant>()

        readNextUntilClosed(tokenIterator) { token ->

            when (token) {

                CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE, is CrescentToken.Modifier -> {
                    /*NOOP*/
                }

                CrescentToken.Variable.VAL -> {
                    val visibility =
                        tokenIterator.peekBack() as? CrescentAST.Visibility ?: CrescentAST.Visibility.PUBLIC
                    variables += readVariable(tokenIterator, visibility, isFinal = true)
                }

                CrescentToken.Statement.FUN -> {
                    functions += readFunction(tokenIterator)
                }

                else -> error("Unexpected token $token")
            }
        }

        return CrescentAST.Node.Object(name, variables, functions, constants)

    }

    fun readEnum(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Enum {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val parameters = readParameters(tokenIterator)
        val entries = mutableListOf<CrescentAST.Node.EnumEntry>()

        readNextUntilClosed(tokenIterator) { token ->

            if (token == CrescentToken.Bracket.OPEN) {
                return@readNextUntilClosed
            }

            val entryName = (token as CrescentToken.Key).string
            val entryArguments = readArguments(tokenIterator)

            entries += CrescentAST.Node.EnumEntry(entryName, entryArguments)
        }

        return CrescentAST.Node.Enum(name, parameters, entries)
    }

    // TODO: Maybe just take a list of modifier tokens
    fun readFunction(
        tokenIterator: PeekingTokenIterator,
        //modifiers: List<CrescentToken.Modifier>
    ): CrescentAST.Node.Function {

        val modifiers = tokenIterator
            .peekBackUntil { it !is CrescentToken.Modifier && it != CrescentToken.Statement.FUN }
            .mapNotNull { it as? CrescentToken.Modifier }

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val parameters = readParameters(tokenIterator)

        val type =
            if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
                tokenIterator.next()
                readType(tokenIterator)
            }
            else {
                CrescentAST.Node.Type.Unit
            }

        val expressions = readBlock(tokenIterator)

        // TODO: Support visibility
        return CrescentAST.Node.Function(
            name,
            modifiers,
            CrescentAST.Visibility.PUBLIC,
            parameters,
            type,
            expressions,
        )
    }

    fun readBlock(tokenIterator: PeekingTokenIterator) : CrescentAST.Node.Statement.Block {

        val expressions = mutableListOf<CrescentAST.Node.Expression>()

        checkEquals(tokenIterator.next(), CrescentToken.Bracket.OPEN)

        while (tokenIterator.peekNext() != CrescentToken.Bracket.CLOSE) {
            expressions += readExpression(tokenIterator)
        }

        checkEquals(tokenIterator.next(), CrescentToken.Bracket.CLOSE)

        return CrescentAST.Node.Statement.Block(expressions)
    }

    fun readFunctionTrait(tokenIterator: PeekingTokenIterator): CrescentAST.Node.FunctionTrait {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val parameters = readParameters(tokenIterator)

        val type = if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
            tokenIterator.next()
            readType(tokenIterator)
        } else {
            CrescentAST.Node.Type.Unit
        }

        return CrescentAST.Node.FunctionTrait(name, parameters, type)
    }

    fun readVariable(
        tokenIterator: PeekingTokenIterator,
        visibility: CrescentAST.Visibility,
        isFinal: Boolean,
    ): CrescentAST.Node.Variable {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        val type =
            if (tokenIterator.peekNext() == CrescentToken.Operator.TYPE_PREFIX) {
                tokenIterator.next()
                readType(tokenIterator)
            }
            else {
                CrescentAST.Node.Type.Implicit
            }

        val expression =
            if (tokenIterator.peekNext() == CrescentToken.Operator.ASSIGN) {
                checkEquals(tokenIterator.next(), CrescentToken.Operator.ASSIGN)
                readExpression(tokenIterator)
            }
            else {
                CrescentAST.Node.Expression(emptyList())
            }


        return CrescentAST.Node.Variable(name, isFinal, visibility, type, expression)
    }

    fun readParameters(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Parameter> {

        if (tokenIterator.peekNext() != CrescentToken.Parenthesis.OPEN) {
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

            parameters += CrescentAST.Node.Parameter.Basic(name, type)
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)

        return parameters
    }

    fun readArguments(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Argument> {

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

    fun readWhen(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Statement.When {

        val argument = if (tokenIterator.peekNext() == CrescentToken.Parenthesis.OPEN) {
            checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)
            readExpression(tokenIterator)
        }
        else {
            CrescentAST.Node.Expression(emptyList())
        }

        val clauses = mutableListOf<CrescentAST.Node.Statement.When.Clause>()

        //println(parameter)

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)

        readNextUntilClosed(tokenIterator) {

            // Unskip the read token from readNextUntilClosed(tokenIterator)
            tokenIterator.back()

            //println(tokenIterator.peekNext())

            val ifExpression = readExpression(tokenIterator)

            //println(ifExpression)

            //println(ifExpression)
            val firstNode = ifExpression.nodes.first()

            // If is else statement, no ifExpression
            if (firstNode is CrescentAST.Node.Statement.Else) {
                clauses += CrescentAST.Node.Statement.When.Clause(null, firstNode.block)
                return@readNextUntilClosed
            }

            //println(tokenIterator.peekNext())

            /*
            if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
                tokenIterator.next()
            }
            */


            val thenExpressions =
                if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
                    readBlock(tokenIterator)
                }
                else {
                    CrescentAST.Node.Statement.Block(listOf(readExpression(tokenIterator)))
                }

            //println(ifExpression)
            //tokenIterator.peekNext()

            clauses += CrescentAST.Node.Statement.When.Clause(ifExpression, thenExpressions)

            //println(thenExpression)
        }

        return CrescentAST.Node.Statement.When(CrescentAST.Node.Argument(argument), clauses)
    }

    fun readType(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Type {

        var type: CrescentAST.Node.Type

        when (val peekNext = tokenIterator.peekNext()) {

            CrescentToken.SquareBracket.OPEN -> {

                tokenIterator.next()
                type = CrescentAST.Node.Type.Array(CrescentAST.Node.Type.Basic((tokenIterator.next() as CrescentToken.Key).string))

                // TODO: Maybe support array of results

                // Skip Array close
                checkEquals(tokenIterator.next(), CrescentToken.SquareBracket.CLOSE)
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

                    if (nodes.isEmpty()) {
                        nodes += CrescentAST.Node.Return(readExpression(tokenIterator))
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                CrescentToken.Statement.WHEN -> {

                    if (nodes.isEmpty()) {
                        nodes += readWhen(tokenIterator)
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                CrescentToken.Variable.VAL -> {

                    if (nodes.isEmpty()) {
                        nodes += readVariable(tokenIterator, CrescentAST.Visibility.LOCAL_SCOPE, true)
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                CrescentToken.Variable.VAR -> {

                    if (nodes.isEmpty()) {
                        nodes += readVariable(tokenIterator, CrescentAST.Visibility.LOCAL_SCOPE, false)
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                // TODO: This should be done differently, as in this function should consume the closing tokens
                CrescentToken.Parenthesis.OPEN -> {

                    if (nodes.isEmpty()) {
                        nodes += readExpression(tokenIterator)
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                CrescentToken.Operator.INSTANCE_OF -> {

                    if (nodes.isEmpty()) {
                        nodes += CrescentAST.Node.InstanceOf(readExpression(tokenIterator))
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                CrescentToken.Statement.IF -> {

                    if (nodes.isEmpty()) {

                        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)

                        nodes += CrescentAST.Node.Statement.If(
                            readExpression(tokenIterator).also { checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE) },
                            readBlock(tokenIterator)
                        )
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                CrescentToken.Statement.ELSE -> {

                    if (nodes.isEmpty()) {
                        nodes += CrescentAST.Node.Statement.Else(readBlock(tokenIterator))
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                is CrescentToken.Comment -> {
                    /*NOOP*/
                    continue
                }

                is CrescentToken.String -> {
                    CrescentAST.Node.String(next.kotlinString)
                }

                is CrescentToken.Char -> {

                    //println("Last: ${nodes.lastOrNull()?.let { it::class }}")

                    if (nodes.lastOrNull() != null && operator == null) {
                        tokenIterator.back()
                        break
                    }


                    //println(next.kotlinChar)

                    CrescentAST.Node.Char(next.kotlinChar)
                }

                is CrescentToken.Boolean -> {

                    if (nodes.lastOrNull() != null && operator == null) {
                        tokenIterator.back()
                        break
                    }

                    CrescentAST.Node.Boolean(next.kotlinBoolean)
                }

                is CrescentToken.Number -> {

                    if (nodes.lastOrNull() != null && operator == null) {
                        tokenIterator.back()
                        break
                    }

                    CrescentAST.Node.Number(next.number)
                }

                is CrescentToken.Operator -> {
                    operator = next
                    continue
                }

                is CrescentToken.Key -> {

                    when (tokenIterator.peekNext()) {

                        CrescentToken.Parenthesis.OPEN -> {
                            CrescentAST.Node.FunctionCall(next.string, readArguments(tokenIterator))
                        }

                        CrescentToken.SquareBracket.OPEN -> {
                            checkEquals(tokenIterator.next(), CrescentToken.SquareBracket.OPEN)
                            CrescentAST.Node.ArrayCall(next.string,
                                (tokenIterator.next() as CrescentToken.Number).number.toInt()).also {
                                tokenIterator.next()
                            }
                        }

                        else -> {

                            if (nodes.lastOrNull() != null && operator == null) {
                                tokenIterator.back()
                                break
                            }

                            CrescentAST.Node.VariableCall(next.string)
                        }

                    }
                }

                CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.CLOSE -> {
                    tokenIterator.back()
                    break
                }

                else -> {
                    error("Unexpected token: $next")
                }
            }

            // If an inlined operator was the previous node
            if (operator != null && operator != CrescentToken.Operator.DOT) {
                nodes += CrescentAST.Node.Operation(operator, nodes.removeLast(), node)
                operator = null
            }
            else {
                nodes += node
            }

        }

        return CrescentAST.Node.Expression(nodes)
    }

    inline fun readNextUntilClosed(tokenIterator: PeekingTokenIterator, block: (token: CrescentToken) -> Unit) {

        var count = 0

        while (tokenIterator.hasNext()) {

            val token = tokenIterator.next()

            when(token) {
                CrescentToken.Bracket.OPEN -> {
                    count++
                }
                CrescentToken.Bracket.CLOSE -> {

                    count--

                    if (count <= 0) {
                        break
                    }
                }
            }

            block(token)
        }
    }

}