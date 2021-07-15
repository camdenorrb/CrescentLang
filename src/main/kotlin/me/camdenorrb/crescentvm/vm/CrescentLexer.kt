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

                peekNext.equalsAny('(', ')', '{', '}', '[', ']', '\'', '"', '^') -> {
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
                "[" -> CrescentToken.ArrayDeclaration.OPEN
                "]" -> CrescentToken.ArrayDeclaration.CLOSE

                // Infix Operators
                "in" -> CrescentToken.Operator.CONTAINS
                ".." -> CrescentToken.Operator.RANGE

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
                "!" -> CrescentToken.Operator.NOT
                "+" -> CrescentToken.Operator.ADD
                "-" -> CrescentToken.Operator.SUB
                "*" -> CrescentToken.Operator.MUL
                "/" -> CrescentToken.Operator.DIV
                "%" -> CrescentToken.Operator.REM
                "^" -> CrescentToken.Operator.POW

                // Assign
                "="  -> CrescentToken.Operator.ASSIGN
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
                ":" -> CrescentToken.Operator.VARIABLE_TYPE_PREFIX

                // String
                // TODO: Add support for \"
                // TODO: Add support for ${}
                "\"" -> CrescentToken.String(charIterator.nextUntilAndSkip('"'))

                // Comment
                "#" -> CrescentToken.Comment(charIterator.nextUntil('\n').trim())

                //"\n" -> CrescentToken.Operator.NEW_LINE
                "->" -> CrescentToken.Operator.RETURN
                "?"  -> CrescentToken.Operator.RESULT
                ","  -> CrescentToken.Operator.COMMA
                
                else -> CrescentToken.Key(key)
            }
        }

        return tokens
    }

}