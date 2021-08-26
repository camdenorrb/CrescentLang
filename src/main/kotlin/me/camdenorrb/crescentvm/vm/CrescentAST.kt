package me.camdenorrb.crescentvm.vm

import java.nio.file.Path


// https://github.com/cretz/kastree/blob/master/ast/ast-common/src/main/kotlin/kastree/ast/Node.kt

// TODO: Store line numbers and start/end char positions
class CrescentAST {

    interface Node {

        sealed class Primitive {

            @JvmInline
            value class Number(
                val data: kotlin.Number,
            ) : Node {

                override fun toString(): kotlin.String {
                    return "$data"
                }

            }

            @JvmInline
            value class Boolean(
                val data: kotlin.Boolean,
            ) : Node {

                override fun toString(): kotlin.String {
                    return "$data"
                }

            }

            @JvmInline
            value class String(
                val data: kotlin.String,
            ) : Node {

                override fun toString(): kotlin.String {
                    return "\"$data\""
                }

            }

            @JvmInline
            value class Char(
                val data: kotlin.Char,
            ) : Node {

                override fun toString(): kotlin.String {
                    return "'$data'"
                }

            }

        }

        data class Array(
            val type: Type.Array,
            val values: kotlin.Array<Node>,
        ) : Node {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Array

                if (type != other.type) return false
                if (!values.contentEquals(other.values)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = type.hashCode()
                result = 31 * result + values.contentHashCode()
                return result
            }

        }

        data class GetCall(
            val identifier: String,
            val arguments: List<Expression>
        ) : Node {

            override fun toString(): String {
                return "[${arguments.joinToString()}]"
            }

        }

        data class IdentifierCall(
            val identifier: String,
            val arguments: List<Expression>
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

        // TONO: Make a class called MathExpression and just store a list of tokens, or just retrieve the list of tokens in the parser and do shunting yard
        // Use Expression and just do a shunting yard algorithm on it, each token should have a precedence or -1 by default
        /*
        data class Operation(
            val first: Node,
            val operator: CrescentToken.Operator,
            val second: Node,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$first ${operator.literal} $second"
            }

        }
        */

        @JvmInline
        value class DotChain(
            val nodes: List<Node>
        ) : Node {

            override fun toString(): String {
                return "DotChain=${nodes.joinToString(".")}"
            }

        }

        // TODO: Make the token implement Node
        @JvmInline
        value class Operator(
            val operator: CrescentToken.Operator
        ) : Node {

            override fun toString(): String {
                return "$operator"
            }

        }

        @JvmInline
        value class InstanceOf(
            val expression: Expression,
        ) : Node {

            override fun toString(): String {
                return "is $expression"
            }

        }

        @JvmInline
        value class Return(
            val expression: Expression,
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
            val variables: List<Variable>,
        ) : Node

        data class Sealed(
            val name: String,
            val structs: List<Struct>,
            val objects: List<Object>
        ) : Node

        data class Trait(
            val name: String,
            val functionTraits: List<FunctionTrait>,
        ) : Node

        // TODO: Force variables to be val not var
        data class Object(
            val name: String,
            val variables: List<Variable>,
            val constants: List<Constant>,
            val functions: List<Function>,
        ) : Node

        data class Impl(
            val type: Type,
            val modifiers: List<CrescentToken.Modifier>,
            val extends: List<Type>,
            val functions: List<Function>,
        ) : Node

        data class Enum(
            val name: String,
            val parameters: List<Parameter>,
            val structs: List<EnumEntry>,
        ) : Node

        data class EnumEntry(
            val name: String,
            val arguments: List<Expression>,
        ) : Node

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

        data class Constant(
            val name: String,
            val visibility: CrescentToken.Visibility,
            val type: Type,
            val value: Node,
        ) : Node {

            override fun toString(): String {
                return "const $name: ${type::class.simpleName} = $value"
            }

        }

        data class Variable(
            val name: String,
            val isFinal: Boolean,
            val visibility: CrescentToken.Visibility,
            val type: Type,
            val value: Node,
        ) : Node {

            override fun toString(): String {
                return "$visibility ${if (isFinal) "val" else "var"} $name: ${type::class.simpleName} = $value"
            }

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
            val variables: Map<String, Variable>,
            val constants: Map<String, Constant>,
            val functions: Map<String, Function>,
            val mainFunction: Function?,
        ) : Node


        sealed class Parameter : Node {

            abstract val name: String


            data class Basic(
                override val name: String,
                val type: Type,
            ) : Parameter()

            data class WithDefault(
                override val name: String,
                val defaultValue: Expression,
            ) : Parameter()

        }

        // TODO: Add toStrings
        interface Type : Node {

            // Should only be used for variables
            object Implicit : Type

            // No return type
            object Unit : Type


            // Should only be used for function return types
            @JvmInline
            value class Result(val type: Type) : Type

            @JvmInline
            value class Basic(val name: String) : Type {

                override fun toString(): String {
                    return name
                }

            }

            @JvmInline
            value class Array(val type: Type) : Type {

                override fun toString(): String {
                    return "[${type}]"
                }

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
                val argument: Expression,
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
                val predicate: Expression,
                val block: Block,
                val elseBlock: Block?
            ) : Statement

            data class While(
                val predicate: Expression,
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

    }

}