package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.iterator.PeekingNodeIterator
import me.camdenorrb.crescentvm.project.checkEquals
import me.camdenorrb.crescentvm.vm.CrescentAST.Node
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.Primitive
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.Type
import java.util.*
import kotlin.math.pow

// TODO: Add a way to add external functions
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
		checkEquals(args.size, function.params.size)

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

		val nodeIterator = PeekingNodeIterator(expression.nodes)

		while (nodeIterator.hasNext()) {
			when (val node = nodeIterator.next()) {

				is Node.Operator -> {
					when (node.operator) {

						CrescentToken.Operator.NOT -> TODO()

						// TODO: Override operators for these in Primitive.Number
						CrescentToken.Operator.ADD -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Number(pop2 + pop1))
						}
						CrescentToken.Operator.SUB -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Number(pop2 - pop1))
						}
						CrescentToken.Operator.MUL -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Number(pop2 * pop1))
						}
						CrescentToken.Operator.DIV -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Number(pop2 / pop1))
						}
						CrescentToken.Operator.POW -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Number(pop2.pow(pop1)))
						}
						CrescentToken.Operator.REM -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Number(pop2 % pop1))
						}

						CrescentToken.Operator.ASSIGN -> TODO()
						CrescentToken.Operator.ADD_ASSIGN -> TODO()
						CrescentToken.Operator.SUB_ASSIGN -> TODO()
						CrescentToken.Operator.MUL_ASSIGN -> TODO()
						CrescentToken.Operator.DIV_ASSIGN -> TODO()
						CrescentToken.Operator.REM_ASSIGN -> TODO()
						CrescentToken.Operator.POW_ASSIGN -> TODO()
						CrescentToken.Operator.OR_COMPARE -> TODO()
						CrescentToken.Operator.AND_COMPARE -> TODO()

						CrescentToken.Operator.EQUALS_COMPARE -> {

							val pop1 = stack.pop()
							val pop2 = stack.pop()

							// TODO: Override !=, ==, >=, <=, <, > on number, then merging this if statement into one statement and remove pop1 and pop2
							if (pop1 is Primitive.Number && pop2 is Primitive.Number) {
								stack.push(Primitive.Boolean(pop2.data.toDouble() == pop1.data.toDouble()))
							}
							else {
								stack.push(Primitive.Boolean(pop2 == pop1))
							}
						}
						CrescentToken.Operator.LESSER_EQUALS_COMPARE -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Boolean(pop2 <= pop1))
						}
						CrescentToken.Operator.GREATER_EQUALS_COMPARE -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Boolean(pop2 >= pop1))
						}

						CrescentToken.Operator.LESSER_COMPARE -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Boolean(pop2 < pop1))
						}
						CrescentToken.Operator.GREATER_COMPARE -> {

							val pop1 = (stack.pop() as Primitive.Number).data.toDouble()
							val pop2 = (stack.pop() as Primitive.Number).data.toDouble()

							stack.push(Primitive.Boolean(pop2 > pop1))
						}


						CrescentToken.Operator.EQUALS_REFERENCE_COMPARE -> TODO()
						CrescentToken.Operator.NOT_EQUALS_COMPARE -> {

							val pop1 = stack.pop()
							val pop2 = stack.pop()

							// TODO: Override !=, ==, >=, <=, <, > on number, then merging this if statement into one statement and remove pop1 and pop2
							if (pop1 is Primitive.Number && pop2 is Primitive.Number) {
								stack.push(Primitive.Boolean(pop2.data.toDouble() != pop1.data.toDouble()))
							}
							else {
								stack.push(Primitive.Boolean(pop2 != pop1))
							}
						}
						CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE -> TODO()
						CrescentToken.Operator.CONTAINS -> TODO()
						CrescentToken.Operator.RANGE -> TODO()
						CrescentToken.Operator.TYPE_PREFIX -> TODO()
						CrescentToken.Operator.RETURN -> TODO()
						CrescentToken.Operator.RESULT -> TODO()
						CrescentToken.Operator.COMMA -> TODO()
						CrescentToken.Operator.DOT -> TODO()
						CrescentToken.Operator.AS -> TODO()
						CrescentToken.Operator.IMPORT_SEPARATOR -> TODO()
						CrescentToken.Operator.INSTANCE_OF -> TODO()
					}
				}

				is Primitive.String,
				is Primitive.Number,
				is Primitive.Char,
				is Primitive.Boolean,
				is Node.Array,
				-> {
					stack.push(node)
				}

				is Node.Identifier -> {
					stack.push(
						context.parameters[node.name]
							?: context.variableValues[node.name]
							?: error("Unknown variable: ${node.name}")
					)
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

			"print" -> {
				checkEquals(node.arguments.size, 1)
				print(runExpression(node.arguments[0], context).asString())
			}

			"println" -> {
				checkEquals(node.arguments.size, 1)
				println(runExpression(node.arguments[0], context).asString())
			}

			"readLine" -> {
				checkEquals(node.arguments.size, 1)
				println(runExpression(node.arguments[0], context).asString())
				return Primitive.String(readLine()!!)
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
					checkEquals(arg.type, parameter.type)
				}

				is Type.Basic -> {
					if (parameter.type.name != "Any") {
						checkEquals(arg::class.simpleName!!, parameter.type.name)
					}
					else {
						// Do nothing
					}
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