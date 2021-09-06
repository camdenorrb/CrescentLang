package me.camdenorrb.crescentvm.iterator

import me.camdenorrb.crescentvm.language.ast.CrescentAST

// TODO: Make a peeking iterator interface
class PeekingNodeIterator(val input: List<CrescentAST.Node>) : Iterator<CrescentAST.Node> {

	@PublishedApi
	internal var index = 0


	override fun hasNext(): Boolean {
		return index < input.size
	}

	override fun next(): CrescentAST.Node {
		return input[index++]
	}

	fun back(): CrescentAST.Node {
		return input[--index]
	}


	fun peekNext(amount: Int = 1): CrescentAST.Node? {
		return input.getOrNull(index + (amount - 1))
	}

	fun peekBack(amount: Int = 1): CrescentAST.Node? {
		return input.getOrNull(index - amount)
	}


	inline fun nextUntil(predicate: (CrescentAST.Node) -> Boolean): List<CrescentAST.Node> {

		val nodes = mutableListOf<CrescentAST.Node>()

		while (index < input.size && !predicate(input[index])) {
			nodes += input[index]
			index++
		}

		return nodes
	}

	inline fun peekBackUntil(predicate: (CrescentAST.Node) -> Boolean): List<CrescentAST.Node> {

		var currentIndex = index - 1
		val result = mutableListOf<CrescentAST.Node>()

		while (currentIndex > 0) {

			val node = input[currentIndex--]

			if (predicate(node)) {
				break
			}

			result += node
		}

		return result
	}

}