package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.project.checkEquals
import me.camdenorrb.crescentvm.vm.CrescentAST.Node
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.Primitive
import java.util.*

class CrescentVM(val files: List<Node.File>, val mainFile: Node.File) {

	fun invoke(args: List<String> = emptyList()) {

		val mainFunction = checkNotNull(mainFile.mainFunction)

		if (mainFunction.params.isEmpty()) {
			runFunction(mainFile, mainFunction, emptyList())
		}
		else {
			runFunction(
				mainFile,
				mainFunction,
				listOf(Node.Array(Node.Type.Array(Node.Type.Basic("String")), args.map { Primitive.String(it) }.toTypedArray()))
			)
		}
	}

	fun runFunction(holder: Node, function: Node.Function, args: List<Node>): Node {

		// TODO: Account for default params
		checkEquals(function.params.size, args.size)

		val paramsToValue = mutableMapOf<String, Node>()

		function.params.forEachIndexed { index, parameter ->
			checkIsSameType(parameter, args[index])
			paramsToValue[parameter.name] = args[index]
		}

		val context = FunctionContext(
			holder,
			paramsToValue
		)

		// TODO: Have a stack
		// TODO: Last expression acts as return
		function.innerCode.nodes.forEach { node ->
			when (node) {

				is Node.FunctionCall -> {
					runFunctionCall(node, context)
				}

				is Node.Expression -> {
					runExpression(node, context)
				}

				else -> error("Unexpected node: $node")
			}
		}

		// TODO: Make this meaningful
		return Node.Type.Unit
	}

	fun runNode(node: Node) {

	}

	// TODO: Take in a stack or something
	fun runExpression(expression: Node.Expression, context: FunctionContext): Node {

		val stack = LinkedList<Node>()

		expression.nodes.forEachIndexed { index, node ->
			when (node) {

				is Primitive.String,
				is Primitive.Number,
				is Primitive.Char,
				is Primitive.Boolean,
				is Node.Array,
				-> {
					// If is last node
					stack.push(node)
					/*
					if (index + 1 == expression.nodes.size) {
						return node
					}
					*/
				}

				is Node.Identifier -> {
					stack.push(context.parameters[node.name] ?: context.variables[node.name]?.value)
				}

				// TODO: Account for operator overloading
				is Node.GetCall -> {
					val arrayNode = (context.parameters[node.identifier] ?: context.variables[node.identifier]?.value) as Node.Array
					stack.push(arrayNode.values[(runExpression(node.arguments[0], context) as Primitive.Number).data.toInt()])
				}

				is Node.FunctionCall -> {
					runFunctionCall(node, context)
				}

				else -> {}
			}
		}

		checkEquals(stack.size, 1)

		return stack.pop()
		//return CrescentAST.Node.Type.Unit
	}

	fun runFunctionCall(node: Node.FunctionCall, context: FunctionContext): Node {

		when (node.identifier) {

			"println" -> {
				checkEquals(node.arguments.size, 1)
				println(runExpression(node.arguments[0], context).asString())
			}

			else -> {

				val functionFile = checkNotNull(files.find { node.identifier in it.functions }) {
					"Unknown function: ${node.identifier}(${node.arguments.map { runExpression(it, context) }})"
				}

				val function = functionFile.functions[node.identifier]!!

				return runFunction(functionFile, function, node.arguments.map { runExpression(it, context) })
			}

		}

		return Node.Type.Unit
	}

	fun Node.asString(): String {
		return when (this) {

			is Primitive.String -> {
				this.data
			}

			is Node.Type -> {
				"$this"
			}

			is Primitive.Char -> {
				"${this.data}"
			}

			is Primitive.Number -> {
				"${this.data}"
			}

			is Primitive.Boolean -> {
				"${this.data}"
			}

			is Node.Array -> {
				"${this.values.map { it.asString() }}"
			}

			else -> {
				// TODO: Attempt to find a toString()
				error("Unknown node $this")
			}

		}
	}


	fun checkIsSameType(parameter: Node.Parameter, arg: Node) = when (parameter) {

		is Node.Parameter.Basic -> {
			when (parameter.type) {

				is Node.Type.Array -> {
					check(arg is Node.Array)
					checkEquals(parameter.type, arg.type)
				}

				is Node.Type.Basic -> {
					checkEquals(parameter.type.name, arg::class.simpleName!!)
				}

				else -> { error("Unexpected parameter: $parameter")}
			}
		}


		else -> { error("Unexpected parameter: $parameter")}
	}

	/**
	 *
	 * @property variables Name -> Variable
	 * @constructor
	 */
	data class FunctionContext(
		val holder: Node,
		val parameters: MutableMap<String, Node>,
		val variables: MutableMap<String, Node.Variable> = mutableMapOf()
	)

}