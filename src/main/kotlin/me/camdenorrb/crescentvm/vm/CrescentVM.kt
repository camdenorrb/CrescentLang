package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.project.checkEquals
import java.util.*

class CrescentVM(val files: List<CrescentAST.Node.File>, val mainFunction: CrescentAST.Node.Function) {

	val globalFunctions = files.flatMap { it.functions }.associateBy { it.name }


	fun invoke() {
		runFunction(mainFunction)
	}


	fun runFunction(function: CrescentAST.Node.Function): CrescentAST.Node {

		// TODO: Have a stack
		// TODO: Last expression acts as return
		function.innerCode.nodes.forEach { node ->
			when (node) {
				is CrescentAST.Node.FunctionCall -> {
					runFunctionCall(node)
				}
				is CrescentAST.Node.Expression -> {
					runExpression(node)
				}
			}
		}

		// TODO: Make this meaningful
		return CrescentAST.Node.Type.Unit
	}

	fun runNode(node: CrescentAST.Node) {

	}

	// TODO: Take in a stack or something
	fun runExpression(expression: CrescentAST.Node.Expression): CrescentAST.Node {

		val stack = LinkedList<CrescentAST.Node>()

		expression.nodes.forEachIndexed { index, node ->
			when (node) {

				is CrescentAST.Node.String,
				is CrescentAST.Node.Number,
				is CrescentAST.Node.Char,
				is CrescentAST.Node.Boolean,
				-> {
					// If is last node
					stack.push(node)
					/*
					if (index + 1 == expression.nodes.size) {
						return node
					}
					*/
				}

				is CrescentAST.Node.Return -> {
					return runExpression(node.expression)
				}

				is CrescentAST.Node.FunctionCall -> {
					runFunctionCall(node)
				}

				else -> {}
			}
		}

		return CrescentAST.Node.Type.Unit
	}

	fun runFunctionCall(node: CrescentAST.Node.FunctionCall): CrescentAST.Node {

		when (node.identifier) {

			"println" -> {
				checkEquals(node.arguments.size, 1)
				println(runExpression(node.arguments[0]).asString())
			}

			else -> {

				val function = checkNotNull(globalFunctions[node.identifier]) {
					"Unknown function: ${node.identifier}(${node.arguments.map { runExpression(it) }})"
				}

				return runFunction(function)
			}

		}

		return CrescentAST.Node.Type.Unit
	}

	fun CrescentAST.Node.asString(): String {
		return when (this) {

			is CrescentAST.Node.String -> {
				this.data
			}

			is CrescentAST.Node.Type -> {
				"$this"
			}

			is CrescentAST.Node.Char -> {
				"${this.data}"
			}

			is CrescentAST.Node.Number -> {
				"${this.data}"
			}

			is CrescentAST.Node.Boolean -> {
				"${this.data}"
			}

			else -> {
				// TODO: Attempt to find a toString()
				error("Unknown node $this")
			}

		}
	}


	data class FunctionContext(
		val variables: MutableMap<String, CrescentAST.Node.Variable>
	)

}