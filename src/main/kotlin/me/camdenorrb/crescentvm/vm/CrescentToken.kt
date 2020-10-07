package me.camdenorrb.crescentvm.vm

// TODO: Make a way to reconstruct the code through .toString
sealed class CrescentToken {

    data class Import(
        val path: String
    ) : CrescentToken()

    data class Struct(
        val name: String,
        val arguments: List<StructParameter>
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
    )

    data class Object(
        val name: String
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

    data class FunctionCall(
        val parameters: List<Value>
    ) : CrescentToken()

    // Either pointing to a Struct or a Trait or a Generic
    data class Type(
        val name: String
    ) : CrescentToken()

    // Use Value instead
    /*
    data class Parameter(
        val value: Value
    ) : CrescentToken()
    */

    data class StructParameter(
        val isMutable: Boolean,
        val name: String,
        val type: Type
    ) : CrescentToken()

    data class FunctionParameter(
        val name: String,
        val type: Type
    ) : CrescentToken()

    data class Value(
        val value: String,
        val type: CrescentPrimitive
    ) : CrescentToken()

    data class Expression(
        val token: CrescentOperator,
        val value1: Value,
        val value2: Value
    ) : CrescentToken()

}