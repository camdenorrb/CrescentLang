package dev.twelveoclock.lang.crescent.language.token

interface Token {

	// Used only by the token iterator
	object Empty : Token

	interface Operator : Token

	@JvmInline
	value class Identifier(
		val string: String
	) : Token

	@JvmInline
	value class Comment(
		val string: String
	) : Token

	interface Primitive : Token {

		// TODO: Take in expressions
		@JvmInline
		value class String(
			val kotlinString: kotlin.String
		) : Primitive

		@JvmInline
		value class Boolean(
			val kotlinBoolean: kotlin.Boolean
		) : Primitive

		@JvmInline
		value class Char(
			val kotlinChar: kotlin.Char
		) : Primitive

		@JvmInline
		value class Number(
			val number: kotlin.Number
		) : Primitive {

			override fun toString(): kotlin.String {
				return "Number(${number} ${number::class.simpleName})"
			}

		}

	}

}

