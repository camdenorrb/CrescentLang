package me.camdenorrb.crescentvm.vm

import java.nio.file.Path


// https://github.com/cretz/kastree/blob/master/ast/ast-common/src/main/kotlin/kastree/ast/Node.kt
class CrescentAST {

    sealed class Node {

        data class Number(
            val data: kotlin.Number,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$data"
            }

        }

        data class Boolean(
            val data: kotlin.Boolean,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$data"
            }

        }

        data class String(
            val data: kotlin.String,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "\"$data\""
            }

        }

        data class Char(
            val data: kotlin.Char,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "'$data'"
            }

        }

        data class GetCall(
            val identifier: kotlin.String,
            val arguments: List<Expression>
        ) : Node() {

            override fun toString(): kotlin.String {
                return "[${arguments.joinToString()}]"
            }

        }

        data class FunctionCall(
            val identifier: kotlin.String,
            val arguments: List<Expression>
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$identifier(${arguments.joinToString()})"
            }

        }

        // Should usually only represent stuff inside (,)'s or return values
        data class Expression(
            val nodes: List<Node>,
        ) : Node() {

            override fun toString(): kotlin.String {
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

        data class DotChain(
            val nodes: List<Node>
        ) : Node() {

            override fun toString(): kotlin.String {
                return "DotChain=${nodes.joinToString(".")}"
            }

        }

        data class Operator(
            val operator: CrescentToken.Operator
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$operator"
            }

        }

        data class InstanceOf(
            val expression: Expression,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "is $expression"
            }

        }

        data class Return(
            val expression: Expression,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "-> $expression"
            }

        }

        data class Import(
            val path: kotlin.String,
            val typeName: kotlin.String,
            val typeAlias: kotlin.String? = null,
        ) : Node()

        data class Struct(
            val name: kotlin.String,
            val variables: List<Variable>,
        ) : Node()

        data class Sealed(
            val name: kotlin.String,
            val structs: List<Struct>
        )

        data class Trait(
            val name: kotlin.String,
            val functionTraits: List<FunctionTrait>,
        ) : Node()

        data class Object(
            val name: kotlin.String,
            val variables: List<Variable>,
            val functions: List<Function>,
            val constants: List<Constant>,
        ) : Node()

        data class Impl(
            val type: Type,
            val modifiers: List<CrescentToken.Modifier>,
            val functions: List<Function>,
            val extends: List<Type>,
        ) : Node()

        data class Enum(
            val name: kotlin.String,
            val parameters: List<Parameter>,
            val structs: List<EnumEntry>,
        ) : Node()

        data class EnumEntry(
            val name: kotlin.String,
            val arguments: List<Expression>,
        ) : Node()

        data class FunctionTrait(
            val name: kotlin.String,
            val params: List<Parameter>,
            val returnType: Type,
        ) : Node()

        data class Identifier(
            val name: kotlin.String,
        ) : Node() {

            override fun toString(): kotlin.String {
                return name
            }

        }

        data class Constant(
            val name: kotlin.String,
            val visibility: CrescentToken.Visibility,
            val type: Type,
            val value: Node,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "const $name: ${type::class.simpleName} = $value"
            }

        }

        data class Variable(
            val name: kotlin.String,
            val isFinal: kotlin.Boolean,
            val visibility: CrescentToken.Visibility,
            val type: Type,
            val value: Node,
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$visibility ${if (isFinal) "val" else "var"} $name: ${type::class.simpleName} = $value"
            }

        }

        // Hacky fix for readExpressionNode, change in the future
        data class Variables(
            val data: List<Variable>
        ) : Node() {

            override fun toString(): kotlin.String {
                return "$data"
            }

        }

        data class Function(
            val name: kotlin.String,
            val modifiers: List<CrescentToken.Modifier>,
            val visibility: CrescentToken.Visibility,
            val params: List<Parameter>,
            val returnType: Type,
            val innerCode: Statement.Block,
        ) : Node()

        // TODO: Make a better toString
        data class File(
            val path: Path,
            val imports: List<Import>,
            val structs: List<Struct>,
            val sealeds: List<Sealed>,
            val impls: List<Impl>,
            val traits: List<Trait>,
            val objects: List<Object>,
            val enums: List<Enum>,
            val variables: List<Variable>,
            val constants: List<Constant>,
            val functions: List<Function>,
            val mainFunction: Function?,
        ) : Node()


        sealed class Parameter : Node() {

            abstract val name: kotlin.String


            data class Basic(
                override val name: kotlin.String,
                val type: Type,
            ) : Parameter()

            data class WithDefault(
                override val name: kotlin.String,
                val defaultValue: Expression,
            ) : Parameter()

        }

        // TODO: Add toStrings
        sealed class Type : Node() {

            // Should only be used for variables
            object Implicit : Type()

            // No return type
            object Unit : Type()


            // Should only be used for function return types
            data class Result(val type: Type) : Type()

            data class Basic(val name: kotlin.String) : Type() {

                override fun toString(): kotlin.String {
                    return name
                }

            }

            data class Array(val type: Type) : Type() {

                override fun toString(): kotlin.String {
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


        sealed class Statement : Node() {

            data class When(
                val argument: Expression,
                val predicateToBlock: List<Clause>,
            ) : Statement() {

                override fun toString(): kotlin.String {
                    return "when (${argument}) ${predicateToBlock.joinToString(prefix = "{ ", postfix = " }")}"
                }

                // ifExpression is null when it's else
                data class Clause(val ifExpressionNode: Node?, val thenBlock: Block) : Statement() {

                    override fun toString(): kotlin.String {
                        return "$ifExpressionNode $thenBlock"
                    }

                }

            }

            data class Else(
                val block: Block,
            ) : Statement()

            data class If(
                val predicate: Expression,
                val block: Block,
            ) : Statement()

            data class While(
                val predicate: Expression,
                val block: Block,
            ) : Statement()

            data class For(
                val variable: Variable,
                val predicate: Expression,
                val block: Block,
            ) : Statement()

            // TODO: Make it a list of nodes
            data class Block(
                val nodes: List<Node>,
            ) : Statement() {

                override fun toString(): kotlin.String {
                    return "{ ${nodes.joinToString()} }"
                }

            }

            data class Range(
                val start: Node,
                val end: Node,
            ) {

                override fun toString(): kotlin.String {
                    return "$start..$end"
                }

            }

        }

    }

}