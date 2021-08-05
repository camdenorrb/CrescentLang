package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentAST.Node
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.*
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.String
import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentToken
import org.junit.Test
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

internal class CrescentParserTests {

    @Test
    fun helloWorld() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {
                println("Hello World")
            }
            """.trimIndent()
        )


        val mainFunction = assertNotNull(
            CrescentParser.invoke(File("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                FunctionCall("println", listOf(Argument(Expression(listOf(String("Hello World"))))))
            ),
            mainFunction.innerCode.nodes
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

        val mainFunction = assertNotNull(
            CrescentParser.invoke(File("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(Parameter.Basic("args", Type.Array(Type.Basic("String")))),
            mainFunction.params
        )

        println(mainFunction.innerCode.nodes)

        /*
        assertContentEquals(
            listOf(
                Statement.If(
                    Expression(listOf(Node.)),
                    listOf(
                        Argument(
                            Node.Expression(
                            listOf(String("Hello World"))
                        ))
                    )
                )
            ),
            mainFunction.innerCode.nodes
        )*/
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

        assertContentEquals(
            listOf(
                CrescentToken.Statement.FUN, CrescentToken.Key("main"), CrescentToken.Bracket.OPEN,
                CrescentToken.Variable.VAL, CrescentToken.Key("input"), CrescentToken.Operator.ASSIGN, CrescentToken.Key("readBoolean"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Enter a boolean value [true/false]"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Statement.IF, CrescentToken.Parenthesis.OPEN, CrescentToken.Key("input"), CrescentToken.Parenthesis.CLOSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Meow"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Statement.ELSE, CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN, CrescentToken.String("Hiss"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
                CrescentToken.Bracket.CLOSE,
            ),
            tokens
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

        assertContentEquals(
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
                CrescentToken.Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun constantsAndObject() {

        val tokens = CrescentLexer.invoke(
            """
            const thing = "Meow"
            
            object Constants {
            
                const thing = "Meow"
            
            }
            """.trimIndent()
        )

        assertContentEquals(
            listOf(
                CrescentToken.Modifier.CONST, CrescentToken.Key("thing"), CrescentToken.Operator.ASSIGN, CrescentToken.String("Meow"),
                CrescentToken.Type.OBJECT, CrescentToken.Key("Constants"), CrescentToken.Bracket.OPEN,
                CrescentToken.Modifier.CONST, CrescentToken.Key("thing"), CrescentToken.Operator.ASSIGN, CrescentToken.String("Meow"),
                CrescentToken.Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun math() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {
                println((1 + 1) + 1 / 10 + 1000 * 10 / 10 ^ 10)
            }
            """.trimIndent()
        )

        assertContentEquals(
            listOf(
                CrescentToken.Statement.FUN, CrescentToken.Key("main"), CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Parenthesis.OPEN,
                CrescentToken.Parenthesis.OPEN, CrescentToken.Number(1.0), CrescentToken.Operator.ADD, CrescentToken.Number(1.0), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Operator.ADD, CrescentToken.Number(1.0), CrescentToken.Operator.DIV, CrescentToken.Number(10.0), CrescentToken.Operator.ADD, CrescentToken.Number(1000.0), CrescentToken.Operator.MUL, CrescentToken.Number(10.0),
                CrescentToken.Operator.DIV, CrescentToken.Number(10.0), CrescentToken.Operator.POW, CrescentToken.Number(10.0),
                CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun sealed() {

        val tokens = CrescentLexer.invoke(
            """
            sealed Example {
                struct Thing1(val name: String)
                struct Thing2(val id: i32)
            }
            """.trimIndent()
        )

        assertContentEquals(
            listOf(
                CrescentToken.Type.SEALED, CrescentToken.Key("Example"), CrescentToken.Bracket.OPEN,
                CrescentToken.Type.STRUCT, CrescentToken.Key("Thing1"), CrescentToken.Parenthesis.OPEN, CrescentToken.Variable.VAL, CrescentToken.Key("name"), CrescentToken.Operator.TYPE_PREFIX, CrescentToken.Key("String"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Type.STRUCT, CrescentToken.Key("Thing2"), CrescentToken.Parenthesis.OPEN, CrescentToken.Variable.VAL, CrescentToken.Key("id"), CrescentToken.Operator.TYPE_PREFIX, CrescentToken.Key("i32"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun comments() {

        val tokens = CrescentLexer.invoke(
            """
            # Project level comment
            fun main {
                println#("Meow")
                #Meow
                # Meow
                "#meow"
                1 +#Meow
                1 -#Meow
                1 /#Meow
                1 *#Meow
                1 =#Meow
            #}
            """.trimIndent()
        )

        println(tokens)

        assertContentEquals(
            listOf(
                CrescentToken.Comment("Project level comment"),
                CrescentToken.Statement.FUN, CrescentToken.Key("main"), CrescentToken.Bracket.OPEN,
                CrescentToken.Key("println"), CrescentToken.Comment("(\"Meow\")"),
                CrescentToken.Comment("Meow"),
                CrescentToken.Comment("Meow"),
                CrescentToken.String("#meow"),
                CrescentToken.Number(1.0), CrescentToken.Operator.ADD, CrescentToken.Comment("Meow"),
                CrescentToken.Number(1.0), CrescentToken.Operator.SUB, CrescentToken.Comment("Meow"),
                CrescentToken.Number(1.0), CrescentToken.Operator.DIV, CrescentToken.Comment("Meow"),
                CrescentToken.Number(1.0), CrescentToken.Operator.MUL, CrescentToken.Comment("Meow"),
                CrescentToken.Number(1.0), CrescentToken.Operator.ASSIGN, CrescentToken.Comment("Meow"),
                CrescentToken.Comment("}")
            ),
            tokens
        )
    }

    @Test
    fun imports() {

        val tokens = CrescentLexer.invoke(
            """
            # Current idea, Package -> Type
            import crescent.examples::Thing

            import crescent.examples as examples
            
            # Short hand method (If in same package)
            import ::Thing
            """.trimIndent()
        )

        assertContentEquals(
            listOf(
                CrescentToken.Comment("Current idea, Package -> Type"),
                CrescentToken.Statement.IMPORT, CrescentToken.Key("crescent"), CrescentToken.Operator.DOT, CrescentToken.Key("examples"), CrescentToken.Operator.IMPORT_SEPARATOR, CrescentToken.Key("Thing"),
                CrescentToken.Statement.IMPORT, CrescentToken.Key("crescent"), CrescentToken.Operator.DOT, CrescentToken.Key("examples"), CrescentToken.Operator.AS, CrescentToken.Key("examples"),
                CrescentToken.Comment("Short hand method (If in same package)"),
                CrescentToken.Statement.IMPORT, CrescentToken.Operator.IMPORT_SEPARATOR, CrescentToken.Key("Thing")
            ),
            tokens
        )
    }

}