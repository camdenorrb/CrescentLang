package me.camdenorrb.crescentvm.vm

// TODO: Make a way to reconstruct the code through .toString
// TODO: Add ++ and --
// TODO: Add Operator keyword
// TODO: Error keyword
// TODO: Maybe remove majority of these and let the parser determine it, EX: Import, while
interface CrescentToken {

    data class Key(
        val string: kotlin.String
    ) : CrescentToken

    data class Number(
        val number: kotlin.Number
    ) : CrescentToken

    // TODO: Take in expressions
    data class String(
        val kotlinString: kotlin.String
    ) : CrescentToken

    data class Comment(
        val string: kotlin.String
    ) : CrescentToken


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

    enum class Variable : CrescentToken {
        VAL,
        VAR
    }

    enum class Type : CrescentToken {
        STRUCT,
        IMPL,
        TRAIT,
        OBJECT,
        ENUM
    }

    enum class Statement : CrescentToken {
        IMPORT,
        WHILE,
        WHEN,
        FOR,
        IF,
        FUN
    }

    enum class Modifier : CrescentToken {
        ASYNC,
        OVERRIDE,
        OPERATOR,
        INLINE,
        PUBLIC,
        PRIVATE,
        STATIC,
        INFIX
    }


    enum class Operator(val literal: kotlin.String) : CrescentToken {
        //NEW_LINE("\n"),
        NOT("!"),
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        POW("^"),
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
        VARIABLE_TYPE_PREFIX(":"),
        RETURN("->"),
        RESULT("?"),
        COMMA(",")
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

}