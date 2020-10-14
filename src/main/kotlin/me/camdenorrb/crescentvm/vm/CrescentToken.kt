package me.camdenorrb.crescentvm.vm

// TODO: Make a way to reconstruct the code through .toString
interface CrescentToken {

    data class Key(
        val string: kotlin.String
    ) : CrescentToken

    data class String(
        val string: kotlin.String
    ) : CrescentToken

    data class Comment(
        val string: kotlin.String
    ) : CrescentToken

    enum class Statement : CrescentToken {
        IMPORT,
        WHILE,
        WHEN,
        FOR,
        IF,
        FUN,
        OVERRIDE
    }

    enum class Parenthesis : CrescentToken {
        OPEN,
        CLOSE
    }

    enum class Bracket : CrescentToken {
        OPEN,
        CLOSE
    }

    enum class ArrayDeclaration : CrescentToken {
        OPEN,
        CLOSE
    }

    enum class Type : CrescentToken {
        STRUCT,
        IMPL,
        TRAIT,
        OBJECT
    }

    enum class Variable : CrescentToken {
        VAL,
        VAR
    }

    enum class Operator(val literal: kotlin.String) : CrescentToken {
        NOT("!"),
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        REM("%"),
        ASSIGN("="),
        ADD_ASSIGN("+="),
        SUB_ASSIGN("-="),
        MUL_ASSIGN("*="),
        DIV_ASSIGN("/="),
        REM_ASSIGN("%="),
        OR_COMPARE("||"),
        AND_COMPARE("&&"),
        EQUALS_COMPARE("=="),
        LESSER_EQUALS_COMPARE("<="),
        GREATER_EQUALS_COMPARE(">="),
        EQUALS_REFERENCE_COMPARE("==="),
        NOT_EQUALS_COMPARE("!="),
        NOT_EQUALS_REFERENCE_COMPARE("!=="),
        CONTAINS("in"),
        RANGE(".."),
        TYPE_PREFIX(":"),
        RETURN("->"),
        OPTIONAL("?")
    }

    enum class Primitive : CrescentToken {

        I8,
        I16,
        I32,
        I64,

        U8,
        U16,
        U32,
        U64,

        F32,
        F64,

        // Other types built into the language
        //Array,
        //List,
        //Map,
        //Tensor,
        //Char,
        //Text,
        //Fun,
        //Time,
        //Struct
    }


    /*
    interface Value


    data class Import(
        val path: String
    ) : CrescentToken()

    data class Struct(
        val name: String,
        val parameters: List<StructParameter>
    ) : CrescentToken()

    data class Impl(
        val forStruct: String,
        val isStatic: Boolean,
        val functions: List<ImplFunction>
    ) : CrescentToken()

    data class Sealed(
        val name: String,
        val structs: List<Struct>,
        val impls: List<Impl>
    ) : CrescentToken()

    data class Object(
        val name: String,
        val functions: List<ImplFunction>
    ) : CrescentToken()

    data class Trait(
        val name: String,
        val functions: List<TraitFunction>
    ) : CrescentToken()

    data class Annotation(
        val text: String
    ) : CrescentToken()

    data class TraitFunction(
        val name: String,
        val parameters: List<FunctionParameter>
    ) : CrescentToken()

    data class ImplFunction(
        val name: String,
        val parameters: List<FunctionParameter>,
        val tokens: List<CrescentToken>
    ) : CrescentToken()

    // Either pointing to a Struct or a Trait or a Generic
    data class Type(
        val name: String
    ) : CrescentToken()

    data class StructParameter(
        val isMutable: Boolean,
        val name: String,
        val type: Type
    ) : CrescentToken()

    data class FunctionParameter(
        val name: String,
        val type: Type
    ) : CrescentToken()

    data class ImplFunctionCall(
        val name: String,
        val parameters: List<Value>
    ) : CrescentToken(), Value

    /*
    data class Expression(
        val text: String
    ) : CrescentToken(), Value
    */

    data class Equation(
        val token: CrescentOperator,
        val value1: Value,
        val value2: Value
    ) : CrescentToken(), Value

    data class Variable(
        val name: String,
        val variableValue: Value
    ) : CrescentToken()


    sealed class Statement : CrescentToken() {

        data class If(
            val predicate: Value
        )

        data class For(
            val predicate: Value
        ) : Statement()

        data class While(
            val predicate: Value
        ) : Statement()

        data class When(
            val statements: Map<Value, ImplFunction>
        ) : Statement()

        data class Error(
            val error: Value
        ) : Statement()

    }*/

}