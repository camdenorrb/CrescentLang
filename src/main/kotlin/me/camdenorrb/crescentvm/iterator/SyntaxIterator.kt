package me.camdenorrb.crescentvm.iterator

import me.camdenorrb.crescentvm.lexerold.CrescentToken
import java.io.File

// Maybe turn into a SyntaxOutputStream?
class SyntaxIterator(private val input: String) {

    private var index = 0


    fun nextToken(): CrescentToken {

        readUntil {
            !it.isWhitespace()
        }

        val isNumber = input[index].let { it == '.' || it.isDigit() }
        val isSymbol = !isNumber && input[index].isLetter().not()

        val value = readUntil {
            when {
                isNumber -> {
                    !it.isDigit()
                }
                isSymbol -> {
                    it.isWhitespace() || it.isLetterOrDigit()
                }
                else -> {
                    it.isWhitespace()
                }
            }
        }

        when (value) {

            "+" -> Token.Operator.ADD
            "-" -> Token.Operator.SUB
            "*" -> Token.Operator.MUL
            "/" -> Token.Operator.DIV
            "%" -> Token.Operator.REM

            else -> error("Unknown token type: $value")
        }

    }


    fun readUntil(vararg chars: Char, inclusive: Boolean = false): String {

        var minIndex = chars
            .map { input.indexOf(it, index) }
            .firstOrNull { it != -1 }
            ?: input.length - 1

        if (inclusive) {
            minIndex++
        }

        return input.substring(index, minIndex).apply {
            index = minIndex
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

    // TODO: Move this out of tokenizer
    fun toStringWithWASMHeaders(): String {

    }


    interface Token {

        data class Complex(
            val name: String,
            val file: File,
            val imports: List<Import>,
            val annotations: List<Annotation>,
            val innerClasses: List<Class>,
            val innerInterfaces: List<Interface>
        ) : Token

        // TODO: Make complex
        data class Annotation(
            val text: String
        ) : Token

        data class Import(
            val moduleInfo: ModuleInfo
        ) : Token

        data class Function(
            val name: String,
            val visibility: VisibilityModifier,
            val parameters: List<ParameterInfo>
        ) : Token

        data class FunctionCall(
            val visibility: VisibilityModifier,
            val parameters: List<Parameter>
        ) : Token

        data class ParameterInfo(
            val name: String,
            val dataType: DataType
        ) : Token

        data class Parameter(
            val value: Value
        ) : Token

        data class Value(
            val value: String,
            val type: DataType
        ) : Token

        data class Expression(
            val token: Operator,
            val values: List<Value>
        ) : Token

        enum class Operator(val expected: String) : Token {
            NOT("!"),
            ADD("+"),
            SUB("-"),
            MUL("*"),
            DIV("/"),
            REM("%"),
            OR_COMPARE("||"),
            AND_COMPARE("&&"),
            REM_EQUALS("%="),
            NOT_EQUALS_COMPARE("!=")
        }

        // https://doc.rust-lang.org/reference/visibility-and-privacy.html
        enum class VisibilityModifier : Token {
            PUBLIC,
            PUBLIC_IN_PATH,
            PUBLIC_CRATE,
            PUBLIC_SUPER,
            PUBLIC_SELF,
            PRIVATE  // Default for most cases, fuck Rust
        }

    }

    interface ReturnType

    enum class DataType : ReturnType {
        BOOL,
        CHAR,
        I8,
        I16,
        I32,
        I64,
        ISIZE,
        U8,
        U16,
        U32,
        U64,
        USIZE,
        F32,
        F64,
        ARRAY,
        SLICE,
        STR,
        TUPLE
    }

}

// Read token
// Read next thing


}