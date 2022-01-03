package dev.twelveoclock.lang.crescent

import dev.twelveoclock.lang.crescent.data.TestCode
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.*
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Enum
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Function
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Primitive.Char
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Primitive.Number.I16
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Primitive.Number.I8
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Primitive.String
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Statement.When
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Operator.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Visibility
import dev.twelveoclock.lang.crescent.lexers.CrescentLexer
import dev.twelveoclock.lang.crescent.parsers.CrescentParser
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

internal class CrescentParserTests {

    @Test
    fun helloWorld() {

        val tokens = CrescentLexer.invoke(TestCode.helloWorlds)

        val mainFunction = assertNotNull(
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                IdentifierCall("println", listOf(String("Hello World"))),
                IdentifierCall("println", listOf(String("Hello World"))),
                IdentifierCall("println", listOf(String("Hello World"))),
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
                        GetCall("args", listOf(I8(0))), String("true"), EQUALS_COMPARE
                    )),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(String("Meow")))
                    )),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(String("Hiss")))
                    )),
                ),
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
                Variable.Basic("input", true, Visibility.PUBLIC, Type.Implicit, IdentifierCall(
                    "readBoolean",
                    listOf(String("Enter a boolean value [true/false]"))
                )),
                Statement.If(
                    Identifier("input"),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(String("Meow")))
                    )),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(String("Hiss")))
                    )),
                ),
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

        assertContentEquals(
            listOf(
                Variable.Basic("x", true, Visibility.PUBLIC, Type.Implicit, I8(0)),
                Variable.Basic("y", true, Visibility.PUBLIC, Type.Implicit, I8(0)),
                Variable.Basic("z", true, Visibility.PUBLIC, Type.Implicit, I8(0)),

                IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),
                IdentifierCall("println", listOf(Expression(listOf(String("Hello "), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD, String(" Hello"), ADD)))),
                IdentifierCall("println", listOf(Expression(listOf(String("Hello "), Identifier("x"), ADD, String(" Hello "), ADD, Identifier("y"), ADD, String(" Hello "), ADD, Identifier("z"), ADD, String(" Hello"), ADD)))),

                IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),
                IdentifierCall("println", listOf(Expression(listOf(String("Hello "), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD, String(" Hello"), ADD)))),
                IdentifierCall("println", listOf(Expression(listOf(String("Hello "), Identifier("x"), ADD, String("Hello"), ADD, Identifier("y"), ADD, String("Hello"), ADD, Identifier("z"), ADD, String(" Hello"), ADD)))),

                IdentifierCall("println", listOf(Expression(listOf(String(""), Char('$'), ADD)))),
                IdentifierCall("println", listOf(String("\$x"))),

                IdentifierCall("println", listOf(String("$ x"))),
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun forLoop() {

        val tokens = CrescentLexer.invoke(TestCode.forLoop1)

        val mainFunction = assertNotNull(
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(

                Variable.Basic("x", true, Visibility.PUBLIC, Type.Implicit, I8(0)),
                Variable.Basic("y", true, Visibility.PUBLIC, Type.Implicit, I8(0)),
                Variable.Basic("z", true, Visibility.PUBLIC, Type.Implicit, I8(0)),

                // TODO: Simplify expression into the above list since it's the only expression
                IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),

                Statement.For(
                    listOf(Identifier("x"), Identifier("y"), Identifier("z")),
                    listOf(Statement.Range(I8(0), I8(10))),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),
                    ))
                ),
                Statement.For(
                    listOf(Identifier("x"), Identifier("y"), Identifier("z")),
                    listOf(Statement.Range(I8(0), I8(10)), Statement.Range(I8(0), I8(10)), Statement.Range(I8(0), I8(10))),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(Expression(listOf(String(""), Identifier("x"), ADD, Identifier("y"), ADD, Identifier("z"), ADD)))),
                    ))
                ),
                IdentifierCall("println", listOf(String("Hello World")))
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun whileLoop() {

        val tokens = CrescentLexer.invoke(TestCode.whileLoop)

        val mainFunction = assertNotNull(
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        assertContentEquals(
            listOf(
                Variable.Basic("x", false, Visibility.PUBLIC, Type.Implicit, I8(1)),
                Statement.While(
                    Expression(listOf(
                        Identifier("x"), I8(10), LESSER_EQUALS_COMPARE
                    )),
                    Statement.Block(listOf(
                        IdentifierCall("println", listOf(Identifier("x"))),
                        Expression(listOf(Identifier("x"), I8(1), ADD_ASSIGN)),
                    ))
                )

            ),
            mainFunction.innerCode.nodes,
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
                Variable.Basic("input1", true, Visibility.PUBLIC, Type.Implicit,
                    IdentifierCall("readDouble", listOf(String("Enter your first number")))
                ),
                Variable.Basic("input2", true, Visibility.PUBLIC, Type.Implicit,
                    IdentifierCall("readDouble", listOf(String("Enter your second number")))
                ),
                Variable.Basic("operation", true, Visibility.PUBLIC, Type.Implicit,
                    IdentifierCall("readLine", listOf(String("Enter an operation [+, -, *, /]")))
                ),
                Variable.Basic("result", true, Visibility.PUBLIC, Type.Implicit,
                    When(
                        Identifier("operation"),
                        listOf(
                            When.Clause(
                                Char('+'),
                                Statement.Block(listOf(Expression(listOf(
                                    Identifier("input1"), Identifier("input2"), ADD
                                ))))
                            ),
                            When.Clause(
                                Char('-'),
                                Statement.Block(listOf(Expression(listOf(
                                    Identifier("input1"), Identifier("input2"), SUB
                                ))))
                            ),
                            When.Clause(
                                Char('*'),
                                Statement.Block(listOf(Expression(listOf(
                                    Identifier("input1"), Identifier("input2"), MUL
                                ))))
                            ),
                            When.Clause(
                                Char('/'),
                                Statement.Block(listOf(Expression(listOf(
                                    Identifier("input1"), Identifier("input2"), DIV
                                ))))
                            )
                        )
                    ),
                ),
                IdentifierCall("println", listOf(Identifier("result")))
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun constantsAndObject() {

        val tokens = CrescentLexer.invoke(TestCode.constantsAndObject)
        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        assertContentEquals(
            listOf(Variable.Constant("thing1", Visibility.PUBLIC, Type.Implicit, String("Mew"))),
            crescentFile.constants.values,
            "Variables not as expected"
        )

        val mainFunction = assertNotNull(
            CrescentParser.invoke(Path.of("example.crescent"), tokens).mainFunction,
            "No main function found"
        )

        val constantsObject = assertNotNull(
            crescentFile.objects["Constants"],
            "Could not find Constants object"
        )

        assertContentEquals(
            listOf(
                Variable.Constant("thing2", Visibility.PUBLIC, Type.Implicit, String("Meow")),
            ),
            constantsObject.constants,
        )

        assertContentEquals(
            listOf(
                IdentifierCall("println", listOf(Identifier("thing1"))),
                IdentifierCall("println", listOf(Identifier("thing2")))
            ),
            constantsObject.functions.find { it.name == "printThing" }!!.innerCode.nodes
        )
        assertContentEquals(
            listOf(
                DotChain(listOf(Identifier("Constants"), IdentifierCall("printThings")))
            ),
            mainFunction.innerCode.nodes
        )
    }

    @Test
    fun impl() {

        val tokens = CrescentLexer.invoke(TestCode.impl)
        val parsed = CrescentParser.invoke(Path.of("example.crescent"), tokens)
        val mainFunction = assertNotNull(parsed.mainFunction, "No main function found")

        assertContentEquals(
            listOf(
                Struct("Example", listOf(
                    Variable.Basic("aNumber", true, Visibility.PUBLIC, Type.Basic("Int"), Expression(emptyList())),
                    Variable.Basic("aValue1", true, Visibility.PUBLIC, Type.Implicit, String("")),
                    Variable.Basic("aValue2", true, Visibility.PUBLIC, Type.Implicit, String("")),
                ))
            ),
            parsed.structs.values,
        )

        assertContentEquals(
            listOf(
                Variable.Basic("example", true, Visibility.PUBLIC, Type.Implicit, IdentifierCall("Example", listOf(I8(1), String("Meow"), String("Mew")))),
                DotChain(listOf(Identifier("example"), IdentifierCall("printValues"))),
                IdentifierCall("println"),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("example"), Identifier("aNumber"))))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("example"), Identifier("aValue1"))))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("example"), Identifier("aValue2"))))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("Example"), IdentifierCall("add", listOf(I8(1), I8(2))))))),
                IdentifierCall("println", listOf(DotChain(listOf(Identifier("Example"), IdentifierCall("sub", listOf(I8(1), I8(2))))))),
            ),
            mainFunction.innerCode.nodes,
        )

        assertContentEquals(
            listOf(
                Impl(
                    type = Type.Basic("Example"),
                    modifiers = emptyList(),
                    functions = listOf(
                        Function(
                            name = "printValues",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = emptyList(),
                            returnType = Type.unit,
                            innerCode = Statement.Block(listOf(
                                IdentifierCall("println", listOf(Identifier("aNumber"))),
                                IdentifierCall("println", listOf(Identifier("aValue1"))),
                                IdentifierCall("println", listOf(Identifier("aValue2"))),
                            ))
                        ),
                    ),
                    extends = emptyList(),
                ),
            ),
            parsed.impls.values,
        )

        assertContentEquals(
            listOf(
                Impl(
                    type = Type.Basic("Example"),
                    modifiers = listOf(CrescentToken.Modifier.STATIC),
                    functions = listOf(
                        Function(
                            name = "add",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = listOf(Parameter.Basic("value1", Type.Basic("Int")), Parameter.Basic("value2", Type.Basic("Int"))),
                            returnType = Type.Basic("Int"),
                            innerCode = Statement.Block(listOf(
                                Return(Expression(listOf(Identifier("value1"), Identifier("value2"), ADD)))
                            ))
                        ),
                        Function(
                            name = "sub",
                            modifiers = emptyList(),
                            visibility = Visibility.PUBLIC,
                            params = listOf(Parameter.Basic("value1", Type.Basic("Int")), Parameter.Basic("value2", Type.Basic("Int"))),
                            returnType = Type.Basic("Int"),
                            innerCode = Statement.Block(listOf(
                                Return(Expression(listOf(Identifier("value1"), Identifier("value2"), SUB)))
                            ))
                        ),
                    ),
                    extends = emptyList(),
                )
            ),
            parsed.staticImpls.values,
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
                IdentifierCall("println", listOf(Expression(listOf(
                    I8(1), I8(1), ADD,
                    I8(1), I8(10), DIV, ADD, I16(1000), I8(10), MUL, I8(11), I8(10), POW, DIV, ADD
                )))),
                IdentifierCall("println", listOf(Expression(listOf(
                    I8(4), I8(3), MUL, I8(1), ADD
                )))),
            ),
            mainFunction.innerCode.nodes,
        )
    }

    @Test
    fun sealed() {

        val tokens = CrescentLexer.invoke(TestCode.sealed)
        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        val sealedExample = assertNotNull(
            crescentFile.sealeds["Example"],
            "Could not find Constants object"
        )

        assertContentEquals(
            listOf(
                Struct("Thing1", listOf(Variable.Basic("name", true, Visibility.PUBLIC, Type.Basic("String"), Expression(emptyList())))),
                Struct("Thing2", listOf(Variable.Basic("id", true, Visibility.PUBLIC, Type.Basic("i32"), Expression(emptyList())))),
            ),
            sealedExample.structs,
        )

        assertContentEquals(
            listOf(
                Object("Thing3", emptyList(), emptyList(), emptyList())
            ),
            sealedExample.objects,
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
                        EnumEntry("RED", listOf(String("Red"))),
                        EnumEntry("GREEN", listOf(String("Green"))),
                        EnumEntry("BLUE", listOf(String("Blue"))),
                    ),
                ),
            ), crescentFile.enums.values,
        )

        assertContentEquals(
            listOf(
                Variable.Basic("color", true, Visibility.PUBLIC, Type.Implicit, DotChain(listOf(Identifier("Color"), IdentifierCall("random", emptyList())))),
                When(
                    Identifier("color"),
                    listOf(
                        When.Clause(
                            When.EnumShortHand("RED"),
                            Statement.Block(listOf(
                                IdentifierCall("println", listOf(String("Meow")))
                            ))
                        ),
                        When.Clause(
                            When.EnumShortHand("GREEN"),
                            Statement.Block(emptyList())
                        ),
                        When.Clause(
                            null,
                            Statement.Block(emptyList())
                        ),
                    )),
                // TODO: Encode name into a Variable instead of a bunch of tokens
                When(Expression(listOf(Identifier("name"), DotChain(listOf(Identifier("color"), Identifier("name"))), ASSIGN)), listOf(
                    When.Clause(
                        String("Red"),
                        Statement.Block(listOf(
                            IdentifierCall("println", listOf(Identifier("name"))),
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
            ),
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

        assertContentEquals(
            listOf(
                Identifier("println"),
                String("#meow"),
                Expression(listOf(I8(1), I8(1), ADD, I8(1), I8(1), DIV, I8(1), MUL, SUB, ASSIGN)),
            ),
            mainFunction.innerCode.nodes,
        )

    }

    @Test
    fun imports() {

        val tokens = CrescentLexer.invoke(TestCode.imports)
        val crescentFile = CrescentParser.invoke(Path.of("example.crescent"), tokens)

        assertContentEquals(
            listOf(
                Import("crescent.examples", "Thing"),
                Import("crescent.examples", "Thing2", "Thing3"),
                Import("crescent.examples", "*"),

                //Import("crescent.examples", "", "examples"),
                Import("", "Thing"),
                Import("", "Thing2", "Thing3"),
            ),
            crescentFile.imports
        )
    }

}