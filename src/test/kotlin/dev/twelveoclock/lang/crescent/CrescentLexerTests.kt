package dev.twelveoclock.lang.crescent

import dev.twelveoclock.lang.crescent.data.TestCode
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Operator.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Statement.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Type.*
import dev.twelveoclock.lang.crescent.language.token.CrescentToken.Variable.*
import dev.twelveoclock.lang.crescent.lexers.CrescentLexer
import kotlin.test.Test
import kotlin.test.assertContentEquals

internal class CrescentLexerTests {

    @Test
    fun helloWorld() {

        val tokens = CrescentLexer.invoke(TestCode.helloWorlds)

        assertContentEquals(
            listOf(
                FUN, Key("main"), Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Hello World"), Parenthesis.CLOSE,
                Key("println"), *Array(2) { Parenthesis.OPEN }, Data.String("Hello World"), *Array(2) { Parenthesis.CLOSE },
                Key("println"), *Array(10) { Parenthesis.OPEN }, Data.String("Hello World"), *Array(10) { Parenthesis.CLOSE },
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun argsHelloWorld() {

        //val tokens = CrescentLexer.invoke(TestCode.helloWorlds)

    }

    @Test
    fun ifStatement() {

        val tokens = CrescentLexer.invoke(TestCode.ifStatement)

        assertContentEquals(
            listOf(
                FUN, Key("main"), Parenthesis.OPEN, Key("args"), TYPE_PREFIX, SquareBracket.OPEN, Key("String"), SquareBracket.CLOSE, Parenthesis.CLOSE, Bracket.OPEN,
                IF, Parenthesis.OPEN, Key("args"), SquareBracket.OPEN, Data.Number(0.toByte()), SquareBracket.CLOSE, EQUALS_COMPARE, Data.String("true"), Parenthesis.CLOSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                ELSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Hiss"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun ifInputStatement() {

        val tokens = CrescentLexer.invoke(TestCode.ifInputStatement)

        assertContentEquals(
            listOf(
                FUN, Key("main"), Bracket.OPEN,
                VAL, Key("input"), ASSIGN, Key("readBoolean"), Parenthesis.OPEN, Data.String("Enter a boolean value [true/false]"), Parenthesis.CLOSE,
                IF, Parenthesis.OPEN, Key("input"), Parenthesis.CLOSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                ELSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Hiss"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun stringInterpolation() {

        val tokens = CrescentLexer.invoke(TestCode.stringInterpolation)

        assertContentEquals(
            listOf(
                FUN, Key("main"), Bracket.OPEN,

                VAL, Key("x"), COMMA, Key("y"), COMMA, Key("z"), ASSIGN, Data.Number(0.toByte()),

                Key("println"), Parenthesis.OPEN, Data.String("\$x\$y\$z"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Data.String("Hello \$x\$y\$z Hello"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Data.String("Hello \$x Hello \$y Hello \$z Hello"), Parenthesis.CLOSE,

                Key("println"), Parenthesis.OPEN, Data.String("\${x}\${y}\${z}"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Data.String("Hello \${x}\${y}\${z} Hello"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Data.String("Hello \${x}Hello\${y}Hello\${z} Hello"), Parenthesis.CLOSE,

                Data.Comment("Should printout a dollar sign"),
                Key("println"), Parenthesis.OPEN, Data.String("\${'$'}"), Parenthesis.CLOSE,

                Data.Comment("Should println dollar sign next to the letter x"),
                Key("println"), Parenthesis.OPEN, Data.String("\\\$x"), Parenthesis.CLOSE,

                Key("println"), Parenthesis.OPEN, Data.String("$ x"), Parenthesis.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun forLoop() {

        val tokens = CrescentLexer.invoke(TestCode.forLoop1)

        assertContentEquals(
            listOf(
                FUN, Key("main"), Bracket.OPEN,

                VAL, Key("x"), COMMA, Key("y"), COMMA, Key("z"), ASSIGN, Data.Number(0.toByte()),
                Key("println"), Parenthesis.OPEN, Data.String("\$x\$y\$z"), Parenthesis.CLOSE,

                FOR, Key("x"), COMMA, Key("y"), COMMA, Key("z"), CONTAINS, Data.Number(0.toByte()), RANGE_TO, Data.Number(10.toByte()), Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("\$x\$y\$z"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                FOR, Key("x"), COMMA, Key("y"), COMMA, Key("z"), CONTAINS, Data.Number(0.toByte()), RANGE_TO, Data.Number(10.toByte()), COMMA, Data.Number(0.toByte()), RANGE_TO, Data.Number(10.toByte()), COMMA, Data.Number(0.toByte()), RANGE_TO, Data.Number(10.toByte()), Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("\$x\$y\$z"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                Key("println"), Parenthesis.OPEN, Data.String("Hello World"), Parenthesis.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun whileLoop() {

        val tokens = CrescentLexer.invoke(TestCode.whileLoop)

        assertContentEquals(
            listOf(
                FUN, Key("main"), Bracket.OPEN,

                VAR, Key("x"), ASSIGN, Data.Number(1.toByte()),

                WHILE, Parenthesis.OPEN, Key("x"), LESSER_EQUALS_COMPARE, Data.Number(10.toByte()), Parenthesis.CLOSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Key("x"), Parenthesis.CLOSE,
                Key("x"), ADD_ASSIGN, Data.Number(1.toByte()),
                Bracket.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun calculator() {

        val tokens = CrescentLexer.invoke(TestCode.calculator)

        assertContentEquals(
            listOf(
                FUN, Key("main"), Bracket.OPEN,
                VAL, Key("input1"), ASSIGN, Key("readDouble"), Parenthesis.OPEN, Data.String("Enter your first number"), Parenthesis.CLOSE,
                VAL, Key("input2"), ASSIGN, Key("readDouble"), Parenthesis.OPEN, Data.String("Enter your second number"), Parenthesis.CLOSE,
                VAL, Key("operation"), ASSIGN, Key("readLine"), Parenthesis.OPEN, Data.String("Enter an operation [+, -, *, /]"), Parenthesis.CLOSE,
                VAL, Key("result"), ASSIGN, WHEN, Parenthesis.OPEN, Key("operation"), Parenthesis.CLOSE, Bracket.OPEN,
                Data.Char('+'), RETURN, Key("input1"), ADD, Key("input2"),
                Data.Char('-'), RETURN, Key("input1"), SUB, Key("input2"),
                Data.Char('*'), RETURN, Key("input1"), MUL, Key("input2"),
                Data.Char('/'), RETURN, Key("input1"), DIV, Key("input2"),
                Bracket.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("result"), Parenthesis.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun constantsAndObject() {

        val tokens = CrescentLexer.invoke(TestCode.constantsAndObject)

        assertContentEquals(
            listOf(

                CONST, Key("thing1"), ASSIGN, Data.String("Mew"),

                OBJECT, Key("Constants"), Bracket.OPEN,
                CONST, Key("thing2"), ASSIGN, Data.String("Meow"),
                FUN, Key("printThing"), Parenthesis.OPEN, Parenthesis.CLOSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Key("thing1"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("thing2"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,

                FUN, Key("main"), Bracket.OPEN,
                Key("Constants"), DOT, Key("printThings"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Bracket.CLOSE
            ),
            tokens
        )
    }

    @Test
    fun impl() {

        val tokens = CrescentLexer.invoke(TestCode.impl)

        assertContentEquals(
            listOf(

                STRUCT, Key("Example"), Parenthesis.OPEN,
                VAL, Key("aNumber"), TYPE_PREFIX, Key("Int"), Data.Comment("New lines makes commas redundant"),
                VAL, Key("aValue1"), Key("aValue2"), ASSIGN, Data.String(""), Data.Comment("Multi declaration of same type, can all be set to one or multiple default values"),
                Parenthesis.CLOSE,

                IMPL, Key("Example"), Bracket.OPEN,
                Data.Comment("All implementation methods"),
                FUN, Key("printValues"), Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Key("aNumber"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("aValue1"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("aValue2"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,

                Data.Comment("Can't use self in static syntax"),
                IMPL, Modifier.STATIC, Key("Example"), Bracket.OPEN,
                FUN, Key("add"), Parenthesis.OPEN, Key("value1"), Key("value2"), TYPE_PREFIX, Key("Int"), Parenthesis.CLOSE, RETURN, Key("Int"), Bracket.OPEN,
                RETURN, Key("value1"), ADD, Key("value2"),
                Bracket.CLOSE,
                FUN, Key("sub"), Parenthesis.OPEN, Key("value1"), Key("value2"), TYPE_PREFIX, Key("Int"), Parenthesis.CLOSE, RETURN, Key("Int"), Bracket.OPEN,
                RETURN, Key("value1"), SUB, Key("value2"),
                Bracket.CLOSE,
                Bracket.CLOSE,

                FUN, Key("main"), Bracket.OPEN,
                VAL, Key("example"), ASSIGN, Key("Example"), Parenthesis.OPEN, Data.Number(1.toByte()), COMMA, Data.String("Meow"), COMMA, Data.String("Mew"), Parenthesis.CLOSE,
                Key("example"), DOT, Key("printValues"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("example"), DOT, Key("aNumber"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("example"), DOT, Key("aValue1"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("example"), DOT, Key("aValue2"), Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("Example"), DOT, Key("add"), Parenthesis.OPEN, Data.Number(1.toByte()), COMMA, Data.Number(2.toByte()), Parenthesis.CLOSE, Parenthesis.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("Example"), DOT, Key("sub"), Parenthesis.OPEN, Data.Number(1.toByte()), COMMA, Data.Number(2.toByte()), Parenthesis.CLOSE, Parenthesis.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
        )
    }


    @Test
    fun math() {

        val tokens = CrescentLexer.invoke(TestCode.math)

        assertContentEquals(
            listOf(
                FUN, Key("main"), Bracket.OPEN,

                Key("println"), Parenthesis.OPEN,
                Parenthesis.OPEN, Data.Number(1.0.toInt().toByte()), ADD, Data.Number(1.toByte()), Parenthesis.CLOSE,
                ADD, Data.Number(1.0.toInt().toByte()), DIV, Data.Number(10.0.toInt().toByte()), ADD, Data.Number(1000.0.toInt().toShort()), MUL, Data.Number(10.0.toInt().toByte()),
                DIV, Data.Number(11.0.toInt().toByte()), POW, Data.Number(10.0.toInt().toByte()),
                Parenthesis.CLOSE,

                Key("println"), Parenthesis.OPEN,
                Data.Number(4.toByte()), MUL, Parenthesis.OPEN, Data.Number(3.toByte()), Parenthesis.CLOSE, ADD, Data.Number(1.toByte()),
                Parenthesis.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun sealed() {

        val tokens = CrescentLexer.invoke(TestCode.sealed)

        assertContentEquals(
            listOf(
                SEALED, Key("Example"), Bracket.OPEN,
                STRUCT, Key("Thing1"), Parenthesis.OPEN, VAL, Key("name"), TYPE_PREFIX, Key("String"), Parenthesis.CLOSE,
                STRUCT, Key("Thing2"), Parenthesis.OPEN, VAL, Key("id"), TYPE_PREFIX, Key("i32"), Parenthesis.CLOSE,
                OBJECT, Key("Thing3"),
                Bracket.CLOSE,
            ),
            tokens
        )
    }

    @Test
    fun enum() {

        val tokens = CrescentLexer.invoke(TestCode.enum)

        assertContentEquals(
            listOf(

                ENUM, Key("Color"), Parenthesis.OPEN, Key("name"), TYPE_PREFIX, Key("String"), Parenthesis.CLOSE, Bracket.OPEN,
                Key("RED"), Parenthesis.OPEN, Data.String("Red"), Parenthesis.CLOSE,
                Key("GREEN"), Parenthesis.OPEN, Data.String("Green"), Parenthesis.CLOSE,
                Key("BLUE"), Parenthesis.OPEN, Data.String("Blue"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                FUN, Key("main"), Bracket.OPEN,

                Data.Comment(".random() will be built into the Enum type implementation"),
                VAL, Key("color"), ASSIGN, Key("Color"), DOT, Key("random"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Data.Comment("Shows off cool Enum shorthand for when statements"),

                WHEN, Parenthesis.OPEN, Key("color"), Parenthesis.CLOSE, Bracket.OPEN,
                DOT, Key("RED"), RETURN, Bracket.OPEN, Key("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE, Bracket.CLOSE,
                DOT, Key("GREEN"), RETURN, Bracket.OPEN, Bracket.CLOSE,
                ELSE, RETURN, Bracket.OPEN, Bracket.CLOSE,
                Bracket.CLOSE,

                WHEN, Parenthesis.OPEN, Key("name"), ASSIGN, Key("color"), DOT, Key("name"), Parenthesis.CLOSE, Bracket.OPEN,
                Data.String("Red"), RETURN, Key("println"), Parenthesis.OPEN, Key("name"), Parenthesis.CLOSE,
                Data.String("Green"), RETURN, Bracket.OPEN, Bracket.CLOSE,
                ELSE, RETURN, Bracket.OPEN, Bracket.CLOSE,
                Bracket.CLOSE,

                Bracket.CLOSE,
            ),
            tokens
        )

    }

    @Test
    fun comments() {

        val tokens = CrescentLexer.invoke(TestCode.comments)

        assertContentEquals(
            listOf(
                Data.Comment("Project level comment"),
                FUN, Key("main"), Bracket.OPEN,
                Key("println"), Data.Comment("(\"Meow\")"),
                Data.Comment("Meow"),
                Data.Comment("Meow"),
                Data.String("#meow"),
                Data.Number(1.toByte()), ADD, Data.Comment("Meow"),
                Data.Number(1.toByte()), SUB, Data.Comment("Meow"),
                Data.Number(1.toByte()), DIV, Data.Comment("Meow"),
                Data.Number(1.toByte()), MUL, Data.Comment("Meow"),
                Data.Number(1.toByte()), ASSIGN, Data.Comment("Meow"),
                Data.Comment("}")
            ),
            tokens
        )
    }

    @Test
    fun imports() {

        val tokens = CrescentLexer.invoke(TestCode.imports)

        assertContentEquals(
            listOf(
                Data.Comment("Current idea, Package -> Type"),
                IMPORT, Key("crescent"), DOT, Key("examples"), IMPORT_SEPARATOR, Key("Thing"),
                IMPORT, Key("crescent"), DOT, Key("examples"), IMPORT_SEPARATOR, Key("Thing2"), AS, Key("Thing3"),
                IMPORT, Key("crescent"), DOT, Key("examples"), IMPORT_SEPARATOR, MUL,

                Data.Comment("import crescent.examples as examples"),//IMPORT, Key("crescent"), DOT, Key("examples"), AS, Key("examples"),

                Data.Comment("Short hand method (If in same package)"),
                IMPORT, IMPORT_SEPARATOR, Key("Thing"),
                IMPORT, IMPORT_SEPARATOR, Key("Thing2"), AS, Key("Thing3")
            ),
            tokens
        )
    }

}