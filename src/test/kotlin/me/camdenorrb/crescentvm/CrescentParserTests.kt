package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentAST
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
                Expression(listOf(
                    FunctionCall("println", listOf(Argument(Expression(listOf(String("Hello World"))))))
                ))
            ),
            mainFunction.innerCode.expressions,
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

        assertContentEquals(
            listOf(
                Expression(listOf(
                    Statement.If(
                        Expression(listOf(
                            Operation(CrescentToken.Operator.EQUALS_COMPARE, ArrayCall("args", 0), String("true"))
                        )),
                        Statement.Block(listOf(
                            Expression(listOf(FunctionCall("println", listOf(Argument(Expression(listOf(String("Meow"))))))))
                        )),
                    ),
                )),
                Expression(listOf(
                    Statement.Else(
                        Statement.Block(listOf(
                            Expression(listOf(FunctionCall("println", listOf(Argument(Expression(listOf(String("Hiss"))))))))
                        ))
                    )
                ))
            ),
            mainFunction.innerCode.expressions,
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

        val mainFunction = assertNotNull(
            CrescentParser.invoke(File("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            emptyList(),
            mainFunction.params
        )

        assertContentEquals(
            listOf(
                Expression(listOf(
                    Variable("input", true, CrescentAST.Visibility.LOCAL_SCOPE, Type.Implicit, Expression(listOf(
                        FunctionCall("readBoolean", listOf(Argument(Expression(listOf(String("Enter a boolean value [true/false]"))))))
                    )))
                )),
                Expression(listOf(
                    Statement.If(
                        Expression(listOf(
                            VariableCall("input")
                        )),
                        Statement.Block(listOf(
                            Expression(listOf(FunctionCall("println", listOf(Argument(Expression(listOf(String("Meow"))))))))
                        )),
                    ),
                )),
                Expression(listOf(
                    Statement.Else(
                        Statement.Block(listOf(
                            Expression(listOf(FunctionCall("println", listOf(Argument(Expression(listOf(String("Hiss"))))))))
                        ))
                    )
                ))
            ),
            mainFunction.innerCode.expressions,
        )
    }


    @Test
    fun calculator() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {
            
                val input1 = readDouble("Enter your first number")
                val input2 = readDouble("Enter your second number")
                val operation = readLine("Enter an operation [+, -, *, /]")
            
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

        val mainFunction = assertNotNull(
            CrescentParser.invoke(File("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            emptyList(),
            mainFunction.params
        )

        mainFunction.innerCode.expressions.forEach {
            println(it)
        }


        assertContentEquals(
            listOf(
                Expression(listOf(
                    Variable("input1", true, CrescentAST.Visibility.LOCAL_SCOPE, Type.Implicit, Expression(listOf(
                        FunctionCall("readDouble", listOf(Argument(Expression(listOf(String("Enter your first number"))))))
                    )))
                )),
                Expression(listOf(
                    Variable("input2", true, CrescentAST.Visibility.LOCAL_SCOPE, Type.Implicit, Expression(listOf(
                        FunctionCall("readDouble", listOf(Argument(Expression(listOf(String("Enter your second number"))))))
                    )))
                )),
                Expression(listOf(
                    Variable("operation", true, CrescentAST.Visibility.LOCAL_SCOPE, Type.Implicit, Expression(listOf(
                        FunctionCall("readLine", listOf(Argument(Expression(listOf(String("Enter an operation [+, -, *, /]"))))))
                    )))
                )),
                Variable("result", true, CrescentAST.Visibility.LOCAL_SCOPE, Type.Implicit,
                    Expression(listOf(
                        Statement.When(
                            Argument(Expression(listOf(VariableCall("operation")))),
                            listOf(
                                Statement.When.Clause(
                                    Expression(listOf(Char('+'))),
                                    Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(CrescentToken.Operator.ADD, VariableCall("input1"), VariableCall("input2")))))))))
                                ),
                                Statement.When.Clause(
                                    Expression(listOf(Char('-'))),
                                    Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(CrescentToken.Operator.SUB, VariableCall("input1"), VariableCall("input2")))))))))
                                ),
                                Statement.When.Clause(
                                    Expression(listOf(Char('*'))),
                                    Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(CrescentToken.Operator.MUL, VariableCall("input1"), VariableCall("input2")))))))))
                                ),
                                Statement.When.Clause(
                                    Expression(listOf(Char('/'))),
                                    Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(CrescentToken.Operator.DIV, VariableCall("input1"), VariableCall("input2")))))))))
                                )
                            )
                        ),
                    ))
                ),
                Expression(listOf(
                    FunctionCall("println", listOf(Argument(Expression(listOf(VariableCall("result"))))))
                ))
            ),
            mainFunction.innerCode.expressions,
        )
        //println(tokens)
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