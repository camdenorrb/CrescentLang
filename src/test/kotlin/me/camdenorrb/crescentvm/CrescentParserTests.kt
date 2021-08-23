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
                Expression(listOf(
                    FunctionCall("println", listOf(Expression(listOf(String("Hello World")))))
                ))
            ),
            mainFunction.innerCode.expressions,
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
                Expression(listOf(
                    Statement.If(
                        Expression(listOf(
                            GetCall("args", listOf(Expression(listOf(Number(0))))), Operator(EQUALS_COMPARE), String("true")
                        )),
                        Statement.Block(listOf(
                            Expression(listOf(FunctionCall("println", listOf(Expression(listOf(String("Meow")))))))
                        )),
                    ),
                )),
                Expression(listOf(
                    Statement.Else(
                        Statement.Block(listOf(
                            Expression(listOf(FunctionCall("println", listOf(Expression(listOf(String("Hiss")))))))
                        ))
                    )
                ))
            ),
            mainFunction.innerCode.expressions,
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
                Expression(listOf(
                    Variable("input", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                        FunctionCall("readBoolean", listOf(Expression(listOf(String("Enter a boolean value [true/false]")))))
                    )))
                )),
                Expression(listOf(
                    Statement.If(
                        Expression(listOf(
                            Identifier("input")
                        )),
                        Statement.Block(listOf(Expression(listOf(
                            FunctionCall("println", listOf(Expression(listOf(String("Meow")))))
                        )))),
                    ),
                )),
                Expression(listOf(
                    Statement.Else(
                        Statement.Block(listOf(Expression(listOf(
                            FunctionCall("println", listOf(Expression(listOf(String("Hiss")))))
                        ))))
                    )
                ))
            ),
            mainFunction.innerCode.expressions,
        )
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
                Expression(listOf(
                    Variable("input1", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                        FunctionCall("readDouble", listOf(Expression(listOf(String("Enter your first number")))))
                    )))
                )),
                Expression(listOf(
                    Variable("input2", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                        FunctionCall("readDouble", listOf(Expression(listOf(String("Enter your second number")))))
                    )))
                )),
                Expression(listOf(
                    Variable("operation", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(
                        FunctionCall("readLine", listOf(Expression(listOf(String("Enter an operation [+, -, *, /]")))))
                    )))
                )),
                Expression(listOf(
                    Variable("result", true, Visibility.PUBLIC, Type.Implicit,
                        Expression(listOf(
                            When(
                                Expression(listOf(Identifier("operation"))),
                                listOf(
                                    When.Clause(
                                        Expression(listOf(Char('+'))),
                                        Statement.Block(listOf(Expression(listOf(
                                            Identifier("input1"), Operator(ADD), Identifier("input2")
                                        ))))
                                    ),
                                    When.Clause(
                                        Expression(listOf(Char('-'))),
                                        Statement.Block(listOf(Expression(listOf(
                                            Identifier("input1"), Operator(SUB), Identifier("input2")
                                        ))))
                                    ),
                                    When.Clause(
                                        Expression(listOf(Char('*'))),
                                        Statement.Block(listOf(Expression(listOf(
                                            Identifier("input1"), Operator(MUL), Identifier("input2")
                                        ))))
                                    ),
                                    When.Clause(
                                        Expression(listOf(Char('/'))),
                                        Statement.Block(listOf(Expression(listOf(
                                            Identifier("input1"), Operator(DIV), Identifier("input2")
                                        ))))
                                    )
                                )
                            ),
                        ))
                    ))),
                Expression(listOf(
                    FunctionCall("println", listOf(Expression(listOf(Identifier("result")))))
                ))
            ),
            mainFunction.innerCode.expressions,
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
                                Expression(listOf(Return(Expression(listOf(Identifier("value1"), Operator(ADD), Identifier("value2"))))))
                            ))
                        ),
                        Function(
                            name = "sub",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = listOf(Parameter.Basic("value1", Type.Basic("Int")), Parameter.Basic("value2", Type.Basic("Int"))),
                            returnType = Type.Basic("Int"),
                            innerCode = Statement.Block(listOf(
                                Expression(listOf(Return(Expression(listOf(Identifier("value1"), Operator(SUB), Identifier("value2"))))))
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
            listOf(Expression(listOf(
                FunctionCall("println", listOf(Expression(listOf(
                    Expression(listOf(Number(1.0), Operator(ADD), Number(1))),
                    Operator(ADD), Number(1), Operator(DIV), Number(10), Operator(ADD), Number(1000), Operator(MUL), Number(10), Operator(DIV), Number(10), Operator(POW), Number(10)
                    //Operation(Operation(Operation(Operation(Operation(Operation(Expression(listOf(Operation(Number(1.0), ADD, Number(1.0)))), ADD, Number(1.0)), DIV, Number(10.0)), ADD, Number(1000.0)), MUL, Number(10.0)), DIV, Number(10.0)), POW, Number(10.0))
                ))))))
            ),
            mainFunction.innerCode.expressions,
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

        assertContentEquals(
            expected = listOf(
                Enum(
                    name = "Color",
                    parameters = listOf(Parameter.Basic("name", Type.Basic("String"))),
                    structs = listOf(
                        EnumEntry("RED", listOf(Expression(listOf(String("Red"))))),
                        EnumEntry("GREEN", listOf(Expression(listOf(String("Green"))))),
                        EnumEntry("BLUE", listOf(Expression(listOf(String("Blue"))))),
                    ),
                ),
            ),
            actual = crescentFile.enums,
        )

        assertEquals(
            expected = Function(
                name = "main",
                modifiers = emptyList(),
                visibility = Visibility.PUBLIC,
                params = emptyList(),
                returnType = Type.Unit,
                innerCode = Statement.Block(listOf(
                    Expression(listOf(
                        Variable("color", true, Visibility.PUBLIC, Type.Implicit, Expression(listOf(Identifier("Color"), Operator(DOT), FunctionCall("random", emptyList()))))
                    )),
                    Expression(listOf(
                        When(Expression(listOf(Identifier("color"))), listOf(
                            When.Clause(
                                Expression(listOf(InstanceOf(Expression(listOf(Operator(DOT), Identifier("RED")))))),
                                Statement.Block(listOf(
                                    Expression(listOf(FunctionCall("println", listOf(Expression(listOf(String("Meow"))))))),
                                ))
                            ),
                            When.Clause(
                                Expression(listOf(InstanceOf(Expression(listOf(Operator(DOT), Identifier("GREEN")))))),
                                Statement.Block(emptyList())
                            ),
                            When.Clause(
                                null,
                                Statement.Block(emptyList())
                            ),
                        ))
                    )),
                    Expression(listOf(
                        // TODO: Encode name into a Variable instead of a bunch of tokens
                        When(Expression(listOf(Identifier("name"), Operator(ASSIGN), Identifier("color"), Operator(DOT), Identifier("name"))), listOf(
                            When.Clause(
                                Expression(listOf(String("Red"))),
                                Statement.Block(listOf(
                                    Expression(listOf(FunctionCall("println", listOf(Expression(listOf(Identifier("name"))))))),
                                ))
                            ),
                            When.Clause(
                                Expression(listOf(String("Green"))),
                                Statement.Block(emptyList())
                            ),
                            When.Clause(
                                null,
                                Statement.Block(emptyList())
                            ),
                        ))
                    )),
                ))
            ),
            actual = crescentFile.mainFunction,
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
                Expression(listOf(Identifier("println"))),
                Expression(listOf(String("#meow"))),
                Expression(listOf(Number(1), Operator(ADD))),
                Expression(listOf(Number(1), Operator(SUB))),
                Expression(listOf(Number(1), Operator(DIV))),
                Expression(listOf(Number(1), Operator(MUL))),
                Expression(listOf(Number(1), Operator(ASSIGN))),
            ),
            mainFunction.innerCode.expressions,
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