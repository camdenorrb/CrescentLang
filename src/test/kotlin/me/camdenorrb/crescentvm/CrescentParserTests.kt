package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.data.TestCode
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.*
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.Statement.When
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

        val tokens = CrescentLexer.invoke(TestCode.helloWorld)

        val mainFunction = assertNotNull(
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                FunctionCall("println", listOf(Expression(listOf(String("Hello World")))))
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun ifStatement() {

        val tokens = CrescentLexer.invoke(TestCode.ifStatement)

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
                Statement.If(
                    Expression(listOf(
                        GetCall("args", listOf(Expression(listOf(Number(0))))), Operator(EQUALS_COMPARE), String("true")
                    )),
                    Statement.Block(listOf(
                        FunctionCall("println", listOf(Expression(listOf(String("Meow")))))
                    )),
                ),
                Statement.Else(
                    Statement.Block(listOf(
                        FunctionCall("println", listOf(Expression(listOf(String("Hiss")))))
                    ))
                )
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun ifInputStatement() {

        val tokens = CrescentLexer.invoke(TestCode.ifInputStatement)

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
                Variable("input", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                    FunctionCall(
                        "readBoolean",
                        listOf(Expression(listOf(String("Enter a boolean value [true/false]"))))
                    )
                ))),
                Statement.If(
                    Expression(listOf(
                        Identifier("input")
                    )),
                    Statement.Block(listOf(
                        FunctionCall("println", listOf(Expression(listOf(String("Meow")))))
                    )),
                ),
                Statement.Else(
                    Statement.Block(listOf(
                        FunctionCall("println", listOf(Expression(listOf(String("Hiss")))))
                    ))
                )
            ),
            mainFunction.innerCode.nodes,
        )
    }


    @Test
    fun stringInterpolation() {

        val tokens = CrescentLexer.invoke(TestCode.stringInterpolation)

        val mainFunction = assertNotNull(
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        println(mainFunction.innerCode)

        assertContentEquals(
            listOf(
                Variable("x", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(Number(0)))),
                Variable("y", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(Number(0)))),
                Variable("z", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(Number(0)))),
                FunctionCall("println", listOf(Expression(listOf(String("\$x\$y\$z")))))
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun forLoop() {

        val tokens = CrescentLexer.invoke(TestCode.forLoop)


    }

    @Test
    fun whileLoop() {

        val tokens = CrescentLexer.invoke(TestCode.whileLoop)

    }

    @Test
    fun calculator() {

        val tokens = CrescentLexer.invoke(TestCode.calculator)

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
                Variable("input1", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                    FunctionCall("readDouble", listOf(Expression(listOf(String("Enter your first number")))))
                ))),
                Variable("input2", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                    FunctionCall("readDouble", listOf(Expression(listOf(String("Enter your second number")))))
                ))),
                Variable("operation", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                    FunctionCall("readLine", listOf(Expression(listOf(String("Enter an operation [+, -, *, /]")))))
                ))),
                Variable("result", true, Visibility.PUBLIC, Type.Implicit,
                    Expression(listOf(
                        When(
                            Expression(listOf(Identifier("operation"))),
                            listOf(
                                When.Clause(
                                    Char('+'),
                                    Statement.Block(listOf(Expression(listOf(
                                        Identifier("input1"), Operator(ADD), Identifier("input2")
                                    ))))
                                ),
                                When.Clause(
                                    Char('-'),
                                    Statement.Block(listOf(Expression(listOf(
                                        Identifier("input1"), Operator(SUB), Identifier("input2")
                                    ))))
                                ),
                                When.Clause(
                                    Char('*'),
                                    Statement.Block(listOf(Expression(listOf(
                                        Identifier("input1"), Operator(MUL), Identifier("input2")
                                    ))))
                                ),
                                When.Clause(
                                    Char('/'),
                                    Statement.Block(listOf(Expression(listOf(
                                        Identifier("input1"), Operator(DIV), Identifier("input2")
                                    ))))
                                )
                            )
                        ),
                    ))
                ),
                FunctionCall("println", listOf(Expression(listOf(Identifier("result")))))

            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun constantsAndObject() {

        val tokens = CrescentLexer.invoke(TestCode.constantsAndObject)

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
                            innerCode = Statement.Block(listOf(
                                Return(Expression(listOf(Identifier("value1"), Operator(ADD), Identifier("value2"))))
                            ))
                        ),
                        Function(
                            name = "sub",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = listOf(Parameter.Basic("value1", Type.Basic("Int")), Parameter.Basic("value2", Type.Basic("Int"))),
                            returnType = Type.Basic("Int"),
                            innerCode = Statement.Block(listOf(
                                Return(Expression(listOf(Identifier("value1"), Operator(SUB), Identifier("value2"))))
                            ))
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
    }



    @Test
    fun math() {

        val tokens = CrescentLexer.invoke(TestCode.math)

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
                FunctionCall("println", listOf(Expression(listOf(
                    Expression(listOf(Number(1.0), Operator(ADD), Number(1))),
                    Operator(ADD), Number(1), Operator(DIV), Number(10), Operator(ADD), Number(1000), Operator(MUL), Number(10), Operator(DIV), Number(10), Operator(POW), Number(10)
                ))))
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun sealed() {

        val tokens = CrescentLexer.invoke(TestCode.sealed)

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

        val tokens = CrescentLexer.invoke(TestCode.enum)
        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        val mainFunction = assertNotNull(
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                Enum(
                    name = "Color",
                    parameters = listOf(Parameter.Basic("name", Type.Basic("String"))),
                    structs = listOf(
                        EnumEntry("RED", listOf(Expression(listOf(String("Red"))))),
                        EnumEntry("GREEN", listOf(Expression(listOf(String("Green"))))),
                        EnumEntry("BLUE", listOf(Expression(listOf(String("Blue"))))),
                    ),
                ),
            ), crescentFile.enums,
        )

        println(mainFunction.innerCode.nodes)

        assertContentEquals(
            listOf(
                Variable("color", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(DotChain(listOf(Identifier("Color"), FunctionCall("random", emptyList())))))),
                When(Expression(listOf(Identifier("color"))), listOf(
                    When.Clause(
                        InstanceOf(Expression(listOf(Operator(DOT), Identifier("RED")))),
                        Statement.Block(listOf(
                            FunctionCall("println", listOf(Expression(listOf(String("Meow")))))
                        ))
                    ),
                    When.Clause(
                        InstanceOf(Expression(listOf(Operator(DOT), Identifier("GREEN")))),
                        Statement.Block(emptyList())
                    ),
                    When.Clause(
                        null,
                        Statement.Block(emptyList())
                    ),
                )),
                // TODO: Encode name into a Variable instead of a bunch of tokens
                When(Expression(listOf(Identifier("name"), Operator(ASSIGN), DotChain(listOf(Identifier("color"), Identifier("name"))))), listOf(
                    When.Clause(
                        String("Red"),
                        Statement.Block(listOf(
                            Expression(listOf(
                                FunctionCall("println", listOf(Expression(listOf(Identifier("name"))))),
                            ))
                        ))
                    ),
                    When.Clause(
                        String("Green"),
                        Statement.Block(emptyList())
                    ),
                    When.Clause(
                        null,
                        Statement.Block(emptyList())
                    ),
                )),
            ).also { println(it) },
            mainFunction.innerCode.nodes,
        )

    }

    @Test
    fun comments() {

        val tokens = CrescentLexer.invoke(TestCode.comments)

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
                Identifier("println"),
                String("#meow"),
                Number(1), Operator(ADD),
                Number(1), Operator(SUB),
                Number(1), Operator(DIV),
                Number(1), Operator(MUL),
                Number(1), Operator(ASSIGN),
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun imports() {

        val tokens = CrescentLexer.invoke(TestCode.imports)

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