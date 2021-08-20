package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentToken.*
import org.junit.Test
import kotlin.test.assertContentEquals

// TODO: Add impl
internal class CrescentLexerTests {

    @Test
    fun helloWorld() {

        val tokens = CrescentLexer.invoke(
            """
            fun main {
                println("Hello World")
            }
            """.trimIndent()
        )

        assertContentEquals(
            listOf(
                Statement.FUN, Key("main"), Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Hello World"), Parenthesis.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
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

        assertContentEquals(
            listOf(
                Statement.FUN, Key("main"), Parenthesis.OPEN, Key("args"), Operator.TYPE_PREFIX, SquareBracket.OPEN, Key("String"), SquareBracket.CLOSE, Parenthesis.CLOSE, Bracket.OPEN,
                Statement.IF, Parenthesis.OPEN, Key("args"), SquareBracket.OPEN, Data.Number(0.0), SquareBracket.CLOSE, Operator.EQUALS_COMPARE, Data.String("true"), Parenthesis.CLOSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Statement.ELSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Hiss"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
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

        assertContentEquals(
            listOf(
                Statement.FUN, Key("main"), Bracket.OPEN,
                Variable.VAL, Key("input"), Operator.ASSIGN, Key("readBoolean"), Parenthesis.OPEN, Data.String("Enter a boolean value [true/false]"), Parenthesis.CLOSE,
                Statement.IF, Parenthesis.OPEN, Key("input"), Parenthesis.CLOSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Statement.ELSE, Bracket.OPEN,
                Key("println"), Parenthesis.OPEN, Data.String("Hiss"), Parenthesis.CLOSE,
                Bracket.CLOSE,
                Bracket.CLOSE,
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

        //println(tokens)

        assertContentEquals(
            listOf(
                Statement.FUN, Key("main"), Bracket.OPEN,
                Variable.VAL, Key("input1"), Operator.ASSIGN, Key("readDouble"), Parenthesis.OPEN, Data.String("Enter your first number"), Parenthesis.CLOSE,
                Variable.VAL, Key("input2"), Operator.ASSIGN, Key("readDouble"), Parenthesis.OPEN, Data.String("Enter your second number"), Parenthesis.CLOSE,
                Variable.VAL, Key("operation"), Operator.ASSIGN, Key("readLine"), Parenthesis.OPEN, Data.String("Enter a operation [+, -, *, /]"), Parenthesis.CLOSE,
                Variable.VAL, Key("result"), Operator.ASSIGN, Statement.WHEN, Parenthesis.OPEN, Key("operation"), Parenthesis.CLOSE, Bracket.OPEN,
                Data.Char('+'), Operator.RETURN, Key("input1"), Operator.ADD, Key("input2"),
                Data.Char('-'), Operator.RETURN, Key("input1"), Operator.SUB, Key("input2"),
                Data.Char('*'), Operator.RETURN, Key("input1"), Operator.MUL, Key("input2"),
                Data.Char('/'), Operator.RETURN, Key("input1"), Operator.DIV, Key("input2"),
                Bracket.CLOSE,
                Key("println"), Parenthesis.OPEN, Key("result"), Parenthesis.CLOSE,
                Bracket.CLOSE,
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
                Variable.CONST, Key("thing"), Operator.ASSIGN, Data.String("Meow"),
                Type.OBJECT, Key("Constants"), Bracket.OPEN,
                Variable.CONST, Key("thing"), Operator.ASSIGN, Data.String("Meow"),
                Bracket.CLOSE,
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
                Statement.FUN, Key("main"), Bracket.OPEN,
                Key("println"), Parenthesis.OPEN,
                Parenthesis.OPEN, Data.Number(1.0), Operator.ADD, Data.Number(1.0), Parenthesis.CLOSE,
                Operator.ADD, Data.Number(1.0), Operator.DIV, Data.Number(10.0), Operator.ADD, Data.Number(1000.0), Operator.MUL, Data.Number(10.0),
                Operator.DIV, Data.Number(10.0), Operator.POW, Data.Number(10.0),
                Parenthesis.CLOSE,
                Bracket.CLOSE,
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
                Type.SEALED, Key("Example"), Bracket.OPEN,
                Type.STRUCT, Key("Thing1"), Parenthesis.OPEN, Variable.VAL, Key("name"), Operator.TYPE_PREFIX, Key("String"), Parenthesis.CLOSE,
                Type.STRUCT, Key("Thing2"), Parenthesis.OPEN, Variable.VAL, Key("id"), Operator.TYPE_PREFIX, Key("i32"), Parenthesis.CLOSE,
                Bracket.CLOSE,
            ),
            tokens
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
            
                    else -> {}
                }
            
                when(name = color.name) {
            
                    "Red"   -> println(name)
                    "Green" -> {}
            
                    else -> {}
                }
            
            }
            """.trimIndent()
        )

        assertContentEquals(
            listOf(

                Type.ENUM, Key("Color"), Parenthesis.OPEN, Key("name"), Operator.TYPE_PREFIX, Key("String"), Parenthesis.CLOSE, Bracket.OPEN,
                Key("RED"), Parenthesis.OPEN, Data.String("Red"), Parenthesis.CLOSE,
                Key("GREEN"), Parenthesis.OPEN, Data.String("Green"), Parenthesis.CLOSE,
                Key("BLUE"), Parenthesis.OPEN, Data.String("Blue"), Parenthesis.CLOSE,
                Bracket.CLOSE,

                Statement.FUN, Key("main"), Bracket.OPEN,

                Data.Comment(".random() will be built into the Enum type implementation"),
                Variable.VAL, Key("color"), Operator.ASSIGN, Key("Color"), Operator.DOT, Key("random"), Parenthesis.OPEN, Parenthesis.CLOSE,
                Data.Comment("Shows off cool Enum shorthand for when statements"),

                Statement.WHEN, Parenthesis.OPEN, Key("color"), Parenthesis.CLOSE, Bracket.OPEN,
                Operator.INSTANCE_OF, Operator.DOT, Key("RED"), Operator.RETURN, Bracket.OPEN, Key("println"), Parenthesis.OPEN, Data.String("Meow"), Parenthesis.CLOSE, Bracket.CLOSE,
                Operator.INSTANCE_OF, Operator.DOT, Key("GREEN"), Operator.RETURN, Bracket.OPEN, Bracket.CLOSE,
                Statement.ELSE, Operator.RETURN, Bracket.OPEN, Bracket.CLOSE,
                Bracket.CLOSE,

                Statement.WHEN, Parenthesis.OPEN, Key("name"), Operator.ASSIGN, Key("color"), Operator.DOT, Key("name"), Parenthesis.CLOSE, Bracket.OPEN,
                Data.String("Red"), Operator.RETURN, Key("println"), Parenthesis.OPEN, Key("name"), Parenthesis.CLOSE,
                Data.String("Green"), Operator.RETURN, Bracket.OPEN, Bracket.CLOSE,
                Statement.ELSE, Operator.RETURN, Bracket.OPEN, Bracket.CLOSE,
                Bracket.CLOSE,

                Bracket.CLOSE,
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

        assertContentEquals(
            listOf(
                Data.Comment("Project level comment"),
                Statement.FUN, Key("main"), Bracket.OPEN,
                Key("println"), Data.Comment("(\"Meow\")"),
                Data.Comment("Meow"),
                Data.Comment("Meow"),
                Data.String("#meow"),
                Data.Number(1.0), Operator.ADD, Data.Comment("Meow"),
                Data.Number(1.0), Operator.SUB, Data.Comment("Meow"),
                Data.Number(1.0), Operator.DIV, Data.Comment("Meow"),
                Data.Number(1.0), Operator.MUL, Data.Comment("Meow"),
                Data.Number(1.0), Operator.ASSIGN, Data.Comment("Meow"),
                Data.Comment("}")
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
                Data.Comment("Current idea, Package -> Type"),
                Statement.IMPORT, Key("crescent"), Operator.DOT, Key("examples"), Operator.IMPORT_SEPARATOR, Key("Thing"),
                Statement.IMPORT, Key("crescent"), Operator.DOT, Key("examples"), Operator.AS, Key("examples"),
                Data.Comment("Short hand method (If in same package)"),
                Statement.IMPORT, Operator.IMPORT_SEPARATOR, Key("Thing")
            ),
            tokens
        )
    }

}