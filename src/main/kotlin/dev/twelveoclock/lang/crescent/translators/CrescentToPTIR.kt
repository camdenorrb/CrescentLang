package dev.twelveoclock.lang.crescent.translators

import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import tech.poder.ir.api.CodeFile
import tech.poder.ir.api.MethodBuilder
import tech.poder.ir.api.Struct
import tech.poder.ir.api.Variable
import tech.poder.ptir.PTIR
import java.nio.file.Path

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
				func.resolveToPTIR(file)
			}
			result.add(file)
		}

		//TODO do setup var pre_init

		return result
	}

	private fun CrescentAST.Node.Function.resolveToPTIR(file: CodeFile) {
		val methodId = environmentMethods[file.name + "F" + name]!!
		file.fromMethodStub(methodId) {
			innerCode.nodes.forEach {
				recursiveFunctionResolve(it)
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

	private fun MethodBuilder.getResult(node: CrescentAST.Node): Any {
		val dataVar = cachedLocalVar()
		val data = recursiveFunctionResolve(node, dataVar)
		return if (data == null) {
			dataVar
		} else {
			unusedLocals.add(dataVar)
			data
		}
	}

	private fun MethodBuilder.recursiveFunctionResolve(node: CrescentAST.Node, result: Variable? = null): Any? {
		when (node) {
			is CrescentAST.Node.GetCall -> {
				when (node.identifier) {
					"args" -> {
						val res = getResult(node.arguments[0])
						getArrayVar(Variable.ARGS, res, result!!)
						if (res is Variable) {
							unusedLocals.add(res)
						}
					}
					else -> println("TODO(NodeGet): ${node.identifier}")
				}
			}
			is CrescentAST.Node.IdentifierCall -> {
				when (node.identifier) {
					"print", "println" -> {
						val args = mutableListOf<Any>()
						node.arguments.forEach {
							args.add(getResult(it))
						}
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
					else -> println("TODO(NodeIdentifier): ${node.identifier}")
				}
			}
			is CrescentAST.Node.Expression -> {
				return resolveExpression(node, result)
			}
			is CrescentAST.Node.Return -> {
				return_(getResult(node.expression))
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
				println("random node: " + node.name)
			}
			else -> println("TODO(Node): ${node::class.simpleName}")
		}
		return null
	}

	private fun MethodBuilder.resolveExpression(expression: CrescentAST.Node.Expression, result: Variable?): Any? {
		expression.nodes.forEachIndexed { index, node ->
			when(node) {
				is CrescentToken -> {

				}
				else -> TODO("Node: ${node::class.java.name}")
			}
		}
		return null
	}

	private fun resolveType(type: CrescentAST.Node.Type): PTIR.FullType {
		return when(type) {
			is CrescentAST.Node.Type.Basic -> {
				TODO(type.name)
			}
			is CrescentAST.Node.Type.Array -> PTIR.FullType.DEFAULT //default is an array!
			else -> error("Unsupported type: $type")
		}
	}
}