package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.extensions.equalsAny
import me.camdenorrb.crescentvm.iterator.PeekingCharIterator
import me.camdenorrb.crescentvm.project.checkEquals

object CrescentLexer {

    // TODO: Remake to act like how my filter works, finds all matches and eliminates as it continues to read
    fun invoke(input: String): List<CrescentToken> {

        val tokens = mutableListOf<CrescentToken>()
        val charIterator = PeekingCharIterator(input)

        while (true) {

            // Skip to next key
            charIterator.nextUntil {
                !it.isWhitespace()
            }

            if (!charIterator.hasNext()) {
                break
            }

            val peekNext = charIterator.peekNext()

            val key = when {

                peekNext.isDigit() -> {
                    charIterator.nextUntil { !it.isDigit() && it != '.' }
                }

                peekNext.isLetter() -> {
                    charIterator.nextUntil { !it.isLetterOrDigit() }
                }

                peekNext.equalsAny('+', '-', '/', '*', '=') -> {

                    val next = charIterator.next()
                    val peek = charIterator.peekNext()

                    if ((next == '-' && peek == '>') || peek == '=') {
                        "$next${charIterator.next()}"
                    }
                    else {
                        "$next"
                    }
                }

                peekNext.equalsAny('(', ')', '{', '}', '[', ']', '\'', '"', '^', '.', '#') -> {
                    charIterator.next().toString()
                }

                // Is symbol
                else -> {
                    charIterator.nextUntil { it.isLetterOrDigit() || it.isWhitespace() }
                }

            }

            val asNumber = key.toDoubleOrNull()

            if (asNumber != null) {
                tokens += CrescentToken.Number(asNumber)
                continue
            }

            // TODO: This should only tokenize symbols, the rest should be a Key token
            tokens += when (key) {

                // Parenthesis
                "(" -> CrescentToken.Parenthesis.OPEN
                ")" -> CrescentToken.Parenthesis.CLOSE

                // Bracket
                "{" -> CrescentToken.Bracket.OPEN
                "}" -> CrescentToken.Bracket.CLOSE

                // Array declaration
                "[" -> CrescentToken.SquareBracket.OPEN
                "]" -> CrescentToken.SquareBracket.CLOSE

                // Infix Operators
                "in" -> CrescentToken.Operator.CONTAINS
                ".." -> CrescentToken.Operator.RANGE
                "as" -> CrescentToken.Operator.AS

                // Variables
                "var" -> CrescentToken.Variable.VAR
                "val" -> CrescentToken.Variable.VAL
                "const" -> CrescentToken.Variable.CONST

                // Types
                "struct" -> CrescentToken.Type.STRUCT
                "impl" -> CrescentToken.Type.IMPL
                "trait" -> CrescentToken.Type.TRAIT
                "object" -> CrescentToken.Type.OBJECT
                "enum" -> CrescentToken.Type.ENUM
                "sealed" -> CrescentToken.Type.SEALED

                // Statements
                "else" -> CrescentToken.Statement.ELSE
                "import" -> CrescentToken.Statement.IMPORT
                "if" -> CrescentToken.Statement.IF
                "when" -> CrescentToken.Statement.WHEN
                "while" -> CrescentToken.Statement.WHILE
                "for" -> CrescentToken.Statement.FOR
                "fun" -> CrescentToken.Statement.FUN

                // Modifiers
                "async" -> CrescentToken.Modifier.ASYNC
                "override" -> CrescentToken.Modifier.OVERRIDE
                "operator" -> CrescentToken.Modifier.OPERATOR
                "public" -> CrescentToken.Modifier.PUBLIC
                "private" -> CrescentToken.Modifier.PRIVATE
                "inline" -> CrescentToken.Modifier.INLINE

                // Arithmetic
                "!" -> CrescentToken.Operator.NOT
                "+" -> CrescentToken.Operator.ADD
                "-" -> CrescentToken.Operator.SUB
                "*" -> CrescentToken.Operator.MUL
                "/" -> CrescentToken.Operator.DIV
                "%" -> CrescentToken.Operator.REM
                "^" -> CrescentToken.Operator.POW

                // Assign
                "=" -> CrescentToken.Operator.ASSIGN
                "+=" -> CrescentToken.Operator.ADD_ASSIGN
                "-=" -> CrescentToken.Operator.SUB_ASSIGN
                "*=" -> CrescentToken.Operator.MUL_ASSIGN
                "/=" -> CrescentToken.Operator.DIV_ASSIGN
                "%=" -> CrescentToken.Operator.REM_ASSIGN

                // Compare
                "||" -> CrescentToken.Operator.OR_COMPARE
                "&&" -> CrescentToken.Operator.AND_COMPARE
                "<=" -> CrescentToken.Operator.GREATER_EQUALS_COMPARE
                ">=" -> CrescentToken.Operator.LESSER_EQUALS_COMPARE
                "==" -> CrescentToken.Operator.EQUALS_COMPARE
                "!=" -> CrescentToken.Operator.NOT_EQUALS_COMPARE

                // Compare references
                "===" -> CrescentToken.Operator.EQUALS_REFERENCE_COMPARE
                "!==" -> CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE

                // Type prefix
                ":" -> CrescentToken.Operator.TYPE_PREFIX

                // String
                // TODO: Add support for ${} - No this will be done in the parser
                "\"" -> CrescentToken.String(charIterator.nextUntilAndSkip('"'))
                "'" -> {
                    val data = charIterator.nextUntilAndSkip('\'')
                    checkEquals(data.length, 1)
                    CrescentToken.Char(data[0])
                }

                // Comment
                "#" -> CrescentToken.Comment(charIterator.nextUntil('\n').trim())

                //"\n" -> CrescentToken.Operator.NEW_LINE
                "is" -> CrescentToken.Operator.INSTANCE_OF
                "->" -> CrescentToken.Operator.RETURN
                "?"  -> CrescentToken.Operator.RESULT
                ","  -> CrescentToken.Operator.COMMA
                "."  -> CrescentToken.Operator.DOT
                "::" -> CrescentToken.Operator.IMPORT_SEPARATOR

                "true" -> CrescentToken.Boolean(true)
                "false" -> CrescentToken.Boolean(false)

                else -> CrescentToken.Key(key)
            }
        }

        return tokens
    }

}