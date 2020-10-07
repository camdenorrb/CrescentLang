package me.camdenorrb.crescentvm.vm

// TODO: Make a way to reconstruct the code through .toString
sealed class CrescentToken {

    data class Import(
        val path: String
    ) : CrescentToken()

    data class Struct(
        val name: String
    ) : CrescentToken()

    data class Object(
        val name: String
    )

    data class Trait(
        val name: String
    ) : CrescentToken()

    data class Annotation(
        val text: String
    ) : CrescentToken()

    data class Function(
        val isOverride: Boolean,
        val parameters: List<Parameter>
    ) : CrescentToken()

    data class FunctionCall(
        val parameters: List<Value>
    ) : CrescentToken()

    /*
    data class Parameter(
        val value: Value
    ) : CrescentToken()
    */

    data class Parameter(
        val name: String,
        val dataType: CrescentDataType
    ) : CrescentToken()

    data class Value(
        val value: String,
        val type: CrescentDataType
    ) : CrescentToken()

    data class Expression(
        val token: CrescentOperator,
        val value1: Value,
        val value2: Value
    ) : CrescentToken()

}