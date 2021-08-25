package me.camdenorrb.crescentvm.math

import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow
import kotlin.system.measureNanoTime

object ShuntingYard {

    //val decimalFormat = DecimalFormat("#.##")

    @JvmStatic
    fun main(args: Array<String>) {

        //println(evaluate("20.00 / 335 + 4 / 2 + 30 * (10 + 10)"))

        //val thing = "20 / 335 + 4 / 2 + 30 * (10 + 10)"

        println(evaluate("0.9346341244777236 + 0.11992406936196554 + 0.096298675640208"))
        println(0.9346341244777236 + 0.11992406936196554 + 0.096298675640208)
        //println(evaluate("10(10 + 10) + 2 - 10(10)"))
        // 10 * 20 + 2 - 100
        //

        //println(evaluate("2(2)(2 - 1) + Meow(1) / 2"))

        //println(evaluate(readLine()!!))
        //val equation = "20 / 335 + 4 / 2 + 30 * (10 + 10)"
        //benchmark(equation)
    }

    fun benchmark(equation: String) {

        repeat(1_000_000) {
            evaluate(equation)
        }

        val nanoTime = measureNanoTime {
            repeat(1_000_000) {
                evaluate(equation)
            }
        } / 1_000_000

        println("It took ${nanoTime}n/s to do the equation $equation")

    }

    fun evaluate(input: String): Double {

        // Stack -> Push/Pop
        val numberStack = LinkedList<Double>()

        invoke(input).forEach {

            if (it is Value) {
                numberStack.push(it.number)
                return@forEach
            }

            val value2 = numberStack.pop()
            val value1 = numberStack.pop()

            when (it) {
                Token.Operator.ADD -> {
                    //println("$value1 + $value2 = ${value1 + value2}")
                    numberStack.push(value1 + value2)
                }
                Token.Operator.SUB -> {
                    //println("$value1 - $value2 = ${value1 - value2}")
                    numberStack.push(value1 - value2)
                }
                Token.Operator.DIV -> {
                    //println("$value1 / $value2 = ${value1 / value2}")
                    numberStack.push(value1 / value2)
                }
                Token.Operator.MUL -> {
                    //println("$value1 * $value2 = ${value1 * value2}")
                    numberStack.push(value1 * value2)
                }
                Token.Operator.REM -> {
                    numberStack.push(value1 % value2)
                }
                Token.Operator.POW -> {
                    //println("$value1 ^ $value2 = ${value1.pow(value2)}")
                    numberStack.push(value1.pow(value2))
                }
            }
        }

        check(numberStack.size == 1) {
            "Didn't evaluate correctly, multiple answers? $numberStack"
        }

        return numberStack.first//decimalFormat.format(numberStack.first).toDouble()
    }

    fun invoke(input: String): LinkedList<Token> {

        // Queue -> Add/Remove
        // Stack -> Push/Pop

        val outputQueue   = LinkedList<Token>()
        val operatorStack = LinkedList<Token.Operator>()
        //val functionStack = LinkedList<Token.Function>()

        var lastToken: Token? = null

        SyntaxIterator(input).forEach {

            when (it) {

                is Value -> {
                    outputQueue.add(it)
                }

                is Name -> {
                    outputQueue.add(it)
                }

                Token.Operator.PARENTHESIS_OPEN -> {

                    if (lastToken is Value || lastToken == Token.Operator.PARENTHESIS_CLOSE) {
                        operatorStack.push(Token.Operator.MUL)
                    }

                    operatorStack.push(Token.Operator.PARENTHESIS_OPEN)
                }

                Token.Operator.PARENTHESIS_CLOSE -> {

                    while (operatorStack.first != Token.Operator.PARENTHESIS_OPEN) {
                        outputQueue.add(operatorStack.pop())
                    }

                    operatorStack.pop()
                }

                is Token.Operator -> {

                    while (operatorStack.isNotEmpty() && operatorStack.first.let { first -> first.precedence > it.precedence || (it != Token.Operator.PARENTHESIS_OPEN && (operatorStack.first.precedence == it.precedence && it.associativity == Token.Operator.Associativity.LEFT)) }) {
                        outputQueue.add(operatorStack.pop())
                    }

                    operatorStack.push(it)
                }
            }

            lastToken = it
        }

        while (operatorStack.isNotEmpty()) {
            outputQueue.add(operatorStack.pop())
        }

        println(outputQueue)

        return outputQueue
    }

    class SyntaxIterator(input: String) : Iterator<Token> {

        private var index = 0

        private val input = input.trim()


        override fun hasNext(): Boolean {
            return index < input.length
        }

        override fun next(): Token {

            readUntil {
                !it.isWhitespace()
            }

            val firstChar         = input[index]
            val isNumber          = firstChar == '.' || firstChar.isDigit()
            val isSymbol          = !isNumber && firstChar.isLetter().not()
            val isParenthesis     = isSymbol && firstChar == '(' || firstChar == ')'

            return when {

                isNumber -> {

                    val number = readUntil {
                        it != '.' && !it.isDigit()
                    }

                    Value(number.toDouble())
                }

                isParenthesis -> {

                    index++

                    if (firstChar == '(') {
                        Token.Operator.PARENTHESIS_OPEN
                    }
                    else {
                        Token.Operator.PARENTHESIS_CLOSE
                    }
                }

                isSymbol -> {

                    val symbol = readUntil {
                        it.isWhitespace() || it.isLetterOrDigit()
                    }

                    when (symbol) {

                        "+" -> Token.Operator.ADD
                        "-" -> Token.Operator.SUB
                        "*" -> Token.Operator.MUL
                        "/" -> Token.Operator.DIV
                        "%" -> Token.Operator.REM
                        "^" -> Token.Operator.POW
                        "[" -> Token.Operator.FUNCTION_OPEN
                        "]" -> Token.Operator.FUNCTION_CLOSE

                        else -> error("Unknown token type: '$symbol'")
                    }
                }

                else -> {

                    val name = readUntil {
                        it == '(' || it == ')' || it.isWhitespace() || it.isDigit()
                    }

                    Name(name)
                }
            }
        }

        fun readUntil(inclusive: Boolean = false, predicate: (Char) -> Boolean): String {

            val initialIndex = index

            while (index < input.length) {

                val nextChar = input[index]

                if (predicate(nextChar)) {
                    break
                }

                index++
            }

            if (inclusive && index != input.length) {
                index++
            }

            return input.substring(initialIndex, index)
        }

    }

    interface Token {

        enum class Operator(val precedence: Int, val associativity: Associativity): Token {

            ADD(2, Associativity.LEFT),
            SUB(2, Associativity.LEFT),
            MUL(3, Associativity.LEFT),
            DIV(3, Associativity.LEFT),
            REM(3, Associativity.LEFT),
            POW(4, Associativity.RIGHT),
            FUNCTION_OPEN(0, Associativity.LEFT),
            FUNCTION_CLOSE(0, Associativity.RIGHT),
            PARENTHESIS_OPEN(0, Associativity.LEFT),
            PARENTHESIS_CLOSE(0, Associativity.RIGHT);

            enum class Associativity {
                LEFT,
                RIGHT
            }
        }

        interface Function : Token {

            fun invoke(value1: Double, value2: Double)

        }

    }
}

private inline class Name(val data: String) : ShuntingYard.Token {

    override fun toString(): String {
        return data
    }

}

// Needs to be project level to inline
private inline class Value(val number: Double): ShuntingYard.Token {

    override fun toString(): String {
        return "$number"
    }

}