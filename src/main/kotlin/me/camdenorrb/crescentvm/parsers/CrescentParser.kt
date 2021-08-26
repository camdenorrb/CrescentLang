package me.camdenorrb.crescentvm.parsers

import me.camdenorrb.crescentvm.iterator.PeekingTokenIterator
import me.camdenorrb.crescentvm.math.ShuntingYard
import me.camdenorrb.crescentvm.project.checkEquals
import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentToken
import java.nio.file.Path

// TODO: Maybe support comments
// TODO: Shunting yard :C
object CrescentParser {

    fun invoke(filePath: Path, tokens: List<CrescentToken>): CrescentAST.Node.File {

        val imports = mutableListOf<CrescentAST.Node.Import>()

        val impls = mutableMapOf<String, CrescentAST.Node.Impl>()
        val enums = mutableMapOf<String, CrescentAST.Node.Enum>()
        val traits = mutableMapOf<String, CrescentAST.Node.Trait>()
        val staticImpls = mutableMapOf<String, CrescentAST.Node.Impl>()

        val sealeds = mutableMapOf<String, CrescentAST.Node.Sealed>()
        val structs = mutableMapOf<String, CrescentAST.Node.Struct>()
        val objects = mutableMapOf<String, CrescentAST.Node.Object>()

        val functions = mutableMapOf<String, CrescentAST.Node.Function>()
        val variables = mutableMapOf<String, CrescentAST.Node.Variable>()
        val constants = mutableMapOf<String, CrescentAST.Node.Constant>()

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
                    readStruct(tokenIterator).let {
                        structs[it.name] = it
                    }
                }

                CrescentToken.Type.IMPL -> {
                    readImpl(tokenIterator).let {
                        if (CrescentToken.Modifier.STATIC in it.modifiers) {
                            staticImpls["${it.type}"] = it
                        }
                        else {
                            impls["${it.type}"] = it
                        }
                    }
                }

                CrescentToken.Variable.CONST -> {
                    readConstant(tokenIterator, visibility).let {
                        constants[it.name] = it
                    }
                }

                CrescentToken.Variable.VAL, CrescentToken.Variable.VAR -> {
                    readVariables(tokenIterator, visibility, token == CrescentToken.Variable.VAL).forEach {
                        variables[it.name] = it
                    }
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

                    traits[name] = CrescentAST.Node.Trait(name, functionTraits)
                }

                CrescentToken.Type.OBJECT -> {
                    readObject(tokenIterator).let {
                        objects[it.name] = it
                    }
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

                    enums[name] = CrescentAST.Node.Enum(name, parameters, entries)
                }

                CrescentToken.Type.SEALED -> {

                    val name = (tokenIterator.next() as CrescentToken.Key).string
                    val innerStructs = mutableListOf<CrescentAST.Node.Struct>()
                    val innerObjects = mutableListOf<CrescentAST.Node.Object>()

                    readNextUntilClosed(tokenIterator) { innerToken ->

                        when (innerToken) {
                            CrescentToken.Type.STRUCT -> {
                                innerStructs += readStruct(tokenIterator)
                            }
                            CrescentToken.Type.OBJECT -> {
                                innerObjects += readObject(tokenIterator)
                            }
                            else -> {}
                        }
                    }

                    sealeds[name] = CrescentAST.Node.Sealed(name, innerStructs, innerObjects)
                }

                CrescentToken.Statement.FUN -> {

                    val function = readFunction(tokenIterator, visibility, modifiers)

                    if (function.name == "main") {

                        check(mainFunction == null) {
                            "More than one main function???"
                        }

                        mainFunction = function
                    }

                    functions[function.name] = function
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
            staticImpls = staticImpls,
            traits = traits,
            objects = objects,
            enums = enums,
            variables = variables,
            constants = constants,
            functions = functions,
            mainFunction = mainFunction,
        )
    }


    fun readObject(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Object {

        val name = (tokenIterator.next() as CrescentToken.Key).string

        val objectVariables = mutableListOf<CrescentAST.Node.Variable>()
        val objectFunctions = mutableListOf<CrescentAST.Node.Function>()
        val objectConstants = mutableListOf<CrescentAST.Node.Constant>()

        var innerVisibility = CrescentToken.Visibility.PUBLIC
        val innerModifiers = mutableListOf<CrescentToken.Modifier>()

        readNextUntilClosed(tokenIterator) { innerToken ->

            when (innerToken) {

                CrescentToken.Bracket.OPEN, CrescentToken.Parenthesis.OPEN, CrescentToken.Parenthesis.CLOSE -> {
                    /*NOOP*/
                }

                is CrescentToken.Modifier -> {
                    innerModifiers += innerToken
                    return@readNextUntilClosed
                }

                CrescentToken.Variable.VAL -> {
                    objectVariables += readVariable(tokenIterator, innerVisibility, isFinal = true)
                }

                CrescentToken.Variable.CONST -> {
                    objectConstants += readConstant(tokenIterator, innerVisibility)
                }

                CrescentToken.Statement.FUN -> {
                    objectFunctions += readFunction(tokenIterator, innerVisibility, innerModifiers)
                }

                is CrescentToken.Visibility -> {
                    innerVisibility = innerToken
                    return@readNextUntilClosed
                }

                else -> error("Unexpected token $innerToken")
            }

            innerVisibility = CrescentToken.Visibility.PUBLIC
            innerModifiers.clear()
        }

        return CrescentAST.Node.Object(name, objectVariables, objectConstants, objectFunctions)
    }

    fun readStruct(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Struct {

        val name = (tokenIterator.next() as CrescentToken.Key).string
        val innerVariables = mutableListOf<CrescentAST.Node.Variable>()

        // Skip open bracket
        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)

        var variableVisibility = CrescentToken.Visibility.PUBLIC

        while (tokenIterator.hasNext()) {

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

        return CrescentAST.Node.Impl(type, implModifiers, emptyList(), functions)
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

        val expressionNodes = mutableListOf<CrescentAST.Node>()

        checkEquals(tokenIterator.next(), CrescentToken.Bracket.OPEN)

        while (tokenIterator.hasNext() && tokenIterator.peekNext() != CrescentToken.Bracket.CLOSE) {

            val peekNext = tokenIterator.peekNext()

            if (peekNext == CrescentToken.Variable.VAL || peekNext == CrescentToken.Variable.VAR) {
                tokenIterator.next()
                readVariables(tokenIterator, CrescentToken.Visibility.PUBLIC, peekNext == CrescentToken.Variable.VAL).forEach {
                    expressionNodes += it
                }
            }
            else {

                val expressionNode = readExpressionNode(tokenIterator)

                if (expressionNode != null) {
                    expressionNodes += expressionNode
                }
            }
        }

        if (tokenIterator.hasNext()) {
            checkEquals(tokenIterator.next(), CrescentToken.Bracket.CLOSE)
        }

        return CrescentAST.Node.Statement.Block(expressionNodes)
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

        val names = tokenIterator.nextUntil { it !is CrescentToken.Key && it != CrescentToken.Operator.COMMA }.mapNotNull {
            if (it == CrescentToken.Operator.COMMA) {
                null
            }
            else {
                (it as CrescentToken.Key).string
            }
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

            if (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {
                checkEquals(tokenIterator.next(), CrescentToken.Operator.COMMA)
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

            if (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {
                checkEquals(tokenIterator.next(), CrescentToken.Operator.COMMA)
            }
        }

        checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)

        return arguments
    }

    // TODO: Merge with readArguments
    fun readGetArguments(tokenIterator: PeekingTokenIterator): List<CrescentAST.Node.Expression> {

        if (tokenIterator.peekNext() != CrescentToken.SquareBracket.OPEN) {
            return emptyList()
        }

        checkEquals(tokenIterator.next(), CrescentToken.SquareBracket.OPEN)
        val arguments = mutableListOf<CrescentAST.Node.Expression>()

        while (tokenIterator.peekNext() != CrescentToken.SquareBracket.CLOSE) {

            arguments += readExpression(tokenIterator)

            if (tokenIterator.peekNext() != CrescentToken.Parenthesis.CLOSE) {
                checkEquals(tokenIterator.next(), CrescentToken.Operator.COMMA)
            }
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
        checkEquals(tokenIterator.next(), CrescentToken.Bracket.OPEN)

        readNextUntilClosed(tokenIterator) {

            // Unskip the read token from readNextUntilClosed(tokenIterator)
            tokenIterator.back()

            val ifExpressionNode = when (tokenIterator.peekNext()) {

                CrescentToken.Operator.DOT -> {

                    checkEquals(tokenIterator.next(), CrescentToken.Operator.DOT)
                    val identifier = readExpressionNode(tokenIterator) as CrescentAST.Node.Identifier
                    //checkEquals(tokenIterator.next(), CrescentToken.Operator.RETURN)

                    CrescentAST.Node.Statement.When.EnumShortHand(identifier.name)
                }

                CrescentToken.Statement.ELSE -> {

                    checkEquals(tokenIterator.next(), CrescentToken.Statement.ELSE)
                    checkEquals(tokenIterator.next(), CrescentToken.Operator.RETURN)

                    CrescentAST.Node.Statement.When.Else(readBlock(tokenIterator))
                }

                else -> {
                    readExpressionNode(tokenIterator) ?: return@readNextUntilClosed
                }

            }


            // Usually happens if there is a comment before last closing }

            //println(ifExpressionNode)

            // If is else statement, no ifExpression
            if (ifExpressionNode is CrescentAST.Node.Statement.When.Else) {
                clauses += CrescentAST.Node.Statement.When.Clause(null, ifExpressionNode.thenBlock)
                return@readNextUntilClosed
            }

            checkEquals(tokenIterator.next(), CrescentToken.Operator.RETURN)

            val thenExpressions =
                if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
                    readBlock(tokenIterator)
                }
                else {
                    CrescentAST.Node.Statement.Block(listOfNotNull(readExpression(tokenIterator)))
                }

            clauses += CrescentAST.Node.Statement.When.Clause(ifExpressionNode, thenExpressions)
        }

        return CrescentAST.Node.Statement.When(argument, clauses)
    }

    fun readType(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Type {

        var type: CrescentAST.Node.Type

        when (val peekNext = tokenIterator.peekNext()) {

            CrescentToken.SquareBracket.OPEN -> {

                tokenIterator.next()
                type =
                    CrescentAST.Node.Type.Array(CrescentAST.Node.Type.Basic((tokenIterator.next() as CrescentToken.Key).string))

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

    // Returns null if the node should be skipped in the expression
    // TODO: Find a way to remove recursion?
    fun readExpressionNode(tokenIterator: PeekingTokenIterator, isInDotChain: Boolean = false): CrescentAST.Node? {

        return when (val next = tokenIterator.next()) {

            is CrescentToken.Data.Comment -> {
                null
            }

            CrescentToken.Operator.RETURN -> {
                CrescentAST.Node.Return(readExpression(tokenIterator))
            }

            CrescentToken.Operator.INSTANCE_OF -> {
                CrescentAST.Node.InstanceOf(readExpression(tokenIterator))
            }

            is CrescentToken.Operator -> {
                CrescentAST.Node.Operator(next)
            }

            CrescentToken.Statement.WHEN -> {
                readWhen(tokenIterator)
            }

            CrescentToken.Parenthesis.OPEN -> {
                readExpression(tokenIterator).also {
                    checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)
                }
            }

            CrescentToken.Statement.IF -> {

                checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.OPEN)

                val argument = readExpression(tokenIterator).also {
                    checkEquals(tokenIterator.next(), CrescentToken.Parenthesis.CLOSE)
                }

                val ifBlock = readBlock(tokenIterator)

                val elseBlock =
                    if (tokenIterator.peekNext() == CrescentToken.Statement.ELSE) {
                        tokenIterator.next()
                        readBlock(tokenIterator)
                    }
                    else {
                        null
                    }

                CrescentAST.Node.Statement.If(argument, ifBlock, elseBlock)
            }

            is CrescentToken.Data -> {
                when (next) {

                    is CrescentToken.Data.String -> CrescentAST.Node.Primitive.String(next.kotlinString)
                    is CrescentToken.Data.Char -> CrescentAST.Node.Primitive.Char(next.kotlinChar)
                    is CrescentToken.Data.Boolean -> CrescentAST.Node.Primitive.Boolean(next.kotlinBoolean)
                    is CrescentToken.Data.Number -> CrescentAST.Node.Primitive.Number(next.number)

                    else -> error("Unknown data token: $next")
                }
            }

            is CrescentToken.Key -> {

                val identifier = next.string

                val keyNode = when (tokenIterator.peekNext()) {

                    CrescentToken.Parenthesis.OPEN -> {
                        CrescentAST.Node.IdentifierCall(identifier, readArguments(tokenIterator))
                    }

                    CrescentToken.SquareBracket.OPEN -> {
                        CrescentAST.Node.GetCall(identifier, readGetArguments(tokenIterator))
                    }

                    else -> {
                        CrescentAST.Node.Identifier(identifier)
                    }

                }

                if (tokenIterator.peekNext() == CrescentToken.Operator.DOT && !isInDotChain) {

                    val nodes = mutableListOf(keyNode)

                    while (tokenIterator.peekNext() == CrescentToken.Operator.DOT) {
                        tokenIterator.next()
                        nodes += readExpressionNode(tokenIterator, true)!!
                    }

                    return CrescentAST.Node.DotChain(nodes)
                }

                return keyNode
            }

            else -> {
                error("Unexpected token: $next")
            }

        }

    }

    // TODO: Separate readExpression and readNode, so that not every node is in an expression
    fun readExpression(tokenIterator: PeekingTokenIterator): CrescentAST.Node.Expression {

        val nodes = mutableListOf<CrescentAST.Node>()

        if (tokenIterator.peekNext() == CrescentToken.Bracket.OPEN) {
            tokenIterator.next()
        }

        while (tokenIterator.hasNext()) {
            nodes += when (val peekNext = tokenIterator.peekNext()) {

                CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.CLOSE, CrescentToken.SquareBracket.CLOSE -> {
                    break
                }

                CrescentToken.Operator.COMMA -> {
                    break
                }

                else -> {

                    if (nodes.isNotEmpty()) {
                        if (peekNext is CrescentToken.Operator) {
                            when (peekNext) {
                                CrescentToken.Operator.RETURN -> break
                                else -> {}
                            }
                        }
                        else if (nodes.lastOrNull() !is CrescentAST.Node.Operator) {
                            break
                        }
                    }

                    readExpressionNode(tokenIterator) ?: continue
                }
            }
        }

        return if (nodes.any { it is CrescentAST.Node.Operator }) {
            CrescentAST.Node.Expression(ShuntingYard.invoke(nodes))
        }
        else {
            CrescentAST.Node.Expression(nodes)
        }
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