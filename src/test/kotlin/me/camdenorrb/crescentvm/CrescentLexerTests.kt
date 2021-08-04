package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentToken
import org.junit.Test
import kotlin.test.assertContentEquals

class CrescentLexerTests {

    @Test
    fun helloWorld() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {
                println("Hello World")
            }
            """.trimIndent()
        )

        assertContentEquals(tokens,
            listOf(
                CrescentToken.Statement.FUN, CrescentToken.Key("main"), CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Hello World"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE
            )
        )
    }

    @Test
    fun ifStatement() {

        val tokens = CrescentLexer.invoke(
            """
            fun main(args: [String]) {
                if (args[0] == "true") {
                    println("Meow")
                }
                else {
                    println("Hiss")
                }
            }
            """.trimIndent()
        )

        assertContentEquals(tokens,
            listOf(
                CrescentToken.Statement.FUN, CrescentToken.Key("main"), CrescentToken.Parenthesis.OPEN, CrescentToken.Key("args"), CrescentToken.Operator.TYPE_PREFIX, CrescentToken.SquareBracket.OPEN, CrescentToken.Key("String"), CrescentToken.SquareBracket.CLOSE, CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Statement.IF, CrescentToken.Parenthesis.OPEN, CrescentToken.Key("args"), CrescentToken.SquareBracket.OPEN, CrescentToken.Number(0.0), CrescentToken.SquareBracket.CLOSE, CrescentToken.Operator.EQUALS_COMPARE, CrescentToken.String("true"), CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Meow"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Statement.ELSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Hiss"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Bracket.CLOSE
            )
        )
    }

    @Test
    fun ifInputStatement() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {

                val input = readBoolean("Enter a boolean value [true/false]")

                if (input) {
                    println("Meow")
                }
                else {
                    println("Hiss")
                }
            }
            """.trimIndent()
        )

        assertContentEquals(tokens,
            listOf(
                CrescentToken.Statement.FUN, CrescentToken.Key("main"), CrescentToken.Bracket.OPEN,
                CrescentToken.Variable.VAL, CrescentToken.Key("input"), CrescentToken.Operator.ASSIGN, CrescentToken.Key("readBoolean"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Enter a boolean value [true/false]"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Statement.IF, CrescentToken.Parenthesis.OPEN, CrescentToken.Key("input"), CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Meow"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Statement.ELSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Hiss"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Bracket.CLOSE
            )
        )
    }

    @Test
    fun whenStatement() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {

                val input = readBoolean("Enter a boolean value [true/false]")

                if (input) {
                    println("Meow")
                }
                else {
                    println("Hiss")
                }
            }
            """.trimIndent()
        )

        assertContentEquals(tokens,
            listOf(
                CrescentToken.Statement.FUN, CrescentToken.Key("main"), CrescentToken.Bracket.OPEN,
                CrescentToken.Variable.VAL, CrescentToken.Key("input"), CrescentToken.Operator.ASSIGN, CrescentToken.Key("readBoolean"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Enter a boolean value [true/false]"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Statement.IF, CrescentToken.Parenthesis.OPEN, CrescentToken.Key("input"), CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Meow"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Statement.ELSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Hiss"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Bracket.CLOSE
            )
        )
    }


    @Test
    fun calculator() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {
            
                val input1 = readDouble("Enter your first number")
                val input2 = readDouble("Enter your second number")
                val operation = readLine("Enter a operation [+, -, *, /]")
            
                val result = when(operation) {
                    '+' -> input1 + input2
                    '-' -> input1 - input2
                    '*' -> input1 * input2
                    '/' -> input1 / input2
                }
            
                println(result)
            }
            """.trimIndent()
        )

        assertContentEquals(tokens,
            listOf(
                CrescentToken.Statement.FUN, CrescentToken.Key("main"), CrescentToken.Bracket.OPEN,
                CrescentToken.Variable.VAL, CrescentToken.Key("input1"), CrescentToken.Operator.ASSIGN, CrescentToken.Key("readDouble"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Enter your first number"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Variable.VAL, CrescentToken.Key("input2"), CrescentToken.Operator.ASSIGN, CrescentToken.Key("readDouble"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Enter your second number"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Variable.VAL, CrescentToken.Key("operation"), CrescentToken.Operator.ASSIGN, CrescentToken.Key("readLine"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Enter a operation [+, -, *, /]"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Variable.VAL, CrescentToken.Key("result"), CrescentToken.Operator.ASSIGN, CrescentToken.Statement.WHEN, CrescentToken.Parenthesis.OPEN, CrescentToken.Key("operation"), CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Char('+'), CrescentToken.Operator.RETURN, CrescentToken.Key("input1"), CrescentToken.Operator.ADD, CrescentToken.Key("input2"),
                CrescentToken.Char('-'), CrescentToken.Operator.RETURN, CrescentToken.Key("input1"), CrescentToken.Operator.SUB, CrescentToken.Key("input2"),
                CrescentToken.Char('*'), CrescentToken.Operator.RETURN, CrescentToken.Key("input1"), CrescentToken.Operator.MUL, CrescentToken.Key("input2"),
                CrescentToken.Char('/'), CrescentToken.Operator.RETURN, CrescentToken.Key("input1"), CrescentToken.Operator.DIV, CrescentToken.Key("input2"),
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.Key("result"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE
            )
        )
    }

}