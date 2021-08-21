package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.*
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.String
import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentToken
import me.camdenorrb.crescentvm.vm.CrescentToken.Operator.*
import me.camdenorrb.crescentvm.vm.CrescentToken.Visibility
import org.junit.Test
import java.nio.file.Path
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
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
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
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
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
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            emptyList(),
            mainFunction.params
        )

        assertContentEquals(
            listOf(
                Expression(listOf(
                    Variable("input", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
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
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            emptyList(),
            mainFunction.params
        )

        assertContentEquals(
            listOf(
                Expression(listOf(
                    Variable("input1", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                        FunctionCall("readDouble", listOf(Argument(Expression(listOf(String("Enter your first number"))))))
                    )))
                )),
                Expression(listOf(
                    Variable("input2", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                        FunctionCall("readDouble", listOf(Argument(Expression(listOf(String("Enter your second number"))))))
                    )))
                )),
                Expression(listOf(
                    Variable("operation", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                        FunctionCall("readLine", listOf(Argument(Expression(listOf(String("Enter an operation [+, -, *, /]"))))))
                    )))
                )),
                Expression(listOf(
                    Variable("result", true, Visibility.PUBLIC, Type.Implicit,
                        Expression(listOf(
                            Statement.When(
                                Argument(Expression(listOf(VariableCall("operation")))),
                                listOf(
                                    Statement.When.Clause(
                                        Expression(listOf(Char('+'))),
                                        Statement.Block(listOf(Expression(listOf(Operation(
                                            VariableCall("input1"),
                                            ADD,
                                            VariableCall("input2"))))))
                                    ),
                                    Statement.When.Clause(
                                        Expression(listOf(Char('-'))),
                                        Statement.Block(listOf(Expression(listOf(Operation(
                                            VariableCall("input1"),
                                            SUB,
                                            VariableCall("input2"))))))
                                    ),
                                    Statement.When.Clause(
                                        Expression(listOf(Char('*'))),
                                        Statement.Block(listOf(Expression(listOf(Operation(
                                            VariableCall("input1"),
                                            MUL,
                                            VariableCall("input2"))))))
                                    ),
                                    Statement.When.Clause(
                                        Expression(listOf(Char('/'))),
                                        Statement.Block(listOf(Expression(listOf(Operation(
                                            VariableCall("input1"),
                                            DIV,
                                            VariableCall("input2"))))))
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

        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        assertEquals(
            File(
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
                mainFunction = null,
                sealeds = emptyList()
            ),
            crescentFile,
            "Crescent file isn't structured as expected"
        )

        assertContentEquals(
            listOf(Constant("thing", Visibility.PUBLIC, Type.Implicit, Expression(listOf(String("Meow"))))),
            crescentFile.constants,
            "Variables not as expected"
        )

        val constantsObject = assertNotNull(
            crescentFile.objects.find { it.name == "Constants" },
            "Could not find Constants object"
        )

        assertContentEquals(
            listOf(
                Constant("thing", Visibility.PUBLIC, Type.Implicit, Expression(listOf(String("Meow"))))
            ),
            constantsObject.constants,
        )
    }

    @Test
    fun impl() {

        val tokens = CrescentLexer.invoke(TestCode.impl)
        val parsed = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        assertEquals(
            File(
                path = parsed.path,
                imports = emptyList(),
                structs = parsed.structs.take(1),
                impls = parsed.impls.take(2),
                traits = emptyList(),
                objects = emptyList(),
                enums = emptyList(),
                variables = emptyList(),
                constants = emptyList(),
                functions = emptyList(),
                mainFunction = null,
                sealeds = emptyList()
            ),
            parsed,
            "Crescent file isn't structured as expected"
        )


        assertContentEquals(
            listOf(
                Struct("Example", listOf(
                    Variable("aNumber", true, Visibility.PUBLIC, Type.Basic("Int"), Expression(emptyList())),
                    Variable("aValue1", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(String("")))),
                    Variable("aValue2", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(String("")))),
                ))
            ),
            parsed.structs,
        )

        assertContentEquals(
            listOf(

                Impl(
                    type = Type.Basic("Example"),
                    modifiers = emptyList(),
                    functions = listOf(
                        Function(
                            name = "add",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = listOf(Parameter.Basic("value1", Type.Basic("Int")), Parameter.Basic("value2", Type.Basic("Int"))),
                            returnType = Type.Basic("Int"),
                            innerCode = Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(VariableCall("value1"), ADD, VariableCall("value2")))))))))
                        ),
                        Function(
                            name = "sub",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = listOf(Parameter.Basic("value1", Type.Basic("Int")), Parameter.Basic("value2", Type.Basic("Int"))),
                            returnType = Type.Basic("Int"),
                            innerCode = Statement.Block(listOf(Expression(listOf(Return(Expression(listOf(Operation(VariableCall("value1"), SUB, VariableCall("value2")))))))))
                        ),
                    ),
                    extends = emptyList(),
                ),
                Impl(
                    type = Type.Basic("Example"),
                    modifiers = listOf(CrescentToken.Modifier.STATIC),
                    functions = emptyList(),
                    extends = emptyList(),
                ),
            ),
            parsed.impls,
        )

        println(parsed)
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

        val mainFunction = assertNotNull(
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            emptyList(),
            mainFunction.params
        )

        assertContentEquals(
            listOf(
                Expression(listOf(FunctionCall("println", listOf(Argument(Expression(listOf(
                    Operation(Operation(Operation(Operation(Operation(Operation(Expression(listOf(Operation(Number(1.0), ADD, Number(1.0)))), ADD, Number(1.0)), DIV, Number(10.0)), ADD, Number(1000.0)), MUL, Number(10.0)), DIV, Number(10.0)), POW, Number(10.0))
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

        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        assertEquals(
            File(
                path = crescentFile.path,
                imports = emptyList(),
                structs = emptyList(),
                sealeds = crescentFile.sealeds.take(1),
                impls = emptyList(),
                traits = emptyList(),
                objects = emptyList(),
                enums = emptyList(),
                variables = emptyList(),
                constants = emptyList(),
                functions = emptyList(),
                mainFunction = null,
            ),
            crescentFile,
            "Crescent file isn't structured as expected"
        )

        val sealedExample = assertNotNull(
            crescentFile.sealeds.find { it.name == "Example" },
            "Could not find Constants object"
        )

        assertContentEquals(
            listOf(
                Struct("Thing1", listOf(Variable("name", true, Visibility.PUBLIC, Type.Basic("String"), Expression(emptyList())))),
                Struct("Thing2", listOf(Variable("id", true, Visibility.PUBLIC, Type.Basic("i32"), Expression(emptyList())))),
            ),
            sealedExample.structs,
        )
    }


    @Test
    fun enum() {

        val tokens = CrescentLexer.invoke(
            """
            enum Color(name: String) {
                RED("Red")
                GREEN("Green")
                BLUE("Blue")
            }
            
            fun main {
            
                # .random() will be built into the Enum type implementation
            
                val color = Color.random()
            
                # Shows off cool Enum shorthand for when statements
                when(color) {
            
                    is .RED   -> { println("Meow") }
                    is .GREEN -> {}
            
                    #else -> {}
                }
            
                when(name = color.name) {
            
                    "Red"   -> println(name)
                    "Green" -> {}
            
                    else -> {}
                }
            
            }
            """.trimIndent()
        )

        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        //TODO("Add test things")
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

        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        val mainFunction = assertNotNull(
            crescentFile.mainFunction,
            "No main function found"
        )

        assertEquals(
            File(
                path = crescentFile.path,
                imports = emptyList(),
                structs = emptyList(),
                sealeds = emptyList(),
                impls = emptyList(),
                traits = emptyList(),
                objects = emptyList(),
                enums = emptyList(),
                variables = emptyList(),
                constants = emptyList(),
                functions = listOf(mainFunction),
                mainFunction = mainFunction,
            ),
            crescentFile,
            "Crescent file isn't structured as expected"
        )

        assertContentEquals(
            listOf(
                Expression(listOf(VariableCall("println"))),
                Expression(listOf(String("#meow"))),
                Expression(listOf(Number(1.0))),
                Expression(listOf(Number(1.0))),
                Expression(listOf(Number(1.0))),
                Expression(listOf(Number(1.0))),
                Expression(listOf(Number(1.0))),
            ),
            mainFunction.innerCode.expressions,
        )
    }

    @Test
    fun imports() {

        val tokens = CrescentLexer.invoke(
            """
            # Current idea, Package -> Type
            import crescent.examples::Thing

            # import crescent.examples as examples
            
            # Short hand method (If in same package)
            import ::Thing
            """.trimIndent()
        )


        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        assertEquals(
            File(
                path = crescentFile.path,
                imports = crescentFile.imports.take(3),
                structs = emptyList(),
                sealeds = emptyList(),
                impls = emptyList(),
                traits = emptyList(),
                objects = emptyList(),
                enums = emptyList(),
                variables = emptyList(),
                constants = emptyList(),
                functions = emptyList(),
                mainFunction = null,
            ),
            crescentFile,
            "Crescent file isn't structured as expected"
        )

        assertContentEquals(
            listOf(
                Import("crescent.examples", "Thing"),
                //Import("crescent.examples", "", "examples"),
                Import("", "Thing"),
            ),
            crescentFile.imports
        )
    }

}