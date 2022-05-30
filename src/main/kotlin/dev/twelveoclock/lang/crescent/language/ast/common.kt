package dev.twelveoclock.lang.crescent.language.ast

import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import kotlin.reflect.KClass

interface Node

// This should not be a parameter, it can be figured out by the compiler
interface Scope


@Target(AnnotationTarget.FIELD)
annotation class Parse(

	// Prefix tokens that should be skipped and expected
	val expectPrefix: kotlin.Array<KClass<out CrescentToken>> = [],

	// Suffix tokens that should be skipped and expected
	val expectSuffix: kotlin.Array<KClass<out CrescentToken>> = [],

	// Used to annotate fields that are only set in certain scopes
	val forScope: kotlin.Array<KClass<out Scope>> = [],

	// Used to annotate fields that are only set in other scopes
	val unlessScope: kotlin.Array<KClass<out Scope>> = [],

)


// TODO: Make it a list of nodes
@JvmInline
value class Block(
	val nodes: List<Node>,
) {

	override fun toString(): String {
		return "{ ${nodes.joinToString()} }"
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

@JvmInline
value class Identifier(
	val name: String,
) : Node {

	override fun toString(): String {
		return name
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
		val data: kotlin.Number,
	) : Primitive {

		override fun toString(): kotlin.String {
			return "$data"
		}

	}

}
