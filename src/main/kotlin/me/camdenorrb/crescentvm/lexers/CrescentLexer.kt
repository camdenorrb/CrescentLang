package me.camdenorrb.crescentvm.lexers

import me.camdenorrb.crescentvm.iterator.PeekingCharIterator
import me.camdenorrb.crescentvm.project.checkEquals
import me.camdenorrb.crescentvm.vm.CrescentToken

// TODO: Support negative numbers
object CrescentLexer {

    // TODO: Remake to act like how my filter works, finds all matches and eliminates as it continues to read
    fun invoke(input: String): List<CrescentToken> {

        val tokens = mutableListOf<CrescentToken>()
        val charIterator = PeekingCharIterator(input)

        while (charIterator.hasNext()) {

            // MicroOptimization to avoid toDoubleOrNull
            var isANumber = false

            // Skip to next key
            charIterator.nextUntil {
                !it.isWhitespace()
            }

            if (!charIterator.hasNext()) {
                break
            }

            val key = when(val peekNext = charIterator.peekNext()) {

                '!', '+', '-', '/', '%', '^', '*', '=', '<', '>' -> {

                    val next = charIterator.next()
                    val peek = charIterator.peekNext()

                    if (peek == '=' || next == '-' && peek == '>') {
                        "$next${charIterator.next()}"
                    }
                    else {
                        "$next"
                    }
                }

                // Only take in one of these at a time
                '(', ')', '{', '}', '[', ']', '\'', '"', '#' -> {
                    charIterator.next().toString()
                }

                // Is symbol
                else -> {
                    when {

                        peekNext.isDigit() -> {

                            isANumber = true

                            // Select number, stop if rangeTo (..) is found
                            charIterator.nextUntil {
                                if (it == '.' && charIterator.peekNext(1) != '.') {
                                    false
                                }
                                else {
                                    !it.isDigit()
                                }
                            }
                        }

                        peekNext.isLetter() -> {
                            charIterator.nextUntil { !it.isLetterOrDigit() }
                        }

                        else -> {
                            charIterator.nextUntil { it.isLetterOrDigit() || it.isWhitespace() }
                        }

                    }
                }

            }

            if (isANumber) {

                // TODO: Determine type of number

                tokens +=
                    if ('.' in key) {
                        CrescentToken.Data.Number(key.toDouble())
                    }
                    else {
                        CrescentToken.Data.Number(key.toInt())
                    }

                continue
            }

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
                "inline" -> CrescentToken.Modifier.INLINE
                "static" -> CrescentToken.Modifier.STATIC

                // Visibility
                "public" -> CrescentToken.Visibility.PUBLIC
                "internal" -> CrescentToken.Visibility.INTERNAL
                "private" -> CrescentToken.Visibility.PRIVATE

                // Arithmetic
                "!" -> CrescentToken.Operator.NOT
                "+" -> CrescentToken.Operator.ADD
                "-" -> CrescentToken.Operator.SUB
                "*" -> CrescentToken.Operator.MUL
                "/" -> CrescentToken.Operator.DIV
                "%" -> CrescentToken.Operator.REM
                "^" -> CrescentToken.Operator.POW

                // Bit
                "shr" -> CrescentToken.Operator.BIT_SHIFT_RIGHT
                "shl" -> CrescentToken.Operator.BIT_SHIFT_LEFT
                "ushr" -> CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT
                "and" -> CrescentToken.Operator.BIT_AND
                "or" -> CrescentToken.Operator.BIT_OR
                "xor" -> CrescentToken.Operator.BIT_XOR

                // Assign
                "=" -> CrescentToken.Operator.ASSIGN
                "+=" -> CrescentToken.Operator.ADD_ASSIGN
                "-=" -> CrescentToken.Operator.SUB_ASSIGN
                "*=" -> CrescentToken.Operator.MUL_ASSIGN
                "/=" -> CrescentToken.Operator.DIV_ASSIGN
                "%=" -> CrescentToken.Operator.REM_ASSIGN
                "^=" -> CrescentToken.Operator.POW_ASSIGN

                // Compare
                "||" -> CrescentToken.Operator.OR_COMPARE
                "&&" -> CrescentToken.Operator.AND_COMPARE
                "<"  -> CrescentToken.Operator.LESSER_COMPARE
                ">"  -> CrescentToken.Operator.GREATER_COMPARE
                "<=" -> CrescentToken.Operator.LESSER_EQUALS_COMPARE
                ">=" -> CrescentToken.Operator.GREATER_EQUALS_COMPARE
                "==" -> CrescentToken.Operator.EQUALS_COMPARE
                "!=" -> CrescentToken.Operator.NOT_EQUALS_COMPARE

                // Compare references
                "===" -> CrescentToken.Operator.EQUALS_REFERENCE_COMPARE
                "!==" -> CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE

                // Type prefix
                ":" -> CrescentToken.Operator.TYPE_PREFIX

                // String
                // TODO: Add support for ${} - No this will be done in the parser
                "\"" -> CrescentToken.Data.String(charIterator.nextUntilAndSkip('"'))
                "'" -> {
                    val data = charIterator.nextUntilAndSkip('\'')
                    checkEquals(data.length, 1)
                    CrescentToken.Data.Char(data[0])
                }

                // Comment
                "#" -> CrescentToken.Data.Comment(charIterator.nextUntil('\n').trim())

                //"\n" -> CrescentToken.Operator.NEW_LINE
                "is" -> CrescentToken.Operator.INSTANCE_OF
                "->" -> CrescentToken.Operator.RETURN
                "?"  -> CrescentToken.Operator.RESULT
                ","  -> CrescentToken.Operator.COMMA
                "."  -> CrescentToken.Operator.DOT
                "::" -> CrescentToken.Operator.IMPORT_SEPARATOR

                "true" -> CrescentToken.Data.Boolean(true)
                "false" -> CrescentToken.Data.Boolean(false)

                else -> CrescentToken.Key(key)
            }
        }

        return tokens
    }

}