package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.extensions.equalsAny
import me.camdenorrb.crescentvm.iterator.PeekingCharIterator

object CrescentLexer {

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
                    charIterator.nextUntil { !it.isLetter() }
                }

                peekNext.equalsAny('(', ')', '{', '}', '[', ']') -> {
                    charIterator.next().toString()
                }

                // TODO: Add char support ''
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
                "[" -> CrescentToken.ArrayDeclaration.OPEN
                "]" -> CrescentToken.ArrayDeclaration.CLOSE

                // Infix Operators
                "in" -> CrescentToken.InfixOperator.CONTAINS
                ".." -> CrescentToken.InfixOperator.RANGE

                // Variables
                "var" -> CrescentToken.Variable.VAR
                "val" -> CrescentToken.Variable.VAL

                // Types
                "struct" -> CrescentToken.Type.STRUCT
                "impl"   -> CrescentToken.Type.IMPL
                "trait"  -> CrescentToken.Type.TRAIT
                "object" -> CrescentToken.Type.OBJECT
                "enum"   -> CrescentToken.Type.ENUM

                // Statements
                "import"   -> CrescentToken.Statement.IMPORT
                "if"       -> CrescentToken.Statement.IF
                "when"     -> CrescentToken.Statement.WHEN
                "while"    -> CrescentToken.Statement.WHILE
                "for"      -> CrescentToken.Statement.FOR
                "fun"      -> CrescentToken.Statement.FUN

                // Modifiers
                "async"    -> CrescentToken.Modifier.ASYNC
                "override" -> CrescentToken.Modifier.OVERRIDE
                "operator" -> CrescentToken.Modifier.OPERATOR
                "public"   -> CrescentToken.Modifier.PUBLIC
                "private"  -> CrescentToken.Modifier.PRIVATE
                "inline"   -> CrescentToken.Modifier.INLINE

                // Arithmetic
                "!" -> CrescentToken.InfixOperator.NOT
                "+" -> CrescentToken.InfixOperator.ADD
                "-" -> CrescentToken.InfixOperator.SUB
                "*" -> CrescentToken.InfixOperator.MUL
                "/" -> CrescentToken.InfixOperator.DIV
                "%" -> CrescentToken.InfixOperator.REM

                // Assign
                "="  -> CrescentToken.InfixOperator.ASSIGN
                "+=" -> CrescentToken.InfixOperator.ADD_ASSIGN
                "-=" -> CrescentToken.InfixOperator.SUB_ASSIGN
                "*=" -> CrescentToken.InfixOperator.MUL_ASSIGN
                "/=" -> CrescentToken.InfixOperator.DIV_ASSIGN
                "%=" -> CrescentToken.InfixOperator.REM_ASSIGN

                // Compare
                "||" -> CrescentToken.InfixOperator.OR_COMPARE
                "&&" -> CrescentToken.InfixOperator.AND_COMPARE
                "<=" -> CrescentToken.InfixOperator.GREATER_EQUALS_COMPARE
                ">=" -> CrescentToken.InfixOperator.LESSER_EQUALS_COMPARE
                "==" -> CrescentToken.InfixOperator.EQUALS_COMPARE
                "!=" -> CrescentToken.InfixOperator.NOT_EQUALS_COMPARE

                // Compare references
                "===" -> CrescentToken.InfixOperator.EQUALS_REFERENCE_COMPARE
                "!==" -> CrescentToken.InfixOperator.NOT_EQUALS_REFERENCE_COMPARE

                // Type prefix
                ":" -> CrescentToken.InfixOperator.VARIABLE_TYPE_PREFIX

                // String
                // TODO: Add support for \"
                // TODO: Add support for ${}
                "\"" -> CrescentToken.String(charIterator.nextUntilAndSkip('"'))

                // Comment
                "#" -> CrescentToken.Comment(charIterator.nextUntil('\n').trim())

                //"\n" -> CrescentToken.Operator.NEW_LINE
                "->" -> CrescentToken.InfixOperator.RETURN
                "?"  -> CrescentToken.InfixOperator.RESULT
                ","  -> CrescentToken.InfixOperator.COMMA
                
                else -> CrescentToken.Key(key)
            }
        }

        return tokens
    }

}