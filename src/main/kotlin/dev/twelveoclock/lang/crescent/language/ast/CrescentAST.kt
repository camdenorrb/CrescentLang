package dev.twelveoclock.lang.crescent.language.ast

import dev.twelveoclock.lang.crescent.language.token.CrescentToken
//import tech.poder.ir.api.CodeHolder
import java.nio.file.Path
import kotlin.math.pow


// https://github.com/cretz/kastree/blob/master/ast/ast-common/src/main/kotlin/kastree/ast/Node.kt

// TODO: Store line numbers and start/end char positions
// TODO: Make Variable a sealed class, Basic, Constant, Local
class CrescentAST {

    interface Node {

        interface Typed {

            val type: Type

        }


        data class GetCall(
            val identifier: String,
            val arguments: List<Node>
        ) : Node {

            override fun toString(): String {
                return "$identifier[${arguments.joinToString()}]"
            }

        }

        data class IdentifierCall(
            val identifier: String,
            val arguments: List<Node> = emptyList()
        ) : Node {

            override fun toString(): String {
                return "$identifier(${arguments.joinToString()})"
            }

        }

        // Should usually only represent stuff inside (,)'s or return values
        @JvmInline
        value class Expression(
	        val nodes: List<Node>,
        ) : Node {

            override fun toString(): String {
                return "Exp$nodes"
            }

        }

        @JvmInline
        value class DotChain(
            val nodes: List<Node>
        ) : Node {

            override fun toString(): String {
                return "DotChain=${nodes.joinToString(".")}"
            }

        }

        /*
        @JvmInline
        value class InstanceOf(
            val expression: Node,
        ) : Node {

            override fun toString(): String {
                return "is $expression"
            }

        }
        */

        @JvmInline
        value class Return(
	        val expression: Node,
        ) : Node {

            override fun toString(): String {
                return "-> $expression"
            }

        }

        data class Import(
            val path: String,
            val typeName: String,
            val typeAlias: String? = null,
        ) : Node

        data class Struct(
	        val name: String,
	        val variables: List<Variable.Basic>,
        ) : Node

        data class Sealed(
	        val name: String,
	        val structs: List<Struct>,
	        val objects: List<Object>
        ) : Node, Typed {

            override val type = Type.Basic(name)

        }

        data class Trait(
	        val name: String,
	        val functionTraits: List<FunctionTrait>,
        ) : Node, Typed {

            override val type = Type.Basic(name)

        }

        // TODO: Force variables to be val not var
        data class Object(
	        val name: String,
	        val variables: List<Variable.Basic>,
	        val constants: List<Variable.Constant>,
	        val functions: List<Function>,
        ) : Node, Typed {

            override val type = Type.Basic(name)

        }

        data class Impl(
            override val type: Type,
            val modifiers: List<CrescentToken.Modifier>,
            val extends: List<Type>,
            val functions: List<Function>,
        ) : Node, Typed

        data class Enum(
	        val name: String,
	        val parameters: List<Parameter>,
	        val structs: List<EnumEntry>,
        ) : Node, Typed {

            override val type = Type.Basic(name)

        }

        data class EnumEntry(
	        val name: String,
	        val arguments: List<Node>,
        ) : Node, Typed {

            override val type = Type.Basic(name)

        }

        data class FunctionTrait(
	        val name: String,
	        val params: List<Parameter>,
	        val returnType: Type,
        ) : Node

        @JvmInline
        value class Identifier(
            val name: String,
        ) : Node {

            override fun toString(): String {
                return name
            }

        }


        interface Variable : Node, Typed {

            data class Basic(
                val name: String,
                val isFinal: Boolean,
                val visibility: CrescentToken.Visibility,
                override val type: Type,
                val value: Node,
            ) : Variable {

                override fun toString(): String {
                    return "$visibility ${if (isFinal) "val" else "var"} $name: $type = $value"
                }

            }

            data class Constant(
                val name: String,
                val visibility: CrescentToken.Visibility,
                override val type: Type,
                val value: Node,
            ) : Variable {

                override fun toString(): String {
                    return "const $name: ${type::class.simpleName} = $value"
                }
            }

            data class Local(
	            val name: String,
	            override val type: Type,
	            val value: Node
            ) : Variable

        }

        data class Function(
            val name: String,
            val modifiers: List<CrescentToken.Modifier>,
            val visibility: CrescentToken.Visibility,
            val params: List<Parameter>,
            val returnType: Type,
            val innerCode: Statement.Block,
        ) : Node

        // TODO: Make a better toString
        data class File(
	        val path: Path,
	        val imports: List<Import>,
	        val structs: Map<String, Struct>,
	        val sealeds: Map<String, Sealed>,
	        val impls: Map<String, Impl>,
	        val staticImpls: Map<String, Impl>,
	        val traits: Map<String, Trait>,
	        val objects: Map<String, Object>,
	        val enums: Map<String, Enum>,
	        val variables: Map<String, Variable.Basic>,
	        val constants: Map<String, Variable.Constant>,
	        val functions: Map<String, Function>,
	        val mainFunction: Function?,
        ) : Node//, CodeHolder


        sealed class Parameter : Node {

            abstract val name: String


            data class Basic(
	            override val name: String,
	            override val type: Type,
            ) : Parameter(), Typed

            // TODO: Maybe have an implicit type from the expression
            data class WithDefault(
	            override val name: String,
	            override val type: Type,
	            val defaultValue: Expression,
            ) : Parameter(), Typed

        }

        // TODO: Add toStrings
        // TODO: Implement objects for primitive types
        interface Type : Node {

            // Should only be used for variables
            object Implicit : Type


            // Should only be used for function return types
            @JvmInline
            value class Result(val type: Type) : Type

            @JvmInline
            value class Basic(val name: String) : Type {

                override fun toString(): String {
                    return "Basic($name)"
                }

            }

            @JvmInline
            value class Array(val type: Type) : Type {

                override fun toString(): String {
                    return "Array[${type}]"
                }

            }


            companion object {

                val any = Basic("Any")

                val unit = Basic("Unit")

                val i32 = Basic("I32")

            }

            /*
            data class Generic(
                val type: Basic,
                val parameters: List<Type>,
            ) : Type() {

                override fun toString(): kotlin.String {
                    return ""
                }

            }
            */

        }


        interface Statement : Node {

            data class When(
	            val argument: Node,
	            val predicateToBlock: List<Clause>,
            ) : Statement {

                override fun toString(): String {
                    return "when (${argument}) ${predicateToBlock.joinToString(prefix = "{ ", postfix = " }")}"
                }

                // ifExpression is null when it's else
                data class Clause(val ifExpressionNode: Node?, val thenBlock: Block) : Statement {

                    override fun toString(): String {
                        return "$ifExpressionNode $thenBlock"
                    }

                }

                // .EnumName
                @JvmInline
                value class EnumShortHand(
                    val name: String,
                ) : Statement

                @JvmInline
                value class Else(
	                val thenBlock: Block,
                ) : Statement

            }

            // TODO: Add else if's, perhaps rename elseBlock to elseBlocks
            data class If(
	            val predicate: Node,
	            val block: Block,
	            val elseBlock: Block?
            ) : Statement

            data class While(
	            val predicate: Node,
	            val block: Block,
            ) : Statement

            data class For(
	            val identifiers: List<Identifier>,
	            val ranges: List<Range>,
	            val block: Block,
            ) : Statement

            // TODO: Make it a list of nodes
            @JvmInline
            value class Block(
	            val nodes: List<Node>,
            ) : Statement {

                override fun toString(): String {
                    return "{ ${nodes.joinToString()} }"
                }

            }

            data class Range(
	            val start: Node,
	            val end: Node,
            ) : Statement {

                override fun toString(): String {
                    return "$start..$end"
                }

            }

        }

        @JvmInline
        value class Array(
            //override val type: Type.Array,
	        val values: kotlin.Array<Node>,
        ) : Node/*, Typed*/ {

            /*
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Array

                if (!values.contentEquals(other.values)) return false

                return true
            }

            override fun hashCode(): Int {
                return values.contentHashCode()
            }
            */

        }

        interface Primitive : Node, Typed {

            @JvmInline
            value class Boolean(
                val data: kotlin.Boolean,
            ) : Primitive {

                override val type get() = Companion.type


                override fun toString(): kotlin.String {
                    return "$data"
                }


                companion object {

                    val type = Type.Basic("Boolean")

                }
            }

            @JvmInline
            value class String(
                val data: kotlin.String,
            ) : Primitive {

                override val type get() = Companion.type


                override fun toString(): kotlin.String {
                    return "\"$data\""
                }


                companion object {

                    val type = Type.Basic("String")

                }
            }

            @JvmInline
            value class Char(
                val data: kotlin.Char,
            ) : Primitive {

                override val type get() = Companion.type


                override fun toString(): kotlin.String {
                    return "'$data'"
                }


                companion object {

                    val type = Type.Basic("Char")

                }
            }

            interface Number : Primitive {

                @JvmInline
                value class U8(val data: UByte) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("U8")

                    }
                }

                @JvmInline
                value class U16(val data: UShort) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("U16")

                    }
                }

                @JvmInline
                value class U32(val data: UInt) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("U32")

                    }
                }

                @JvmInline
                value class U64(val data: ULong) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("U64")

                    }
                }

                @JvmInline
                value class I8(val data: Byte) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("I8")

                    }
                }

                @JvmInline
                value class I16(val data: Short) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("I16")

                    }
                }

                @JvmInline
                value class I32(val data: Int) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("I32")

                    }
                }

                @JvmInline
                value class I64(val data: Long) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("I64")

                    }
                }

                @JvmInline
                value class F32(val data: Float) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("F32")

                    }
                }

                @JvmInline
                value class F64(val data: Double) : Number {

                    override val type get() = Companion.type

                    override fun toString(): kotlin.String {
                        return "$data"
                    }

                    companion object {

                        val type = Type.Basic("F64")

                    }
                }

                fun toU8() = when (this) {
                    is U8 -> this
                    is U16 -> U8(data.toUByte())
                    is U32 -> U8(data.toUByte())
                    is U64 -> U8(data.toUByte())
                    is I8 -> U8(data.toUByte())
                    is I16 -> U8(data.toUByte())
                    is I32 -> U8(data.toUByte())
                    is I64 -> U8(data.toUByte())
                    else -> error("Can't convert $type to U8")
                }

                fun toU16() = when (this) {
                    is U8 -> U16(data.toUShort())
                    is U16 -> this
                    is U32 -> U16(data.toUShort())
                    is U64 -> U16(data.toUShort())
                    is I8 -> U16(data.toUShort())
                    is I16 -> U16(data.toUShort())
                    is I32 -> U16(data.toUShort())
                    is I64 -> U16(data.toUShort())
                    else -> error("Can't convert $type to U16")
                }

                fun toU32() = when (this) {
                    is U8 -> U32(data.toUInt())
                    is U16 -> U32(data.toUInt())
                    is U32 -> this
                    is U64 -> U32(data.toUInt())
                    is I8 -> U32(data.toUInt())
                    is I16 -> U32(data.toUInt())
                    is I32 -> U32(data.toUInt())
                    is I64 -> U32(data.toUInt())
                    else -> error("Can't convert $type to U32")
                }

                fun toU64() = when (this) {
                    is U8 -> U64(data.toULong())
                    is U16 -> U64(data.toULong())
                    is U32 -> U64(data.toULong())
                    is U64 -> this
                    is I8 -> U64(data.toULong())
                    is I16 -> U64(data.toULong())
                    is I32 -> U64(data.toULong())
                    is I64 -> U64(data.toULong())
                    else -> error("Can't convert $type to U64")
                }

                fun toI8() = when (this) {
                    is U8 -> I8(data.toByte())
                    is U16 -> I8(data.toByte())
                    is U32 -> I8(data.toByte())
                    is U64 -> I8(data.toByte())
                    is I8 -> this
                    is I16 -> I8(data.toByte())
                    is I32 -> I8(data.toByte())
                    is I64 -> I8(data.toByte())
                    is F32 -> I8(data.toInt().toByte())
                    is F64 -> I8(data.toInt().toByte())
                    else -> error("Can't convert $type to I8")
                }

                fun toI16() = when (this) {
                    is U8 -> I16(data.toShort())
                    is U16 -> I16(data.toShort())
                    is U32 -> I16(data.toShort())
                    is U64 -> I16(data.toShort())
                    is I8 -> I16(data.toShort())
                    is I16 -> this
                    is I32 -> I16(data.toShort())
                    is I64 -> I16(data.toShort())
                    is F32 -> I16(data.toInt().toShort())
                    is F64 -> I16(data.toInt().toShort())
                    else -> error("Can't convert $type to I16")
                }

                fun toI32() = when (this) {
                    is U8 -> I32(data.toInt())
                    is U16 -> I32(data.toInt())
                    is U32 -> I32(data.toInt())
                    is U64 -> I32(data.toInt())
                    is I8 -> I32(data.toInt())
                    is I16 -> I32(data.toInt())
                    is I32 -> this
                    is I64 -> I32(data.toInt())
                    is F32 -> I32(data.toInt())
                    is F64 -> I32(data.toInt())
                    else -> error("Can't convert $type to I32")
                }

                fun toI64() = when (this) {
                    is U8 -> I64(data.toLong())
                    is U16 -> I64(data.toLong())
                    is U32 -> I64(data.toLong())
                    is U64 -> I64(data.toLong())
                    is I8 -> I64(data.toLong())
                    is I16 -> I64(data.toLong())
                    is I32 -> I64(data.toLong())
                    is I64 -> this
                    is F32 -> I64(data.toLong())
                    is F64 -> I64(data.toLong())
                    else -> error("Can't convert $type to I64")
                }

                fun toF32() = when (this) {
                    is U8 -> F32(data.toFloat())
                    is U16 -> F32(data.toFloat())
                    is U32 -> F32(data.toFloat())
                    is U64 -> F32(data.toFloat())
                    is I8 -> F32(data.toFloat())
                    is I16 -> F32(data.toFloat())
                    is I32 -> F32(data.toFloat())
                    is I64 -> F32(data.toFloat())
                    is F32 -> this
                    is F64 -> F32(data.toFloat())
                    else -> error("Can't convert $type to F32")
                }

                fun toF64() = when (this) {
                    is U8 -> F64(data.toDouble())
                    is U16 -> F64(data.toDouble())
                    is U32 -> F64(data.toDouble())
                    is U64 -> F64(data.toDouble())
                    is I8 -> F64(data.toDouble())
                    is I16 -> F64(data.toDouble())
                    is I32 -> F64(data.toDouble())
                    is I64 -> F64(data.toDouble())
                    is F32 -> F64(data.toDouble())
                    is F64 -> this
                    else -> error("Can't convert $type to F64")
                }

                fun toKotlinNumber() = when (this) {
                    is U8 -> data
                    is U16 -> data
                    is U32 -> data
                    is U64 -> data
                    is I8 -> data
                    is I16 -> data
                    is I32 -> data
                    is I64 -> data
                    is F32 -> data
                    is F64 -> data
                    else -> error("Can't convert $type to F64")
                }

                fun pow(number: Number): Number {
                    return F64(toF64().data.pow(number.toF64().data))
                }

                operator fun minus(number: Number): Number = when (this) {

                    is U8 -> when (number) {
                        is U8 -> U32(this.data - number.data)
                        is U16 -> U32(this.data - number.data)
                        is U32 -> U32(this.data - number.data)
                        is U64 -> U64(this.data - number.data)
                        else -> error("Can't subtract an unsigned to a signed")
                    }
                    is U16 -> when (number) {
                        is U8 -> U32(this.data - number.data)
                        is U16 -> U32(this.data - number.data)
                        is U32 -> U32(this.data - number.data)
                        is U64 -> U64(this.data - number.data)
                        else -> error("Can't subtract an unsigned to a signed")
                    }
                    is U32 -> when (number) {
                        is U8 -> U32(this.data - number.data)
                        is U16 -> U32(this.data - number.data)
                        is U32 -> U32(this.data - number.data)
                        is U64 -> U64(this.data - number.data)
                        else -> error("Can't subtract an unsigned to a signed")
                    }
                    is U64 -> when (number) {
                        is U8 -> U64(this.data - number.data)
                        is U16 -> U64(this.data - number.data)
                        is U32 -> U64(this.data - number.data)
                        is U64 -> U64(this.data - number.data)
                        else -> error("Can't subtract an unsigned to a signed")
                    }

                    is I8 -> when (number) {
                        is I8 -> I32(this.data - number.data)
                        is I16 -> I32(this.data - number.data)
                        is I32 -> I32(this.data - number.data)
                        is I64 -> I64(this.data - number.data)
                        is F32 -> F32(this.data - number.data)
                        is F64 -> F64(this.data - number.data)
                        else -> error("Can't subtract an signed to a unsigned")
                    }
                    is I16 -> when (number) {
                        is I8 -> I32(this.data - number.data)
                        is I16 -> I32(this.data - number.data)
                        is I32 -> I32(this.data - number.data)
                        is I64 -> I64(this.data - number.data)
                        is F32 -> F32(this.data - number.data)
                        is F64 -> F64(this.data - number.data)
                        else -> error("Can't subtract an signed to a unsigned")
                    }
                    is I32 -> when (number) {
                        is I8 -> I32(this.data - number.data)
                        is I16 -> I32(this.data - number.data)
                        is I32 -> I32(this.data - number.data)
                        is I64 -> I64(this.data - number.data)
                        is F32 -> F32(this.data - number.data)
                        is F64 -> F64(this.data - number.data)
                        else -> error("Can't subtract an signed to a unsigned")
                    }
                    is I64 -> when (number) {
                        is I8 -> I64(this.data - number.data)
                        is I16 -> I64(this.data - number.data)
                        is I32 -> I64(this.data - number.data)
                        is I64 -> I64(this.data - number.data)
                        is F32 -> F32(this.data - number.data)
                        is F64 -> F64(this.data - number.data)
                        else -> error("Can't subtract an signed to a unsigned")
                    }
                    is F32 -> when (number) {
                        is I8 -> F32(this.data - number.data)
                        is I16 -> F32(this.data - number.data)
                        is I32 -> F32(this.data - number.data)
                        is I64 -> F32(this.data - number.data)
                        is F32 -> F32(this.data - number.data)
                        is F64 -> F64(this.data - number.data)
                        else -> error("Can't subtract an signed to a unsigned")
                    }
                    is F64 -> when (number) {
                        is I8 -> F64(this.data - number.data)
                        is I16 -> F64(this.data - number.data)
                        is I32 -> F64(this.data - number.data)
                        is I64 -> F64(this.data - number.data)
                        is F32 -> F64(this.data - number.data)
                        is F64 -> F64(this.data - number.data)
                        else -> error("Can't subtract an signed to a unsigned")
                    }

                    else -> error("Unknown type: $this")
                }

                operator fun plus(number: Number): Number = when (this) {

                    is U8 -> when (number) {
                        is U8 -> U32(this.data + number.data)
                        is U16 -> U32(this.data + number.data)
                        is U32 -> U32(this.data + number.data)
                        is U64 -> U64(this.data + number.data)
                        else -> error("Can't add an unsigned to a signed")
                    }
                    is U16 -> when (number) {
                        is U8 -> U32(this.data + number.data)
                        is U16 -> U32(this.data + number.data)
                        is U32 -> U32(this.data + number.data)
                        is U64 -> U64(this.data + number.data)
                        else -> error("Can't add an unsigned to a signed")
                    }
                    is U32 -> when (number) {
                        is U8 -> U32(this.data + number.data)
                        is U16 -> U32(this.data + number.data)
                        is U32 -> U32(this.data + number.data)
                        is U64 -> U64(this.data + number.data)
                        else -> error("Can't add an unsigned to a signed")
                    }
                    is U64 -> when (number) {
                        is U8 -> U64(this.data + number.data)
                        is U16 -> U64(this.data + number.data)
                        is U32 -> U64(this.data + number.data)
                        is U64 -> U64(this.data + number.data)
                        else -> error("Can't add an unsigned to a signed")
                    }

                    is I8 -> when (number) {
                        is I8 -> I32(this.data + number.data)
                        is I16 -> I32(this.data + number.data)
                        is I32 -> I32(this.data + number.data)
                        is I64 -> I64(this.data + number.data)
                        is F32 -> F32(this.data + number.data)
                        is F64 -> F64(this.data + number.data)
                        else -> error("Can't add an signed to a unsigned")
                    }
                    is I16 -> when (number) {
                        is I8 -> I32(this.data + number.data)
                        is I16 -> I32(this.data + number.data)
                        is I32 -> I32(this.data + number.data)
                        is I64 -> I64(this.data + number.data)
                        is F32 -> F32(this.data + number.data)
                        is F64 -> F64(this.data + number.data)
                        else -> error("Can't add an signed to a unsigned")
                    }
                    is I32 -> when (number) {
                        is I8 -> I32(this.data + number.data)
                        is I16 -> I32(this.data + number.data)
                        is I32 -> I32(this.data + number.data)
                        is I64 -> I64(this.data + number.data)
                        is F32 -> F32(this.data + number.data)
                        is F64 -> F64(this.data + number.data)
                        else -> error("Can't add an signed to a unsigned")
                    }
                    is I64 -> when (number) {
                        is I8 -> I64(this.data + number.data)
                        is I16 -> I64(this.data + number.data)
                        is I32 -> I64(this.data + number.data)
                        is I64 -> I64(this.data + number.data)
                        is F32 -> F32(this.data + number.data)
                        is F64 -> F64(this.data + number.data)
                        else -> error("Can't add an signed to a unsigned")
                    }
                    is F32 -> when (number) {
                        is I8 -> F32(this.data + number.data)
                        is I16 -> F32(this.data + number.data)
                        is I32 -> F32(this.data + number.data)
                        is I64 -> F32(this.data + number.data)
                        is F32 -> F32(this.data + number.data)
                        is F64 -> F64(this.data + number.data)
                        else -> error("Can't add an signed to a unsigned")
                    }
                    is F64 -> when (number) {
                        is I8 -> F64(this.data + number.data)
                        is I16 -> F64(this.data + number.data)
                        is I32 -> F64(this.data + number.data)
                        is I64 -> F64(this.data + number.data)
                        is F32 -> F64(this.data + number.data)
                        is F64 -> F64(this.data + number.data)
                        else -> error("Can't add an signed to a unsigned")
                    }

                    else -> error("Unknown type: $this")
                }

                operator fun div(number: Number): Number = when (this) {

                    is U8 -> when (number) {
                        is U8 -> U32(this.data / number.data)
                        is U16 -> U32(this.data / number.data)
                        is U32 -> U32(this.data / number.data)
                        is U64 -> U64(this.data / number.data)
                        else -> error("Can't divide an unsigned to a signed")
                    }
                    is U16 -> when (number) {
                        is U8 -> U32(this.data / number.data)
                        is U16 -> U32(this.data / number.data)
                        is U32 -> U32(this.data / number.data)
                        is U64 -> U64(this.data / number.data)
                        else -> error("Can't divide an unsigned to a signed")
                    }
                    is U32 -> when (number) {
                        is U8 -> U32(this.data / number.data)
                        is U16 -> U32(this.data / number.data)
                        is U32 -> U32(this.data / number.data)
                        is U64 -> U64(this.data / number.data)
                        else -> error("Can't divide an unsigned to a signed")
                    }
                    is U64 -> when (number) {
                        is U8 -> U64(this.data / number.data)
                        is U16 -> U64(this.data / number.data)
                        is U32 -> U64(this.data / number.data)
                        is U64 -> U64(this.data / number.data)
                        else -> error("Can't divide an unsigned to a signed")
                    }

                    is I8 -> when (number) {
                        is I8 -> I32(this.data / number.data)
                        is I16 -> I32(this.data / number.data)
                        is I32 -> I32(this.data / number.data)
                        is I64 -> I64(this.data / number.data)
                        is F32 -> F32(this.data / number.data)
                        is F64 -> F64(this.data / number.data)
                        else -> error("Can't divide an signed to a unsigned")
                    }
                    is I16 -> when (number) {
                        is I8 -> I32(this.data / number.data)
                        is I16 -> I32(this.data / number.data)
                        is I32 -> I32(this.data / number.data)
                        is I64 -> I64(this.data / number.data)
                        is F32 -> F32(this.data / number.data)
                        is F64 -> F64(this.data / number.data)
                        else -> error("Can't divide an signed to a unsigned")
                    }
                    is I32 -> when (number) {
                        is I8 -> I32(this.data / number.data)
                        is I16 -> I32(this.data / number.data)
                        is I32 -> I32(this.data / number.data)
                        is I64 -> I64(this.data / number.data)
                        is F32 -> F32(this.data / number.data)
                        is F64 -> F64(this.data / number.data)
                        else -> error("Can't divide an signed to a unsigned")
                    }
                    is I64 -> when (number) {
                        is I8 -> I64(this.data / number.data)
                        is I16 -> I64(this.data / number.data)
                        is I32 -> I64(this.data / number.data)
                        is I64 -> I64(this.data / number.data)
                        is F32 -> F32(this.data / number.data)
                        is F64 -> F64(this.data / number.data)
                        else -> error("Can't divide an signed to a unsigned")
                    }
                    is F32 -> when (number) {
                        is I8 -> F32(this.data / number.data)
                        is I16 -> F32(this.data / number.data)
                        is I32 -> F32(this.data / number.data)
                        is I64 -> F32(this.data / number.data)
                        is F32 -> F32(this.data / number.data)
                        is F64 -> F64(this.data / number.data)
                        else -> error("Can't divide an signed to a unsigned")
                    }
                    is F64 -> when (number) {
                        is I8 -> F64(this.data / number.data)
                        is I16 -> F64(this.data / number.data)
                        is I32 -> F64(this.data / number.data)
                        is I64 -> F64(this.data / number.data)
                        is F32 -> F64(this.data / number.data)
                        is F64 -> F64(this.data / number.data)
                        else -> error("Can't divide an signed to a unsigned")
                    }

                    else -> error("Unknown type: $this")
                }

                operator fun times(number: Number): Number = when (this) {

                    is U8 -> when (number) {
                        is U8 -> U32(this.data * number.data)
                        is U16 -> U32(this.data * number.data)
                        is U32 -> U32(this.data * number.data)
                        is U64 -> U64(this.data * number.data)
                        else -> error("Can't multiply an unsigned to a signed")
                    }
                    is U16 -> when (number) {
                        is U8 -> U32(this.data * number.data)
                        is U16 -> U32(this.data * number.data)
                        is U32 -> U32(this.data * number.data)
                        is U64 -> U64(this.data * number.data)
                        else -> error("Can't multiply an unsigned to a signed")
                    }
                    is U32 -> when (number) {
                        is U8 -> U32(this.data * number.data)
                        is U16 -> U32(this.data * number.data)
                        is U32 -> U32(this.data * number.data)
                        is U64 -> U64(this.data * number.data)
                        else -> error("Can't multiply an unsigned to a signed")
                    }
                    is U64 -> when (number) {
                        is U8 -> U64(this.data * number.data)
                        is U16 -> U64(this.data * number.data)
                        is U32 -> U64(this.data * number.data)
                        is U64 -> U64(this.data * number.data)
                        else -> error("Can't multiply an unsigned to a signed")
                    }

                    is I8 -> when (number) {
                        is I8 -> I32(this.data * number.data)
                        is I16 -> I32(this.data * number.data)
                        is I32 -> I32(this.data * number.data)
                        is I64 -> I64(this.data * number.data)
                        is F32 -> F32(this.data * number.data)
                        is F64 -> F64(this.data * number.data)
                        else -> error("Can't multiply an signed to a unsigned")
                    }
                    is I16 -> when (number) {
                        is I8 -> I32(this.data * number.data)
                        is I16 -> I32(this.data * number.data)
                        is I32 -> I32(this.data * number.data)
                        is I64 -> I64(this.data * number.data)
                        is F32 -> F32(this.data * number.data)
                        is F64 -> F64(this.data * number.data)
                        else -> error("Can't multiply an signed to a unsigned")
                    }
                    is I32 -> when (number) {
                        is I8 -> I32(this.data * number.data)
                        is I16 -> I32(this.data * number.data)
                        is I32 -> I32(this.data * number.data)
                        is I64 -> I64(this.data * number.data)
                        is F32 -> F32(this.data * number.data)
                        is F64 -> F64(this.data * number.data)
                        else -> error("Can't multiply an signed to a unsigned")
                    }
                    is I64 -> when (number) {
                        is I8 -> I64(this.data * number.data)
                        is I16 -> I64(this.data * number.data)
                        is I32 -> I64(this.data * number.data)
                        is I64 -> I64(this.data * number.data)
                        is F32 -> F32(this.data * number.data)
                        is F64 -> F64(this.data * number.data)
                        else -> error("Can't multiply an signed to a unsigned")
                    }
                    is F32 -> when (number) {
                        is I8 -> F32(this.data * number.data)
                        is I16 -> F32(this.data * number.data)
                        is I32 -> F32(this.data * number.data)
                        is I64 -> F32(this.data * number.data)
                        is F32 -> F32(this.data * number.data)
                        is F64 -> F64(this.data * number.data)
                        else -> error("Can't multiply an signed to a unsigned")
                    }
                    is F64 -> when (number) {
                        is I8 -> F64(this.data * number.data)
                        is I16 -> F64(this.data * number.data)
                        is I32 -> F64(this.data * number.data)
                        is I64 -> F64(this.data * number.data)
                        is F32 -> F64(this.data * number.data)
                        is F64 -> F64(this.data * number.data)
                        else -> error("Can't multiply an signed to a unsigned")
                    }

                    else -> error("Unknown type: $this")
                }

                operator fun rem(number: Number): Number = when (this) {

                    is U8 -> when (number) {
                        is U8 -> U32(this.data % number.data)
                        is U16 -> U32(this.data % number.data)
                        is U32 -> U32(this.data % number.data)
                        is U64 -> U64(this.data % number.data)
                        else -> error("Can't take the remainder of an unsigned to a signed")
                    }
                    is U16 -> when (number) {
                        is U8 -> U32(this.data % number.data)
                        is U16 -> U32(this.data % number.data)
                        is U32 -> U32(this.data % number.data)
                        is U64 -> U64(this.data % number.data)
                        else -> error("Can't take the remainder of an unsigned to a signed")
                    }
                    is U32 -> when (number) {
                        is U8 -> U32(this.data % number.data)
                        is U16 -> U32(this.data % number.data)
                        is U32 -> U32(this.data % number.data)
                        is U64 -> U64(this.data % number.data)
                        else -> error("Can't take the remainder of an unsigned to a signed")
                    }
                    is U64 -> when (number) {
                        is U8 -> U64(this.data % number.data)
                        is U16 -> U64(this.data % number.data)
                        is U32 -> U64(this.data % number.data)
                        is U64 -> U64(this.data % number.data)
                        else -> error("Can't take the remainder of an unsigned to a signed")
                    }

                    is I8 -> when (number) {
                        is I8 -> I32(this.data % number.data)
                        is I16 -> I32(this.data % number.data)
                        is I32 -> I32(this.data % number.data)
                        is I64 -> I64(this.data % number.data)
                        is F32 -> F32(this.data % number.data)
                        is F64 -> F64(this.data % number.data)
                        else -> error("Can't take the remainder of an signed to a unsigned")
                    }
                    is I16 -> when (number) {
                        is I8 -> I32(this.data % number.data)
                        is I16 -> I32(this.data % number.data)
                        is I32 -> I32(this.data % number.data)
                        is I64 -> I64(this.data % number.data)
                        is F32 -> F32(this.data % number.data)
                        is F64 -> F64(this.data % number.data)
                        else -> error("Can't take the remainder of an signed to a unsigned")
                    }
                    is I32 -> when (number) {
                        is I8 -> I32(this.data % number.data)
                        is I16 -> I32(this.data % number.data)
                        is I32 -> I32(this.data % number.data)
                        is I64 -> I64(this.data % number.data)
                        is F32 -> F32(this.data % number.data)
                        is F64 -> F64(this.data % number.data)
                        else -> error("Can't take the remainder of an signed to a unsigned")
                    }
                    is I64 -> when (number) {
                        is I8 -> I64(this.data % number.data)
                        is I16 -> I64(this.data % number.data)
                        is I32 -> I64(this.data % number.data)
                        is I64 -> I64(this.data % number.data)
                        is F32 -> F32(this.data % number.data)
                        is F64 -> F64(this.data % number.data)
                        else -> error("Can't take the remainder of an signed to a unsigned")
                    }
                    is F32 -> when (number) {
                        is I8 -> F32(this.data % number.data)
                        is I16 -> F32(this.data % number.data)
                        is I32 -> F32(this.data % number.data)
                        is I64 -> F32(this.data % number.data)
                        is F32 -> F32(this.data % number.data)
                        is F64 -> F64(this.data % number.data)
                        else -> error("Can't take the remainder of an signed to a unsigned")
                    }
                    is F64 -> when (number) {
                        is I8 -> F64(this.data % number.data)
                        is I16 -> F64(this.data % number.data)
                        is I32 -> F64(this.data % number.data)
                        is I64 -> F64(this.data % number.data)
                        is F32 -> F64(this.data % number.data)
                        is F64 -> F64(this.data % number.data)
                        else -> error("Can't take the remainder of an signed to a unsigned")
                    }

                    else -> error("Unknown type: $this")
                }


                companion object {

                    fun from(number: kotlin.Number): Number = when (number) {
                        is Byte -> I8(number)
                        is Short -> I16(number)
                        is Int -> I32(number)
                        is Long -> I64(number)
                        is Float -> F32(number)
                        is Double -> F64(number)
                        else -> error("Unknown type: ${number::class.simpleName}")
                    }

                }
            }
        }
    }
}