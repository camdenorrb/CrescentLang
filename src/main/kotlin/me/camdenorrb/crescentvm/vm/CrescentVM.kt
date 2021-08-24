package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.project.checkEquals
import me.camdenorrb.crescentvm.vm.CrescentAST.Node
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.Primitive
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.Type
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
				listOf(Node.Array(Type.Array(Type.Basic("String")), Array(args.size) { Primitive.String(args[it]) }))
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

		// TODO: Last expression acts as return
		runBlock(function.innerCode, context)

		// TODO: Make this meaningful
		return Type.Unit
	}

	// TODO: Have a return value
	fun runBlock(block: Node.Statement.Block, context: FunctionContext): Node {

		block.nodes.forEachIndexed { index, node ->

			// If is last node in the block
			if (index + 1 == block.nodes.size) {
				return runNode(node, context)
			}
			else {
				runNode(node, context)
			}

		}

		return Type.Unit
	}

	fun runNode(node: Node, context: FunctionContext): Node {
		when (node) {

			is Primitive.String,
			is Primitive.Number,
			is Primitive.Char,
			is Primitive.Boolean,
			is Node.Array,
			-> {
				return node
			}

			is Node.FunctionCall -> {
				return runFunctionCall(node, context)
			}

			is Node.Expression -> {
				return runExpression(node, context)
			}

			is Node.Statement.If -> {
				return if ((runExpression(node.predicate, context) as Primitive.Boolean).data) {
					runBlock(node.block, context)
				}
				else {
					node.elseBlock?.let {
						runBlock(it, context)
					}
				} ?: Type.Unit
			}

			is Node.Variable -> {
				//context.variables[node.name] = node
				context.variableValues[node.name] = runNode(node.value, context)
			}

			else -> error("Unexpected node: $node")
		}

		return Type.Unit
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
					stack.push(context.parameters[node.name] ?: context.variableValues[node.name])
				}

				// TODO: Account for operator overloading
				is Node.GetCall -> {
					val arrayNode = (context.parameters[node.identifier] ?: context.variableValues[node.identifier]) as Node.Array
					stack.push(arrayNode.values[(runExpression(node.arguments[0], context) as Primitive.Number).data.toInt()])
				}

				is Node.Statement.If -> {
					stack.push(runNode(node, context))
				}

				is Node.FunctionCall -> {

					val returnValue = runFunctionCall(node, context)

					if (returnValue != Type.Unit) {
						stack.push(returnValue)
					}
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

			"readBoolean" -> {
				checkEquals(node.arguments.size, 1)
				println(runExpression(node.arguments[0], context).asString())
				return Primitive.Boolean(readLine()!!.toBooleanStrict())
			}

			else -> {

				val functionFile = checkNotNull(files.find { node.identifier in it.functions }) {
					"Unknown function: ${node.identifier}(${node.arguments.map { runExpression(it, context) }})"
				}

				val function = functionFile.functions.getValue(node.identifier)

				return runFunction(functionFile, function, node.arguments.map { runExpression(it, context) })
			}

		}

		return Type.Unit
	}

	fun Node.asString(): String {
		return when (this) {

			is Primitive.String -> {
				this.data
			}

			is Type -> {
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

				is Type.Array -> {
					check(arg is Node.Array)
					checkEquals(parameter.type, arg.type)
				}

				is Type.Basic -> {
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
		//val variables: MutableMap<String, Node.Variable> = mutableMapOf(),
		val variableValues: MutableMap<String, Node> = mutableMapOf(),
	)

}