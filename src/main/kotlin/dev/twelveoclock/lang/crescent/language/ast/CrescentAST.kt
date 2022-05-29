package dev.twelveoclock.lang.crescent.language.ast

//import tech.poder.ir.api.CodeHolder
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import java.nio.file.Path


// https://github.com/cretz/kastree/blob/master/ast/ast-common/src/main/kotlin/kastree/ast/Node.kt

// TODO: Store line numbers and start/end char positions
// TODO: Make Variable a sealed class, Basic, Constant, Local
class CrescentAST {

	interface Node {

		interface Scope

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
			val arguments: List<Node> = emptyList(),
			var callee: Node? = null
		) : Node {

			override fun toString(): String {
				return "$identifier(${arguments.joinToString()})"
			}

		}

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
		) : Node

		data class Trait(
			val name: String,
			val functionTraits: List<FunctionTrait>,
		) : Node

		// TODO: Force variables to be val not var
		data class Object(
			val name: String,
			val variables: Map<String, Variable>,
			val constants: Map<String, Variable>,
			val functions: Map<String, Function>,
		) : Node, Scope

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
			val arguments: List<Node>,
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


		// Can check if constant based on scope and type
		data class Variable(
			val name: String,
			val type: Type,
			val value: Node,
			val isFinal: Boolean,
			val scope: Scope,
			// Nullable only if local
			val visibility: CrescentToken.Visibility?,
		)


		data class Function(
			val name: String,
			val modifiers: List<CrescentToken.Modifier>,
			val visibility: CrescentToken.Visibility,
			val params: List<Parameter>,
			val returnType: Type,
			val innerCode: Statement.Block,
		) : Node, Scope

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
		) : Node, Scope

		class Parameter(
			val name: String,
			val type: Type,
			val defaultValue: Expression?,
		): Node


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
		) : Node {

			override fun toString(): String {
				return values.contentToString()
			}

		}

		interface Primitive : Node {

			@JvmInline
			value class Boolean(
				val data: kotlin.Boolean,
			) : Primitive {

				override fun toString(): kotlin.String {
					return "$data"
				}
			}

			@JvmInline
			value class String(
				val data: kotlin.String,
			) : Primitive {

				override fun toString(): kotlin.String {
					return "\"$data\""
				}

			}

			@JvmInline
			value class Char(
				val data: kotlin.Char,
			) : Primitive {

				override fun toString(): kotlin.String {
					return "'$data'"
				}

			}

			@JvmInline
			value class Number(
				val data: kotlin.Number
			) {

				override fun toString(): kotlin.String {
					return "$data"
				}

			}
		}

	}
}