package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.*
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.String
import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentToken
import me.camdenorrb.crescentvm.vm.CrescentToken.Operator.*
import org.junit.Test
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
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
                            Operation(ArrayCall("args", 0), EQUALS_COMPARE, String("true"))
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

        /*
        mainFunction.innerCode.expressions.forEach {
            println(it)
        }
        */

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
                Expression(listOf(
                    Variable("result", true, CrescentAST.Visibility.LOCAL_SCOPE, Type.Implicit,
                        Expression(listOf(
                            Statement.When(
                                Argument(Expression(listOf(VariableCall("operation")))),
                                listOf(
                                    Statement.When.Clause(
                                        Expression(listOf(Char('+'))),
                                        Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(
                                            VariableCall("input1"),
                                            ADD,
                                            VariableCall("input2")))))))))
                                    ),
                                    Statement.When.Clause(
                                        Expression(listOf(Char('-'))),
                                        Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(
                                            VariableCall("input1"),
                                            SUB,
                                            VariableCall("input2")))))))))
                                    ),
                                    Statement.When.Clause(
                                        Expression(listOf(Char('*'))),
                                        Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(
                                            VariableCall("input1"),
                                            MUL,
                                            VariableCall("input2")))))))))
                                    ),
                                    Statement.When.Clause(
                                        Expression(listOf(Char('/'))),
                                        Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(
                                            VariableCall("input1"),
                                            DIV,
                                            VariableCall("input2")))))))))
                                    )
                                )
                            ),
                        ))
                    ))),
                Expression(listOf(
                    FunctionCall("println", listOf(Argument(Expression(listOf(VariableCall("result"))))))
                ))
            ),
            mainFunction.innerCode.expressions,
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

        val crescentFile = CrescentParser.invoke(File("example.crescent"), tokens)

        assertEquals(
            File(
                name = crescentFile.name,
                path = crescentFile.path,
                imports = emptyList(),
                structs = emptyList(),
                impls = emptyList(),
                traits = emptyList(),
                objects = crescentFile.objects.take(1),
                enums = emptyList(),
                variables = emptyList(),
                constants = crescentFile.constants.take(1),
                functions = emptyList(),
                mainFunction = null
            ),
            crescentFile,
            "Crescent file isn't structured as expected"
        )

        assertContentEquals(
            listOf(Constant("thing", CrescentAST.Visibility.PUBLIC, Type.Implicit, Expression(listOf(String("Meow"))))),
            crescentFile.constants,
            "Variables not as expected"
        )

        val constantsObject = assertNotNull(
            crescentFile.objects.find { it.name == "Constants" },
            "Could not find Constants object"
        )

        assertContentEquals(
            listOf(
                Constant("thing", CrescentAST.Visibility.PUBLIC, Type.Implicit, Expression(listOf(String("Meow"))))
            ),
            constantsObject.constants,
        )
    }

    @Test
    fun math() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {
                println((1 + 1) + 2 / 10 + 1000 * 10 / 10 ^ 10)
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
                Expression(listOf(FunctionCall("println", listOf(Argument(Expression(listOf(
                    Operation(Operation(Operation(Operation(Operation(Operation(Expression(listOf(Operation(Number(1.0), ADD, Number(1.0)))), ADD, Number(2.0)), DIV, Number(10.0)), ADD, Number(1000.0)), MUL, Number(10.0)), DIV, Number(10.0)), POW, Number(10.0))
                )))))))
            ),
            mainFunction.innerCode.expressions,
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
                CrescentToken.Type.STRUCT, CrescentToken.Key("Thing1"), CrescentToken.Parenthesis.OPEN, CrescentToken.Variable.VAL, CrescentToken.Key("name"), TYPE_PREFIX, CrescentToken.Key("String"), CrescentToken.Parenthesis.CLOSE,
                CrescentToken.Type.STRUCT, CrescentToken.Key("Thing2"), CrescentToken.Parenthesis.OPEN, CrescentToken.Variable.VAL, CrescentToken.Key("id"), TYPE_PREFIX, CrescentToken.Key("i32"), CrescentToken.Parenthesis.CLOSE,
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
                CrescentToken.Number(1.0), ADD, CrescentToken.Comment("Meow"),
                CrescentToken.Number(1.0), SUB, CrescentToken.Comment("Meow"),
                CrescentToken.Number(1.0), DIV, CrescentToken.Comment("Meow"),
                CrescentToken.Number(1.0), MUL, CrescentToken.Comment("Meow"),
                CrescentToken.Number(1.0), ASSIGN, CrescentToken.Comment("Meow"),
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
                CrescentToken.Statement.IMPORT, CrescentToken.Key("crescent"), DOT, CrescentToken.Key("examples"), IMPORT_SEPARATOR, CrescentToken.Key("Thing"),
                CrescentToken.Statement.IMPORT, CrescentToken.Key("crescent"), DOT, CrescentToken.Key("examples"), AS, CrescentToken.Key("examples"),
                CrescentToken.Comment("Short hand method (If in same package)"),
                CrescentToken.Statement.IMPORT, IMPORT_SEPARATOR, CrescentToken.Key("Thing")
            ),
            tokens
        )
    }

}