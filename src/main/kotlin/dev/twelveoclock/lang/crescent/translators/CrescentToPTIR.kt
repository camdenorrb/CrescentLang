package dev.twelveoclock.lang.crescent.translators

import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import tech.poder.ir.api.CodeFile
import tech.poder.ir.api.MethodBuilder
import tech.poder.ir.api.Struct
import tech.poder.ir.api.Variable
import tech.poder.ptir.PTIR
import java.nio.file.Path
import java.util.*

class CrescentToPTIR {
	private val environmentStructs = mutableMapOf<String, Struct>()
	private val environmentFields = mutableMapOf<String, Variable>()
	private val environmentMethods = mutableMapOf<String, UInt>()
	private val environmentFiles = mutableMapOf<String, CodeFile>()
	private val unusedLocals = mutableListOf<Variable>()

	fun resetEnv() {
		environmentFiles.clear()
		environmentMethods.clear()
	}

	fun translate(projectDir: Path, vararg crescent: CrescentAST.Node.File): List<CodeFile> {
		val exec = mutableMapOf<String, CrescentAST.Node.File>()
		crescent.forEach { nodeFile ->
			val file = CodeFile(projectDir.parent.relativize(nodeFile.path).toString())
			check(!environmentFiles.containsKey(file.name)) {
				"File ${file.name} already exists in environment!"
			}
			var mainFunc = "NOT_FOUND"
			environmentFiles[file.name] = file
			environmentMethods[file.name + "Iinit"] = 0u //first is init(0u)
			if (nodeFile.mainFunction !== null) {
				mainFunc = nodeFile.mainFunction.name
				val mainMethod = file.addMethodStub() //second is main(1u)
				val name = file.name + "F" + nodeFile.mainFunction.name
				check(!environmentMethods.containsKey(name)) {
					"Function $name already exists in environment!"
				}
				environmentMethods[name] = mainMethod
			}
			nodeFile.variables.forEach { (_, vars) ->
				val name = file.name + "V" + vars.name
				check(!environmentMethods.containsKey(name)) {
					"Variable $name already exists in environment!"
				}
				val variable = Variable.newGlobal()
				environmentFields[name] = variable
			}
			nodeFile.structs.forEach { (_, struct) ->
				val name = file.name + "S" + struct.name
				check(!environmentMethods.containsKey(name)) {
					"Struct $name already exists in environment!"
				}
				val types = mutableListOf<PTIR.FullType>()
				struct.variables.forEach { type ->
					types.add(resolveType(type.type))
				}
				environmentStructs[name] = Struct(types.toTypedArray())
			}
			nodeFile.functions.forEach { (_, func) ->
				if (func.name != mainFunc) {
					val name = file.name + "F" + func.name
					check(!environmentMethods.containsKey(name)) {
						"Function $name already exists in environment!"
					}
					val method = file.addMethodStub()
					environmentMethods[name] = method
				}
			}
			exec[file.name] = nodeFile
		}
		val result = mutableListOf<CodeFile>()
		exec.forEach { (name, node) ->
			val file = environmentFiles[name]!!
			node.functions.forEach { (_, func) ->
				unusedLocals.clear()
				func.resolveToPTIR(file, name)
			}
			result.add(file)
		}

		//TODO do setup var pre_init

		return result
	}

	private fun CrescentAST.Node.Function.resolveToPTIR(file: CodeFile, currentFile: String) {
		val methodId = environmentMethods[file.name + "F" + name]!!
		file.fromMethodStub(methodId) {
			innerCode.nodes.forEach { node ->
				val map = mutableMapOf<String, Any>()
				params.forEachIndexed { index, param ->
					map[param.name] = Param(index.toUInt())
				}
				recursiveFunctionResolve(node, currentFile, map)
			}
		}
	}

	private fun MethodBuilder.cachedLocalVar(): Variable {
		return if (unusedLocals.isNotEmpty()) {
			unusedLocals.removeLast()
		} else {
			newLocal()
		}
	}

	private fun MethodBuilder.getResult(node: CrescentAST.Node, currentFile: String, identifiers: Map<String, Any>): Any {
		val dataVar = cachedLocalVar()
		val data = recursiveFunctionResolve(node, currentFile, identifiers, dataVar)
		return if (data == null || (data is Variable && data == dataVar)) {
			dataVar
		} else {
			unusedLocals.add(dataVar)
			data
		}
	}

	private fun MethodBuilder.resolveIdentifier(name: String, identifiers: Map<String, Any>, result: Variable?): Variable {
		return when (val type = identifiers[name]!!) {
			is Param -> {
				val identity = result ?: cachedLocalVar()
				getArrayVar(Variable.ARGS, type.index, identity)
				identity
			}
			is Variable -> {
				type
			}
			else -> {
				throw IllegalStateException("Unknown type ${type::class.java}")
			}
		}
	}

	private fun MethodBuilder.recursiveFunctionResolve(node: CrescentAST.Node, currentFile: String, identifiers: Map<String, Any>, result: Variable? = null): Any? {
		when (node) {
			is CrescentAST.Node.GetCall -> {
				val inQuestion = resolveIdentifier(node.identifier, identifiers, result)
				if (node.arguments.isNotEmpty()) {
					getArrayVar(inQuestion, (node.arguments[0] as CrescentAST.Node.Primitive.Number).toI32().data, inQuestion)
				}
				return inQuestion
			}
			is CrescentAST.Node.IdentifierCall -> {
				val args = mutableListOf<Any>()
				node.arguments.forEach {
					args.add(getResult(it, currentFile, identifiers))
				}
				when (node.identifier) {
					"print", "println" -> {
						var arg = if (args.size > 1) {
							val res = cachedLocalVar()
							args.forEach {
								add(res, res, it)
							}
							args.forEach {
								if (it is Variable) {
									unusedLocals.add(it)
								}
							}
							args.clear()
							res
						} else {
							args[0]
						}
						if (node.identifier.endsWith("ln")) {
							if (arg is Variable) {
								add(arg, arg, "\n")
							} else {
								when (arg) {
									is String -> arg += "\n"
									else -> {
										println("TODO(ArgResolve): ${arg::class.java.name}")
										val res = cachedLocalVar()
										add(res, arg, "\n")
										arg = res
									}
								}
							}
						}
						invoke(PTIR.STDCall.PRINT, null, arg)
						if (arg is Variable) {
							unusedLocals.add(arg)
						}
					}
					else -> {
						val function = environmentMethods["${currentFile}F${node.identifier}"]!! //TODO resolve multi file functions!
						invoke(environmentFiles[currentFile]!!, function, result, *args.toTypedArray())
					}
				}
			}
			is CrescentAST.Node.Expression -> {
				return resolveExpression(node, currentFile, identifiers)
			}
			is CrescentAST.Node.Return -> {
				return_(getResult(node.expression, currentFile, identifiers))
			}
			is CrescentAST.Node.Primitive.Number.I8 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.I16 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.I32 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.I64 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.F32 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.F64 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.U8 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.U16 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.U32 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.Number.U64 -> {
				return node.data
			}
			is CrescentAST.Node.Primitive.String -> {
				return node.data
			}
			is CrescentAST.Node.Identifier -> {
				return resolveIdentifier(node.name, identifiers, result)
			}
			else -> println("TODO(Node): ${node::class.simpleName}")
		}
		return null
	}

	private fun MethodBuilder.resolveExpression(expression: CrescentAST.Node.Expression, currentFile: String, identifiers: Map<String, Any>): Any? {
		val stack = Stack<Any>() //need to de-stack the data since PTIR is Linear!
		expression.nodes.forEachIndexed { index, node ->
			when (node) {
				is CrescentToken.Operator -> {
					when (node) {
						CrescentToken.Operator.NOT -> TODO()
						CrescentToken.Operator.ADD -> {
							val result = cachedLocalVar()
							add(result, stack.pop(), stack.pop())
							stack.push(result)
						}
						CrescentToken.Operator.SUB -> {
							val result = cachedLocalVar()
							if (stack.size == 1) {
								multiply(result, stack.pop(), -1)
							} else {
								subtract(result, stack.pop(), stack.pop())
							}
							stack.push(result)
						}
						CrescentToken.Operator.MUL -> TODO()
						CrescentToken.Operator.DIV -> TODO()
						CrescentToken.Operator.POW -> TODO()
						CrescentToken.Operator.REM -> TODO()
						CrescentToken.Operator.ASSIGN -> TODO()
						CrescentToken.Operator.ADD_ASSIGN -> TODO()
						CrescentToken.Operator.SUB_ASSIGN -> TODO()
						CrescentToken.Operator.MUL_ASSIGN -> TODO()
						CrescentToken.Operator.DIV_ASSIGN -> TODO()
						CrescentToken.Operator.REM_ASSIGN -> TODO()
						CrescentToken.Operator.POW_ASSIGN -> TODO()
						CrescentToken.Operator.OR_COMPARE -> TODO()
						CrescentToken.Operator.AND_COMPARE -> TODO()
						CrescentToken.Operator.EQUALS_COMPARE -> TODO()
						CrescentToken.Operator.LESSER_COMPARE -> TODO()
						CrescentToken.Operator.GREATER_COMPARE -> TODO()
						CrescentToken.Operator.LESSER_EQUALS_COMPARE -> TODO()
						CrescentToken.Operator.GREATER_EQUALS_COMPARE -> TODO()
						CrescentToken.Operator.BIT_SHIFT_RIGHT -> TODO()
						CrescentToken.Operator.BIT_SHIFT_LEFT -> TODO()
						CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT -> TODO()
						CrescentToken.Operator.BIT_OR -> TODO()
						CrescentToken.Operator.BIT_XOR -> TODO()
						CrescentToken.Operator.BIT_AND -> TODO()
						CrescentToken.Operator.EQUALS_REFERENCE_COMPARE -> TODO()
						CrescentToken.Operator.NOT_EQUALS_COMPARE -> TODO()
						CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE -> TODO()
						CrescentToken.Operator.CONTAINS -> TODO()
						CrescentToken.Operator.NOT_CONTAINS -> TODO()
						CrescentToken.Operator.RANGE_TO -> TODO()
						CrescentToken.Operator.TYPE_PREFIX -> TODO()
						CrescentToken.Operator.RETURN -> TODO()
						CrescentToken.Operator.RESULT -> TODO()
						CrescentToken.Operator.COMMA -> TODO()
						CrescentToken.Operator.DOT -> TODO()
						CrescentToken.Operator.AS -> TODO()
						CrescentToken.Operator.IMPORT_SEPARATOR -> TODO()
						CrescentToken.Operator.INSTANCE_OF -> TODO()
						CrescentToken.Operator.NOT_INSTANCE_OF -> TODO()
					}
				}
				else -> stack.push(getResult(node, currentFile, identifiers))
			}
		}
		check(stack.size < 2) {
			"Too many items in stack!"
		}
		if (stack.isNotEmpty()) {
			return stack.pop()
		}
		return null
	}

	private fun resolveType(type: CrescentAST.Node.Type): PTIR.FullType {
		return when (type) {
			is CrescentAST.Node.Type.Basic -> {
				TODO(type.name)
			}
			is CrescentAST.Node.Type.Array -> PTIR.FullType.DEFAULT //default is an array!
			else -> error("Unsupported type: $type")
		}
	}
}