package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.project.checkEquals

class CrescentVM {

	fun invoke(file: CrescentAST.Node.File) {
		file.mainFunction?.innerCode?.expressions?.forEach { expression ->
			runExpression(expression)
		}
	}

	fun runExpression(expression: CrescentAST.Node.Expression): List<CrescentAST.Node> {

		val returnValues = mutableListOf<CrescentAST.Node>()

		var lastIdentifier = ""

		expression.nodes.forEach { node ->
			when (node) {

				is CrescentAST.Node.Return -> {
					return runExpression(node.expression)
				}

				is CrescentAST.Node.Identifier -> {
					lastIdentifier = node.name
				}

				is CrescentAST.Node.Call -> {
					when (lastIdentifier) {

						"println" -> {
							checkEquals(node.arguments.size, 1)
							println(node.arguments.joinToString { it.nodes.joinToString { "${convertDataNode(it)}" } })
						}

						else -> {}
					}
				}

				else -> {}

			}
		}

		return returnValues
	}

	fun convertDataNode(node: CrescentAST.Node): Any {
		return when (node) {

			is CrescentAST.Node.Char -> {
				node.data
			}

			is CrescentAST.Node.String -> {
				node.data
			}

			is CrescentAST.Node.Number -> {
				node.data
			}

			is CrescentAST.Node.Boolean -> {
				node.data
			}

			else -> error("Unknown node $node")
		}
	}

}