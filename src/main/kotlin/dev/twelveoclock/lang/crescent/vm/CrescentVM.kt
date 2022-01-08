package dev.twelveoclock.lang.crescent.vm

import dev.twelveoclock.lang.crescent.iterator.PeekingNodeIterator
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
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

	// Object name -> Object
	val objects = mutableMapOf<String, Instance.Object>()


	fun invoke(args: List<String> = emptyList()) {

		val mainFunction = mainFile.mainFunction!!

		val functionArgs = if (mainFunction.params.isEmpty()) {
			emptyList()
		}
		else {
			listOf(Node.Array(Array(args.size) { Primitive.String(args[it]) }))
		}

		runFunction(
			mainFunction,
			functionArgs,
			BlockContext(mainFile, mainFile, mutableMapOf(), mutableMapOf())
		)
	}

	fun runFunction(function: Node.Function, args: List<Node>, context: BlockContext): Node {

		// TODO: Account for default params
		checkEquals(function.params.size, args.size)

		val functionContext = context.copy()

		function.params.forEachIndexed { index, parameter ->

			val arg = args[index]

			checkIsSameType(parameter, arg) { parameterType ->
				"Parameter type doesn't match argument: $parameterType != ${findType(args[index])}"
			}

			functionContext.parameters[parameter.name] = Variable(parameter.name, Instance.Node(findType(arg), arg), false)
		}

		return runBlock(function.innerCode, functionContext)
	}

	// TODO: Have a return value
	fun runBlock(block: Node.Statement.Block, context: BlockContext): Node {

		block.nodes.forEachIndexed { index, node ->
			// If is last node in the block
			if (index + 1 == block.nodes.size || node is Node.Return) {
				return runNode(node, context)
			} else {
				runNode(node, context)
			}
		}

		return Type.unit
	}

	fun runNode(node: Node, context: BlockContext): Instance {
		when (node) {

			is Primitive.String -> Instance.Node(node.data)
			is Primitive.Number,
			is Primitive.Char,
			is Primitive.Boolean,
			is Node.Array,
			-> {
				return Instance.Node(findType(), node)
			}

			is Node.Identifier -> {

				val holderVariable = when (val holder = context.holder) {
					is Node.Object -> holder.constants[node.name]?.value ?: holder.variables[node.name]?.value
					is Node.File -> holder.constants[node.name]?.value ?: holder.variables[node.name]?.value
					else -> files.firstNotNullOfOrNull { it.constants[node.name]?.value }
				}

				return holderVariable
					?: context.parameters[node.name]?.instance
					?: context.variables[node.name]?.instance
					?: context.file.constants[node.name]?.value
					?: context.file.objects[node.name]?.let { runObject(it, context) }
					?: node
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

				val arrayNode = (context.parameters[node.identifier]
					?: context.variables.getValue(node.identifier).instance.value) as Node.Array

				return arrayNode.values[(runNode(node.arguments[0], context) as Primitive.Number).toI32().data]
			}

			is Node.DotChain -> {

				var lastNode: Node? = null

				node.nodes.forEach {

					if (lastNode == null) {
						lastNode = runNode(it, context)
						return@forEach
					}

					lastNode = runNode(it, context.copy(holder = lastNode!!))
				}

				return lastNode!!
			}

			is Node.Expression -> {
				return runNode(runExpression(node, context), context)
			}

			is Node.Statement.If -> {
				return if ((runNode(node.predicate, context) as Primitive.Boolean).data) {
					runBlock(node.block, context)
				} else {
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
				} else {
					node.ranges.map {
						(it.start as Primitive.Number).toI32().data..(it.end as Primitive.Number).toI32().data
					}
				}

				val counters = node.identifiers.mapIndexed { index, identifier ->

					val counter = Variable(
						identifier.name,
						Primitive.Number.I32(ranges.getOrNull(index)?.first ?: ranges[0].first).let {
							NodeInstance(Primitive.Number.I32.type, it)
						},
						isFinal = true
					)

					forContext.variables[counter.name] = counter

					return@mapIndexed counter
				}

				// Moo's version of For N Loop TODO: Merge

				val range = ranges[ranges.size - 1]
				val count = counters[ranges.size - 1]

				while (true) {

					// Go through last range
					range.forEach {
						count.instance.value = Primitive.Number.I32(it)
						runBlock(node.block, forContext)
					}

					// Set range
					var tmpIndex = ranges.size - 2

					while (tmpIndex > -1) {
						if ((counters[tmpIndex].instance.value as Primitive.Number.I32).data >= ranges[tmpIndex].last) {
							counters[tmpIndex].instance.value = Primitive.Number.I32(ranges[tmpIndex].first)
							tmpIndex--
						} else {
							counters[tmpIndex].instance.value =
								Primitive.Number.I32((counters[tmpIndex].instance.value as Primitive.Number.I32).data + 1)
							break
						}
					}

					if (tmpIndex < 0) {
						break
					}
				}


				/*
				// N For Loop - Kat version TODO: Merge with Moo's version
				/*
				 Broken case:
				    fun main {
                        for x, y, z in 0..3, 0..2, 0..1 {
                            println("$x $y $z")
                        }
					}

					prints 0 0 0 twice
				 */
				while (counters.anyIndexed { index, count -> (count.instance.value as Primitive.Number.I32).data != ranges[index].last }) {

					for (rangeIndex in ranges.indices.reversed()) {

						val range = ranges[rangeIndex]
						val count = counters[rangeIndex]

						if ((count.instance.value as Primitive.Number.I32).data < range.last) {

							runBlock(node.block, forContext)

							count.instance.value = Primitive.Number.I32(
								(count.instance.value as Primitive.Number.I32).data + range.step
							)

							break
						}
						else {

							if (rangeIndex == 0) {
								break
							}

							count.instance.value = Primitive.Number.I32(range.first)
						}
					}
				}

				runBlock(node.block, forContext)*/
			}

			is Node.Variable.Basic,
			is Node.Variable.Local -> {
				runVariable(node as Node.Variable, context)
			}

			else -> error("Unexpected node: $node")
		}

		return Type.unit
	}

	fun runVariable(node: Node.Variable, context: BlockContext): Variable {

		var type = node.type

		val value = when (node) {

			is Node.Variable.Basic -> {

				if (node.type is Type.Implicit) {
					type = findType(node.value)
				}

				runNode(node.value, context)
			}

			is Node.Variable.Local -> {

				if (node.type is Type.Implicit) {
					type = findType(node.value)
				}

				runNode(node.value, context)
			}

			is Node.Variable.Constant -> {

				if (node.type is Type.Implicit) {
					type = findType(node.value)
				}

				if (context.holder is Node.File || context.holder is Node.Object) {
					runNode(node.value, context)
				}
				else {
					error("Constant $node declared not in an Object or File!")
				}
			}

			else -> error("Not a recognized variable node: $node")
		}

		return Variable(node.name, NodeInstance(type, value), node.isFinal)
	}

	fun runObject(objectNode: Node.Object, context: BlockContext): Node.Object {

		val objectContext = context.copy(holder = objectNode, variables = mutableMapOf())

		objects[objectNode.name] = ObjectInstance(
			objectNode.name,
			objectNode.type,
			objectNode.constants.mapValues { runVariable(it.value, objectContext) },
			objectNode.variables.mapValues { runVariable(it.value, objectContext) },
			objectNode.functions,
		)

		return objectNode
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
								} else {
									(pop2 as Primitive.Number) + (pop1 as Primitive.Number)
								}
							)
						}
						CrescentToken.Operator.SUB -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number)
							val pop2 = stack.poll()?.let { (runNode(it, context) as Primitive.Number) }

							if (pop2 == null) {
								stack.push(pop1.multiply(Primitive.Number.I8(-1)))
							}
							else {
								stack.push(pop2 - pop1)
							}
						}
						CrescentToken.Operator.MUL -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number)
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number)

							stack.push(pop2.multiply(pop1))
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
									(context.variables.getValue(pop2.identifier).instance.value as Node.Array).values[index] =
										value
								}

								is Node.Identifier -> {

									val variable = checkNotNull(context.variables[pop2.name]) {
										"Variable ${pop2.name} not found for reassignment."
									}

									val valueType = findType(value)

									checkIsSameType(variable.instance.type, valueType) {
										"Variable ${variable.name}: ${variable.instance.type} cannot be assigned to a value of type $valueType"
									}

									check(!variable.isFinal) {
										"Variable ${variable.name} is not mutable"
									}

									variable.instance = NodeInstance(valueType, value)
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
							} else {
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
							} else {
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
						CrescentToken.Operator.NOT_CONTAINS -> TODO()
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
	}

	fun runFunctionCall(node: Node.IdentifierCall, context: BlockContext): Instance {

		when (node.identifier) {

			"sqrt" -> {
				checkEquals(1, node.arguments.size)
				return Primitive.Number.F64(
					sqrt((runNode(node.arguments[0], context) as Primitive.Number).toF64().data)
				)
			}

			"sin" -> {
				checkEquals(1, node.arguments.size)
				return Primitive.Number.F64(sin((runNode(node.arguments[0], context) as Primitive.Number).toF64().data))
			}

			"round" -> {
				checkEquals(1, node.arguments.size)
				return Primitive.Number.F64(
					round(
						(runNode(
							node.arguments[0],
							context
						) as Primitive.Number).toF64().data
					)
				)
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

				val struct = mainFile.structs[node.identifier]
				val argumentValues = node.arguments.map { runNode(it, context) }

				if (struct != null) {

					val parameters = mutableMapOf<String, Variable>()

					struct.variables.forEachIndexed { index, variable ->

						val argument = argumentValues[index]

						check(variable.type == Type.any || variable.type == findType(argument)) {
							"Variable ${variable.name} had an argument of type ${findType(argumentValues[index])}, expected ${variable.type}"
						}

						parameters[variable.name] = Variable(variable.name, Instance.Node(findType(argument), argument), variable.isFinal)
					}

					Instance.Struct(struct.name, parameters)
				}
				else {

					val function = when (val holder = context.holder) {
						is Node.Object -> holder.functions[node.identifier]
						is Node.File -> holder.functions[node.identifier]
						else -> files.firstNotNullOfOrNull { it.functions[node.identifier] }
					}

					checkNotNull(function) {
						"Unknown function: ${node.identifier}(${node.arguments.map { runNode(it, context) }})"
					}

					//val function = functionFile.functions.getValue(node.identifier)

					function.params.forEachIndexed { index, parameter ->
						check(parameter is Node.Parameter.Basic) {
							"Crescent doesn't support parameters with default values yet."
						}
						check(parameter.type == Type.any || parameter.type == findType(argumentValues[index])) {
							"Parameter ${parameter.name} had an argument of type ${findType(argumentValues[index])}, expected ${parameter.type}"
						}
					}

					return runFunction(function, argumentValues, context)
				}
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

	inline fun checkIsSameType(parameter: Node.Parameter, value: Node, errorBlock: (parameterType: Type) -> String) =
		when (parameter) {

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
			if (type != Type.any) {
				check(type == findType(value)) {
					errorBlock()
					"Expected ${type.name}, got ${value::class.qualifiedName}"
				}
			} else {
				// Do nothing
			}
		}

		else -> {
			error("Expected $type, got ${value::class.qualifiedName}")
		}
	}


	fun findType(value: Node) = when (value) {

		is Node.Typed -> value.type
		is Type.Basic -> value
		is Node.Array -> Type.Array(Type.any) // TODO: Do better

		is Node.IdentifierCall -> {
			// TODO: Add a way to find structs and objects in all imports and what not
			if (value.identifier in mainFile.structs || value.identifier in mainFile.objects) {
				Type.Basic(value.identifier)
			}
			else {
				error("Unexpected value: ${value::class}")
			}
		}

		else -> error("Unexpected value: ${value::class}")
	}


	 sealed class Instance {

		 abstract val type: Type


		 object Unit : Instance()

		 data class Node(
			 override val type: Type,
			 var node: CrescentAST.Node,
		 ) : Instance()

		 data class Value(
			 override val type: Type,
			 var value: Any,
		 ) : Instance()

		 // Merge with instance so it can
		 data class Struct(
			 val name: String,
			 val variables: Map<String, Variable>,
		 ) : Instance() {

			 override val type =

		 }

		 data class Object(
			 val name: String,
			 val constants: Map<String, Variable>,
			 val variables: Map<String, Variable>,
			 val functions: Map<String, CrescentAST.Node.Function>,
		 ) : Instance() {

		 }

	}

	data class Variable(
		val name: String,
		var instance: Instance,
		val isFinal: Boolean,
	)


	/**
	 * @property variables Name -> Variable
	 * @constructor
	 */
	data class BlockContext(
		val file: Node.File,
		val holder: Node,
		val parameters: MutableMap<String, Variable>,
		//val variables: MutableMap<String, Variable(Node.Variable, )> = mutableMapOf(),
		val variables: MutableMap<String, Variable> = mutableMapOf(),
	)

}