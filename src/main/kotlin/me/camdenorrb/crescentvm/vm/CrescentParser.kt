package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.iterator.PeekingTokenIterator
import me.camdenorrb.crescentvm.project.checkEquals
import java.nio.file.Path

// TODO: Maybe support comments
object CrescentParser {

    fun invoke(filePath: Path, tokens: List<CrescentToken>): CrescentAST.Node.File {

        val impls = mutableListOf<CrescentAST.Node.Impl>()
        val enums = mutableListOf<CrescentAST.Node.Enum>()
        val traits = mutableListOf<CrescentAST.Node.Trait>()

        val sealeds = mutableListOf<CrescentAST.Node.Sealed>()
        val imports = mutableListOf<CrescentAST.Node.Import>()
        val structs = mutableListOf<CrescentAST.Node.Struct>()
        val objects = mutableListOf<CrescentAST.Node.Object>()

        val functions = mutableListOf<CrescentAST.Node.Function>()
        val variables = mutableListOf<CrescentAST.Node.Variable>()
        val constants = mutableListOf<CrescentAST.Node.Constant>()

        var mainFunction: CrescentAST.Node.Function? = null
        val tokenIterator = PeekingTokenIterator(tokens)

        var visibility = CrescentToken.Visibility.PUBLIC
        val modifiers = mutableListOf<CrescentToken.Modifier>()

        while (tokenIterator.hasNext()) {

            val token = tokenIterator.next()

            // Should read top level tokens
            when (token) {

                is CrescentToken.Data.Comment -> {
                    /*NOOP*/
                }

                is CrescentToken.Visibility -> {
                    visibility = token
                    continue
                }

                is CrescentToken.Modifier -> {
                    modifiers += token
                    continue
                }

                CrescentToken.Type.STRUCT -> {
                    structs += readStruct(tokenIterator)
                }

                CrescentToken.Type.IMPL -> {
                    impls += readImpl(tokenIterator)
                }

                CrescentToken.Variable.CONST -> {
                    constants += readConstant(tokenIterator, visibility)
                }

                CrescentToken.Variable.VAL, CrescentToken.Variable.VAR -> {
                    variables += readVariable(tokenIterator, visibility, token == CrescentToken.Variable.VAL)
                }

                CrescentToken.Type.TRAIT -> {

                    val name = (tokenIterator.next() as CrescentToken.Key).string
                    val functionTraits = mutableListOf<CrescentAST.Node.FunctionTrait>()

                    readNextUntilClosed(tokenIterator) { innerTokens ->

                        when (innerTokens) {

                            CrescentToken.Bracket.OPEN,
                            CrescentToken.Parenthesis.OPEN,
                            CrescentToken.Parenthesis.CLOSE -> {
                                /*NOOP*/
                            }

                            CrescentToken.Statement.FUN -> {
                                functionTraits += readFunctionTrait(tokenIterator)
                            }

                            else -> error("Unexpected token $innerTokens")
                        }

                    }

                    traits += CrescentAST.Node.Trait(name, functionTraits)
                }

                CrescentToken.Type.OBJECT -> {

                    val name = (tokenIterator.next() as CrescentToken.Key).string

                    val objectVariables = mutableListOf<CrescentAST.Node.Variable>()
                    val objectFunctions = mutableListOf<CrescentAST.Node.Function>()
                    val objectConstants = mutableListOf<CrescentAST.Node.Constant>()

                    var innerVisibility = CrescentToken.Visibility.PUBLIC
                    val innerModifiers = mutableListOf<CrescentToken.Modifier>()

                    readNextUntilClosed(tokenIterator) { token ->

                        when (token) {

                            CrescentToken.Bracket.OPEN, CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE -> {
                                /*NOOP*/
                            }

                            is CrescentToken.Modifier -> {
                                innerModifiers += token
                                return@readNextUntilClosed
                            }

                            CrescentToken.Variable.VAL -> {
                                objectVariables += readVariable(tokenIterator, innerVisibility, isFinal = true)
                            }

                            CrescentToken.Variable.CONST -> {
                                objectConstants += readConstant(tokenIterator, innerVisibility)
                            }

                            CrescentToken.Statement.FUN -> {
                                objectFunctions += readFunction(tokenIterator, innerVisibility, modifiers)
                            }

                            is CrescentToken.Visibility -> {
                                innerVisibility = token
                                return@readNextUntilClosed
                            }

                            else -> error("Unexpected token $token")
                        }

                        innerVisibility = CrescentToken.Visibility.PUBLIC
                        innerModifiers.clear()
                    }

                    objects += CrescentAST.Node.Object(name, objectVariables, objectFunctions, objectConstants)
                }

                CrescentToken.Type.ENUM -> {

                    val name = (tokenIterator.next() as CrescentToken.Key).string
                    val parameters = readParameters(tokenIterator)
                    val entries = mutableListOf<CrescentAST.Node.EnumEntry>()

                    readNextUntilClosed(tokenIterator) { innerToken ->

                        if (innerToken == CrescentToken.Bracket.OPEN) {
                            return@readNextUntilClosed
                        }

                        val entryName = (innerToken as CrescentToken.Key).string
                        val entryArgs = readArguments(tokenIterator)

                        entries += CrescentAST.Node.EnumEntry(entryName, entryArgs)
                    }

                    enums += CrescentAST.Node.Enum(name, parameters, entries)
                }

                CrescentToken.Type.SEALED -> {

                    val name = (tokenIterator.next() as CrescentToken.Key).string
                    val innerStructs = mutableListOf<CrescentAST.Node.Struct>()

                    readNextUntilClosed(tokenIterator) { innerToken ->

                        if (innerToken == CrescentToken.Type.STRUCT || innerToken == CrescentToken.Bracket.OPEN) {
                            return@readNextUntilClosed
                        }

                        tokenIterator.back()
                        innerStructs += readStruct(tokenIterator)
                    }

                    sealeds += CrescentAST.Node.Sealed(name, innerStructs)
                }

                CrescentToken.Statement.FUN -> {

                    val function = readFunction(tokenIterator, visibility, modifiers)

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

                        imports += CrescentAST.Node.Import(
                            path = "",
                            typeName = (tokenIterator.next() as CrescentToken.Key).string
                        )

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

                    imports += CrescentAST.Node.Import(
                        path = path,
                        typeName = (tokenIterator.next() as CrescentToken.Key).string
                    )
                }

                else -> error("Unexpected token: $token")
            }

            // Reset visibility and modifiers
            visibility = CrescentToken.Visibility.PUBLIC
            modifiers.clear()
        }

        return CrescentAST.Node.File(
            path = filePath,
            imports = imports,
            structs = structs,
            sealeds = sealeds,
            impls = impls,
            traits = traits,
            objects = objects,
            enums = enums,
            variables = variables,
            constants = constants,
            functions = functions,
            mainFunction = mainFunction,
        )
    }

    fun readStruct(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Struct {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val innerVariables = mutableListOf<CrescentAST.Node.Variable>()

        // Skip open bracket
        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)

        while (tokenIterator.hasNext()) {

            var variableVisibility = CrescentToken.Visibility.PUBLIC

            when (val nextToken = tokenIterator.next()) {

                CrescentToken.Parenthesis.CLOSE -> break

                is CrescentToken.Data.Comment -> continue

                CrescentToken.Operator.COMMA -> {
                    // NOOP
                }

                is CrescentToken.Visibility -> {
                    variableVisibility = nextToken
                    continue
                }

                is CrescentToken.Variable -> {
                    innerVariables.addAll(
                        readVariables(
                            tokenIterator,
                            variableVisibility,
                            nextToken == CrescentToken.Variable.VAL
                        )
                    )
                }

                else -> error("Unexpected token $nextToken")
            }

            // Reset visibility and modifiers
            variableVisibility = CrescentToken.Visibility.PUBLIC
        }

        return CrescentAST.Node.Struct(name, innerVariables)
    }

    fun readImpl(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Impl {

        lateinit var type: CrescentAST.Node.Type

        val functions = mutableListOf<CrescentAST.Node.Function>()
        val readModifiers = mutableListOf<CrescentToken.Modifier>()
        val implModifiers = mutableListOf<CrescentToken.Modifier>()

        var readVisibility = CrescentToken.Visibility.PUBLIC

        readNextUntilClosed(tokenIterator) { token ->

            when (token) {

                CrescentToken.Bracket.OPEN, is CrescentToken.Data.Comment, CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE -> {
                    /*NOOP*/
                }

                is CrescentToken.Key, CrescentToken.SquareBracket.OPEN -> {
                    tokenIterator.back() // UnSkip type start
                    type = readType(tokenIterator)
                    implModifiers.addAll(readModifiers)
                }

                is CrescentToken.Modifier -> {
                    readModifiers += token
                    return@readNextUntilClosed
                }

                is CrescentToken.Visibility -> {
                    readVisibility = token
                    return@readNextUntilClosed
                }

                CrescentToken.Statement.FUN -> {
                    functions += readFunction(tokenIterator, readVisibility, readModifiers)
                }

                else -> error("Unexpected token $token")
            }

            readModifiers.clear()
            readVisibility = CrescentToken.Visibility.PUBLIC
        }

        return CrescentAST.Node.Impl(type, implModifiers, functions, emptyList())
    }


    // TODO: Maybe just take a list of modifier tokens
    fun readFunction(tokenIterator: PeekingTokenIterator, visibility: CrescentToken.Visibility, modifiers: List<CrescentToken.Modifier>): CrescentAST.Node.Function {

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

        return CrescentAST.Node.Function(
            name,
            modifiers,
            visibility,
            parameters,
            type,
            expressions,
        )
    }

    fun readBlock(tokenIterator: PeekingTokenIterator) : CrescentAST.Node.Statement.Block {

        val expressions = mutableListOf<CrescentAST.Node.Expression>()

        checkEquals(tokenIterator.next(), CrescentToken.Bracket.OPEN)

        while (tokenIterator.hasNext() && tokenIterator.peekNext() != CrescentToken.Bracket.CLOSE) {

            val expression = readExpression(tokenIterator)

            if (expression.nodes.isNotEmpty()) {
                expressions += expression
            }
        }

        if (tokenIterator.hasNext()) {
            checkEquals(tokenIterator.next(), CrescentToken.Bracket.CLOSE)
        }

        return CrescentAST.Node.Statement.Block(expressions)
    }

    fun readFunctionTrait(tokenIterator: PeekingTokenIterator): CrescentAST.Node.FunctionTrait {

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

        return CrescentAST.Node.FunctionTrait(name, parameters, type)
    }

    fun readConstant(
        tokenIterator: PeekingTokenIterator,
        visibility: CrescentToken.Visibility,
    ): CrescentAST.Node.Constant {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        val type =
            if (tokenIterator.peekNext() == CrescentToken.Operator.TYPE_PREFIX) {
                tokenIterator.next() // Skip
                readType(tokenIterator)
            }
            else {
                CrescentAST.Node.Type.Implicit
            }

        val expression =
            if (tokenIterator.peekNext() == CrescentToken.Operator.ASSIGN) {
                tokenIterator.next() // Skip
                readExpression(tokenIterator)
            }
            else {
                CrescentAST.Node.Expression(emptyList())
            }


        return CrescentAST.Node.Constant(name, visibility, type, expression)
    }

    fun readVariable(
        tokenIterator: PeekingTokenIterator,
        visibility: CrescentToken.Visibility,
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

    fun readVariables(
        tokenIterator: PeekingTokenIterator,
        visibility: CrescentToken.Visibility,
        isFinal: Boolean,
    ): List<CrescentAST.Node.Variable> {

        val names = tokenIterator.nextUntil { it !is CrescentToken.Key }.map {
            (it as CrescentToken.Key).string
        }

        check(names.isNotEmpty()) {
            "Variable has no names?"
        }

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

        return names.map { name ->
            CrescentAST.Node.Variable(name, isFinal, visibility, type, expression)
        }
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

            val names = tokenIterator.nextUntil { it !is CrescentToken.Key }.map {
                (it as CrescentToken.Key).string
            }

            checkEquals(tokenIterator.next(), CrescentToken.Operator.TYPE_PREFIX)
            val type = readType(tokenIterator)

            names.forEach { name ->
                parameters += CrescentAST.Node.Parameter.Basic(name, type)
            }
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)

        return parameters
    }

    fun readArguments(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Expression> {

        if (tokenIterator.peekNext() != CrescentToken.Parenthesis.OPEN) {
            return emptyList()
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)
        val arguments = mutableListOf<CrescentAST.Node.Expression>()

        while (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {
            arguments += readExpression(tokenIterator)
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)

        return arguments
    }

    // TODO: Merge with get arguments
    fun readGetArguments(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Expression> {

        if (tokenIterator.peekNext() != CrescentToken.SquareBracket.OPEN) {
            return emptyList()
        }

        checkEquals(tokenIterator.next(), CrescentToken.SquareBracket.OPEN)
        val arguments = mutableListOf<CrescentAST.Node.Expression>()

        while (tokenIterator.peekNext() != CrescentToken.SquareBracket.CLOSE) {
            arguments += readExpression(tokenIterator)
        }

        checkEquals(tokenIterator.next(), CrescentToken.SquareBracket.CLOSE)

        return arguments
    }

    fun readWhen(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Statement.When {

        val clauses = mutableListOf<CrescentAST.Node.Statement.When.Clause>()

        val argument =
            if (tokenIterator.peekNext() == CrescentToken.Parenthesis.OPEN) {
                checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)
                readExpression(tokenIterator)
            }
            else {
                CrescentAST.Node.Expression(emptyList())
            }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)

        readNextUntilClosed(tokenIterator) {

            // Unskip the read token from readNextUntilClosed(tokenIterator)
            tokenIterator.back()

            val ifExpression = readExpression(tokenIterator)

            // Usually happens if there is a comment before last closing }
            if (ifExpression.nodes.isEmpty()) {
                return@readNextUntilClosed
            }

            val firstNode = ifExpression.nodes.first()

            // If is else statement, no ifExpression
            if (firstNode is CrescentAST.Node.Statement.Else) {
                clauses += CrescentAST.Node.Statement.When.Clause(null, firstNode.block)
                return@readNextUntilClosed
            }

            checkEquals(tokenIterator.next(), CrescentToken.Operator.RETURN)

            val thenExpressions =
                if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
                    readBlock(tokenIterator)
                }
                else {
                    CrescentAST.Node.Statement.Block(listOf(readExpression(tokenIterator)))
                }

            clauses += CrescentAST.Node.Statement.When.Clause(ifExpression, thenExpressions)
        }

        return CrescentAST.Node.Statement.When(argument, clauses)
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

        if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
            tokenIterator.next()
        }

        while (tokenIterator.hasNext()) {

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

                CrescentToken.Variable.VAL, CrescentToken.Variable.VAR -> {

                    if (nodes.isEmpty()) {
                        nodes += readVariable(tokenIterator, CrescentToken.Visibility.PUBLIC, next == CrescentToken.Variable.VAL)
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                CrescentToken.Parenthesis.OPEN -> {
                    readExpression(tokenIterator).also {
                        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)
                    }
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

                        // If used like a when statement. TODO: Add more validation that it is in a when statement
                        if (tokenIterator.peekNext() == CrescentToken.Operator.RETURN) {
                            tokenIterator.next()
                        }

                        nodes += CrescentAST.Node.Statement.Else(readBlock(tokenIterator))
                    }
                    else {
                        tokenIterator.back()
                    }

                    break
                }

                is CrescentToken.Data.Comment -> {
                    if (nodes.isEmpty()) {
                        continue
                    }
                    else {
                        break
                    }
                }

                is CrescentToken.Data -> {

                    if (nodes.isNotEmpty() && nodes.last() !is CrescentAST.Node.Operator) {
                        tokenIterator.back()
                        break
                    }

                    when (next) {

                        is CrescentToken.Data.String -> CrescentAST.Node.String(next.kotlinString)
                        is CrescentToken.Data.Char -> CrescentAST.Node.Char(next.kotlinChar)
                        is CrescentToken.Data.Boolean -> CrescentAST.Node.Boolean(next.kotlinBoolean)
                        is CrescentToken.Data.Number -> CrescentAST.Node.Number(next.number)

                        else -> continue
                    }
                }

                is CrescentToken.Operator -> {
                    CrescentAST.Node.Operator(next)
                }

                is CrescentToken.Type -> {
                    tokenIterator.back()
                    break
                }

                is CrescentToken.Key -> {

                    val identifier = next.string

                    when (tokenIterator.peekNext()) {

                        CrescentToken.Parenthesis.OPEN -> {
                            CrescentAST.Node.FunctionCall(identifier, readArguments(tokenIterator))
                        }

                        CrescentToken.SquareBracket.OPEN -> {
                            CrescentAST.Node.GetCall(identifier, readGetArguments(tokenIterator))
                        }

                        else -> {
                            CrescentAST.Node.Identifier(identifier)
                        }

                    }
                }

                CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.CLOSE, CrescentToken.SquareBracket.CLOSE -> {
                    tokenIterator.back()
                    break
                }

                else -> {
                    error("Unexpected token: $next")
                }
            }

            // If an inlined operator was the previous node
            nodes += node
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