package me.camdenorrb.crescentvm.iterator

import me.camdenorrb.crescentvm.vm.CrescentToken

object CrescentLexer {

    operator fun invoke(input: String): List<CrescentToken> {

        val tokens = mutableListOf<CrescentToken>()
        val charIterator = PeekingCharIterator(input)

        while (charIterator.hasNext()) {

            // Skip to next key
            charIterator.nextUntil {
                !it.isWhitespace()
            }
            
            val first = charIterator.peekNext()

            when {

                first.isDigit() -> {
                    charIterator.nextUntil { it.isDigit() || it == '.' }
                }

                first.isLetter() -> {
                    charIterator.nextUntil { !it.isLetter() }
                }

                first == '(', first == ')'
                // Is symbol
                else -> {
                }

            }


            val key = charIterator.nextUntil { !it.isLetterOrDigit() }

            tokens += when (key) {

                // Parenthesis
                "(" -> CrescentToken.Parenthesis.OPEN
                ")" -> CrescentToken.Parenthesis.CLOSE

                // Bracket
                "{" -> CrescentToken.Bracket.OPEN
                "}" -> CrescentToken.Bracket.CLOSE

                // Infix Operators
                "in" -> CrescentToken.Operator.CONTAINS
                ".." -> CrescentToken.Operator.RANGE

                // Variables
                "var" -> CrescentToken.Variable.VAR
                "val" -> CrescentToken.Variable.VAL

                // Types
                "struct" -> CrescentToken.Type.STRUCT
                "trait"  -> CrescentToken.Type.TRAIT
                "object" -> CrescentToken.Type.OBJECT

                // Statements
                "import" -> CrescentToken.Statement.IMPORT
                "if"     -> CrescentToken.Statement.IF
                "when"   -> CrescentToken.Statement.WHEN
                "while"  -> CrescentToken.Statement.WHILE
                "for"    -> CrescentToken.Statement.FOR
                "fun"    -> CrescentToken.Statement.FUN

                // Arithmetic
                "!" -> CrescentToken.Operator.NOT
                "+" -> CrescentToken.Operator.ADD
                "-" -> CrescentToken.Operator.SUB
                "*" -> CrescentToken.Operator.MUL
                "/" -> CrescentToken.Operator.DIV
                "%" -> CrescentToken.Operator.REM

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

                else -> CrescentToken.Name(key)
            }
        }


        return tokens
    }


    private fun Char.equalsAny(vararg chars: Char): Boolean {
        return chars.any { this == it }
    }

}