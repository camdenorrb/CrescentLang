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
				unusedLocals.clear()
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
			innerCode.nodes.forEach { node ->
				val map = mutableMapOf<String, Any>()
				params.forEachIndexed { index, param ->
					map[param.name] = Param(index.toUInt())
				}
				recursiveFunctionResolve(node, map)
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

	private fun MethodBuilder.getResult(node: CrescentAST.Node, identifiers: Map<String, Any>): Any {
		val dataVar = cachedLocalVar()
		val data = recursiveFunctionResolve(node, identifiers, dataVar)
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

	private fun MethodBuilder.recursiveFunctionResolve(node: CrescentAST.Node, identifiers: Map<String, Any>, result: Variable? = null): Any? {
		when (node) {
			is CrescentAST.Node.GetCall -> {
				val inQuestion = resolveIdentifier(node.identifier, identifiers, result)
				if (node.arguments.isNotEmpty()) {
					getArrayVar(inQuestion, (node.arguments[0] as CrescentAST.Node.Primitive.Number).toI32().data, inQuestion)
				}
				return inQuestion
			}
			is CrescentAST.Node.IdentifierCall -> {
				when (node.identifier) {
					"print", "println" -> {
						val args = mutableListOf<Any>()
						node.arguments.forEach {
							args.add(getResult(it, identifiers))
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
				return resolveExpression(node, identifiers, result)
			}
			is CrescentAST.Node.Return -> {
				return_(getResult(node.expression, identifiers))
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

	private fun MethodBuilder.resolveExpression(expression: CrescentAST.Node.Expression, identifiers: Map<String, Any>, result: Variable?): Any? {
		expression.nodes.forEachIndexed { index, node ->
			when (node) {
				is CrescentToken -> {
					TODO("Node: ${CrescentToken::class.java.name}")
				}
				else -> TODO("Node: ${node::class.java.name}")
			}
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