package dev.twelveoclock.lang.crescent.vm

import dev.twelveoclock.lang.crescent.iterator.PeekingNodeIterator
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Primitive
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST.Node.Type
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import dev.twelveoclock.lang.crescent.project.checkEquals
import java.util.*
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

// TODO: Add a way to add external functions
// TODO: Find a way to remove recursion
// TODO: Don't modify the AST
class CrescentVM(val files: List<Node.File>, val mainFile: Node.File) {

	fun invoke(args: List<String> = emptyList()) {

		val mainFunction = checkNotNull(mainFile.mainFunction)

		if (mainFunction.params.isEmpty()) {
			runFunction(mainFile, mainFile, mainFunction, emptyList())
		}
		else {
			runFunction(
				mainFile,
				mainFile,
				mainFunction,
				listOf(Node.Array(Array(args.size) { Primitive.String(args[it]) }))
			)
		}
	}

	fun runFunction(file: Node.File, holder: Node, function: Node.Function, args: List<Node>): Node {

		// TODO: Account for default params
		checkEquals(function.params.size, args.size)

		val paramsToValue = mutableMapOf<String, Node>()

		function.params.forEachIndexed { index, parameter ->

			checkIsSameType(parameter, args[index]) { parameterType ->
				"Parameter type doesn't match argument: $parameterType != ${findType(args[index])}"
			}

			paramsToValue[parameter.name] = args[index]
		}

		val context = BlockContext(
			file,
			holder,
			paramsToValue
		)

		// TODO: Last expression acts as return
		return runBlock(function.innerCode, context)

		// TODO: Make this meaningful
		//return Type.unit
	}

	// TODO: Have a return value
	fun runBlock(block: Node.Statement.Block, context: BlockContext): Node {

		block.nodes.forEachIndexed { index, node ->
			// If is last node in the block
			if (index + 1 == block.nodes.size || node is Node.Return) {
				return runNode(node, context)
			}
			else {
				runNode(node, context)
			}
		}

		return Type.unit
	}

	fun runNode(node: Node, context: BlockContext): Node {
		when (node) {

			is Primitive.String,
			is Primitive.Number,
			is Primitive.Char,
			is Primitive.Boolean,
			is Node.Array,
			-> {
				return node
			}

			is Node.Identifier -> {
				return context.parameters[node.name]
					?: context.variables[node.name]?.instance?.value
					?: context.file.constants[node.name]?.value
					?: Node.Identifier(node.name)
					//?: error("Unknown variable: ${node.name}")
			}

			is Node.IdentifierCall -> {
				// TODO: Determine if it's a constructor call
				return runFunctionCall(node, context)
			}

			is Node.Return -> {
				return runNode(node.expression, context)
			}

			// TODO: Account for operator overloading
			is Node.GetCall -> {
				val arrayNode = (context.parameters[node.identifier] ?: context.variables.getValue(node.identifier).instance.value) as Node.Array
				return arrayNode.values[(runNode(node.arguments[0], context) as Primitive.Number).toI32().data]
			}

			is Node.DotChain -> {
				/*
				node.nodes.forEach {
					runNode()
				}
				*/
			}

			is Node.Expression -> {
				return runExpression(node, context)
			}

			is Node.Statement.If -> {
				return if ((runNode(node.predicate, context) as Primitive.Boolean).data) {
					runBlock(node.block, context)
				}
				else {
					node.elseBlock?.let {
						runBlock(it, context)
					}
				} ?: Type.unit
			}

			is Node.Statement.While -> {
				while ((runNode(node.predicate, context) as Primitive.Boolean).data) {
					runBlock(node.block, context)
				}
			}

			is Node.Statement.For -> {

				val forContext = context.copy()

				val ranges = if (node.ranges.size == 1 && node.identifiers.size > 1) {
					node.identifiers.map {
						(node.ranges[0].start as Primitive.Number).toI32().data..(node.ranges[0].end as Primitive.Number).toI32().data
					}
				}
				else {
					node.ranges.map {
						(it.start as Primitive.Number).toI32().data..(it.end as Primitive.Number).toI32().data
					}
				}

				val counters = node.identifiers.mapIndexed { index, identifier ->

					val counter = BlockContext.Variable(
						identifier.name,
						Primitive.Number.I32(ranges.getOrNull(index)?.first ?: ranges[0].first).let {
							Instance(Primitive.Number.I32.type, it)
						},
						isMutable = false
					)

					forContext.variables[counter.name] = counter

					return@mapIndexed counter
				}

				// N For Loop
				while ((counters.first().instance.value as Primitive.Number.I32).data <= ranges.first().last) {

					for (rangeIndex in ranges.indices.reversed()) {

						val range = ranges[rangeIndex]
						val count = counters[rangeIndex]

						if ((count.instance.value as Primitive.Number.I32).data <= range.last) {

							//println(count.instance.value)
							runBlock(node.block, forContext)

							count.instance.value = Primitive.Number.I32(
								(count.instance.value as Primitive.Number.I32).data + range.step
							)

							break
						}
						else {
							count.instance.value = Primitive.Number.I32(range.first)
						}
					}

				}

			}

			is Node.Variable.Basic -> {

				val value = runNode(node.value, context)

				val type = if (node.type is Type.Implicit) {
					findType(node.value)
				}
				else {
					node.type
				}

				context.variables[node.name] = BlockContext.Variable(node.name, Instance(type, value), node.isFinal)
			}

			else -> error("Unexpected node: $node")
		}

		return Type.unit
	}

	// TODO: Take in a stack or something
	fun runExpression(expression: Node.Expression, context: BlockContext): Node {

		val stack = LinkedList<Node>()

		val nodeIterator = PeekingNodeIterator(expression.nodes)

		while (nodeIterator.hasNext()) {
			when (val node = nodeIterator.next()) {

				// TODO: Run operator function
				is CrescentToken.Operator -> {
					when (node) {

						CrescentToken.Operator.NOT -> TODO()

						// TODO: Override operators for these in Primitive.Number
						CrescentToken.Operator.ADD -> {

							val pop1 = runNode(stack.pop(), context)
							val pop2 = runNode(stack.pop(), context)

							stack.push(
								if (pop2 is Primitive.String || pop1 is Primitive.String) {
									Primitive.String(pop2.asString() + pop1.asString())
								}
								else {
									(pop2 as Primitive.Number) + (pop1 as Primitive.Number)
								}
							)
						}
						CrescentToken.Operator.SUB -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number)
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number)

							stack.push(pop2 - pop1)
						}
						CrescentToken.Operator.MUL -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number)
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number)

							stack.push(pop2 * pop1)
						}
						CrescentToken.Operator.DIV -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number)
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number)

							stack.push(pop2 / pop1)
						}
						CrescentToken.Operator.POW -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number)
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number)

							stack.push(pop2.pow(pop1))
						}
						CrescentToken.Operator.REM -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number)
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number)

							stack.push(pop2 % pop1)
						}

						CrescentToken.Operator.ASSIGN -> {

							val value = runNode(stack.pop(), context)

							when (val pop2 = stack.pop()) {

								is Node.GetCall -> {
									checkEquals(1, pop2.arguments.size)
									val index = (pop2.arguments.first() as Primitive.Number).toI32().data
									(context.variables.getValue(pop2.identifier).instance.value as Node.Array).values[index] = value
								}

								is Node.Identifier -> {

									val variable = checkNotNull(context.variables[pop2.name]) {
										"Variable ${pop2.name} not found for reassignment."
									}

									val valueType = findType(value)

									checkIsSameType(variable.instance.type, valueType) {
										"Variable ${variable.name}: ${variable.instance.type} cannot be assigned to a value of type $valueType"
									}

									check(variable.isMutable) {
										"Variable ${variable.name} is not mutable"
									}

									variable.instance = Instance(valueType, value)
								}
							}

							return Type.unit
						}

						CrescentToken.Operator.ADD_ASSIGN -> {

						}
						CrescentToken.Operator.SUB_ASSIGN -> TODO()
						CrescentToken.Operator.MUL_ASSIGN -> TODO()
						CrescentToken.Operator.DIV_ASSIGN -> TODO()
						CrescentToken.Operator.REM_ASSIGN -> TODO()
						CrescentToken.Operator.POW_ASSIGN -> TODO()

						CrescentToken.Operator.OR_COMPARE -> {

							val pop1 = runNode(stack.pop(), context) as Primitive.Boolean
							val pop2 = runNode(stack.pop(), context) as Primitive.Boolean

							stack.push(Primitive.Boolean(pop2.data || pop1.data))
						}

						CrescentToken.Operator.AND_COMPARE -> {

							val pop1 = runNode(stack.pop(), context) as Primitive.Boolean
							val pop2 = runNode(stack.pop(), context) as Primitive.Boolean

							stack.push(Primitive.Boolean(pop2.data && pop1.data))
						}

						CrescentToken.Operator.EQUALS_COMPARE -> {

							val pop1 = runNode(stack.pop(), context)
							val pop2 = runNode(stack.pop(), context)

							// TODO: Override !=, ==, >=, <=, <, > on number, then merging this if statement into one statement and remove pop1 and pop2
							if (pop1 is Primitive.Number && pop2 is Primitive.Number) {
								stack.push(Primitive.Boolean(pop2.toF64().data == pop1.toF64().data))
							}
							else {
								stack.push(Primitive.Boolean(pop2 == pop1))
							}
						}
						CrescentToken.Operator.LESSER_EQUALS_COMPARE -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data

							stack.push(Primitive.Boolean(pop2 <= pop1))
						}
						CrescentToken.Operator.GREATER_EQUALS_COMPARE -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data

							stack.push(Primitive.Boolean(pop2 >= pop1))
						}

						CrescentToken.Operator.LESSER_COMPARE -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data

							stack.push(Primitive.Boolean(pop2 < pop1))
						}
						CrescentToken.Operator.GREATER_COMPARE -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data

							stack.push(Primitive.Boolean(pop2 > pop1))
						}


						CrescentToken.Operator.EQUALS_REFERENCE_COMPARE -> TODO()

						CrescentToken.Operator.NOT_EQUALS_COMPARE -> {

							val pop1 = runNode(stack.pop(), context)
							val pop2 = runNode(stack.pop(), context)

							// TODO: Override !=, ==, >=, <=, <, >, xor, or, and, etc on number, then merging this if statement into one statement and remove pop1 and pop2
							if (pop1 is Primitive.Number && pop2 is Primitive.Number) {
								stack.push(Primitive.Boolean(pop2.toF64().data != pop1.toF64().data))
							}
							else {
								stack.push(Primitive.Boolean(pop2 != pop1))
							}
						}

						CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE -> TODO()
						CrescentToken.Operator.CONTAINS -> TODO()
						CrescentToken.Operator.RANGE_TO -> TODO()
						CrescentToken.Operator.TYPE_PREFIX -> TODO()
						CrescentToken.Operator.RETURN -> {
							return stack.poll() ?: Type.unit
						}
						CrescentToken.Operator.RESULT -> TODO()
						CrescentToken.Operator.COMMA -> TODO()
						CrescentToken.Operator.DOT -> TODO()
						CrescentToken.Operator.AS -> TODO()
						CrescentToken.Operator.IMPORT_SEPARATOR -> TODO()

						CrescentToken.Operator.INSTANCE_OF -> {

							val pop1 = (runNode(stack.pop(), context) as Node.Identifier)
							val pop2 = runNode(stack.pop(), context)

							stack.push(Primitive.Boolean(pop1.name == Type.any.name || "${findType(pop2)}" == pop1.name))
						}

						CrescentToken.Operator.BIT_SHIFT_RIGHT -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 shr pop1))
						}

						CrescentToken.Operator.BIT_SHIFT_LEFT -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 shl pop1))
						}

						CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 ushr pop1))
						}

						CrescentToken.Operator.BIT_OR -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 or pop1))
						}

						CrescentToken.Operator.BIT_AND -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 and pop1))
						}
						CrescentToken.Operator.BIT_XOR -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 xor pop1))
						}

						CrescentToken.Operator.NOT_INSTANCE_OF -> TODO()
					}
				}

				is Primitive.String,
				is Primitive.Number,
				is Primitive.Char,
				is Primitive.Boolean,
				is Node.Array,
				is Node.Identifier,
				is Node.GetCall,
				is Node.Statement.If,
				is Node.IdentifierCall,
				-> {
					stack.push(node)
				}

				else -> error("Unexpected node: $node")
			}
		}

		checkEquals(1, stack.size)

		return runNode(stack.pop(), context)
		//return CrescentAST.Node.Type.Unit
	}

	fun runFunctionCall(node: Node.IdentifierCall, context: BlockContext): Node {

		when (node.identifier) {

			"sqrt" -> {
				checkEquals(1, node.arguments.size)
				return Primitive.Number.F64(sqrt((runNode(node.arguments[0], context) as Primitive.Number).toF64().data))
			}

			"sin" -> {
				checkEquals(1, node.arguments.size)
				return Primitive.Number.F64(sin((runNode(node.arguments[0], context) as Primitive.Number).toF64().data))
			}

			"round" -> {
				checkEquals(1, node.arguments.size)
				return Primitive.Number.F64(round((runNode(node.arguments[0], context) as Primitive.Number).toF64().data))
			}

			"print" -> {
				checkEquals(1, node.arguments.size)
				print(runNode(node.arguments[0], context).asString())
			}

			"println" -> {

				check(node.arguments.size <= 1) {
					"Too many args for println call!"
				}

				if (node.arguments.isEmpty()) {
					println()
				}
				else {
					println(runNode(node.arguments[0], context).asString())
				}
			}

			"readLine" -> {
				checkEquals(1, node.arguments.size)
				println(runNode(node.arguments[0], context).asString())
				return Primitive.String(readLine()!!)
			}

			"readBoolean" -> {
				checkEquals(1, node.arguments.size)
				println(runNode(node.arguments[0], context).asString())
				return Primitive.Boolean(readLine()!!.toBooleanStrict())
			}

			// TODO: Make this return a special type struct instance
			"typeOf" -> {
				checkEquals(1, node.arguments.size)
				return findType(runNode(node.arguments[0], context))
			}

			else -> {

				val functionFile = checkNotNull(files.find { node.identifier in it.functions }) {
					"Unknown function: ${node.identifier}(${node.arguments.map { runNode(it, context) }})"
				}

				val function = functionFile.functions.getValue(node.identifier)
				val argumentValues = node.arguments.map { runNode(it, context) }

				function.params.forEachIndexed { index, parameter ->
					check(parameter is Node.Parameter.Basic) {
						"Crescent doesn't support parameters with default values yet."
					}
					check(parameter.type == Type.any || parameter.type == findType(argumentValues[index])) {
						"Parameter ${parameter.name} had an argument of type ${findType(argumentValues[index])}, expected ${parameter.type}"
					}
				}

				return runFunction(functionFile, functionFile, function, argumentValues)
			}

		}

		return Type.unit
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
				"$this"
			}

			is Primitive.Boolean -> {
				"${this.data}"
			}

			is Node.Array -> {
				"${this.values.map { it.asString() }}"
			}

			is Node.Identifier -> {
				this.name
			}

			else -> {
				// TODO: Attempt to find a toString()
				error("Unknown node ${this::class}")
			}
		}
	}


	fun findType(value: Node) = when (value) {

		is Node.Typed -> value.type
		is Type.Basic -> value
		is Node.Array -> Type.Array(Type.any) // TODO: Do better

		else -> error("Unexpected value: ${value::class}")
	}

	inline fun checkIsSameType(parameter: Node.Parameter, value: Node, errorBlock: (parameterType: Type) -> String) = when (parameter) {

		is Node.Parameter.Basic -> {
			checkIsSameType(parameter.type, value) {
				errorBlock(parameter.type)
			}
		}

		else -> TODO()
	}

	// TODO: Use typeOf instead
	inline fun checkIsSameType(type: Type, value: Node, errorBlock: () -> String) = when (type) {

		/*
        is Type.Array -> {
            check(arg is Node.Array)
            checkEquals(parameter.type, typeOf(arg.values.first()))
        }
        */

		is Type.Array -> {
			// TODO: Implement this
		}

		is Type.Basic -> {
			if (type.name != "Any") {
				check(type.name == value::class.simpleName) {
					errorBlock()
					"Expected ${type.name}, got ${value::class.qualifiedName}"
				}
			}
			else {
				// Do nothing
			}
		}

		else -> {
			error("Expected $type, got ${value::class.qualifiedName}")
		}
	}


	data class Instance(
		val type: Type,
		var value: Node
	)

	/**
	 * @property variables Name -> Variable
	 * @constructor
	 */
	data class BlockContext(
		val file: Node.File,
		val holder: Node,
		val parameters: MutableMap<String, Node>,
		//val variables: MutableMap<String, Variable(Node.Variable, )> = mutableMapOf(),
		val variables: MutableMap<String, Variable> = mutableMapOf(),
	) {

		data class Variable(
			val name: String,
			var instance: Instance,
			val isMutable: Boolean,
		)

	}

}