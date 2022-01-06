package dev.twelveoclock.lang.crescent.translators

import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import dev.twelveoclock.lang.crescent.translators.CresentToPTIR.resolveToPTIR
import tech.poder.ir.api.CodeFile
import tech.poder.ir.api.MethodBuilder
import tech.poder.ir.api.Struct
import tech.poder.ir.api.Variable
import tech.poder.ptir.PTIR
import java.nio.file.Path

object CresentToPTIR {
	private val environmentStructs = mutableMapOf<String, Struct>()
	private val environmentFields = mutableMapOf<String, Variable>()
	private val environmentMethods = mutableMapOf<String, UInt>()
	private val environmentFiles = mutableMapOf<String, CodeFile>()

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
				recursiveFunctionResolve(it, this@resolveToPTIR)
			}
		}
	}

	private fun MethodBuilder.recursiveFunctionResolve(node: CrescentAST.Node, base: CrescentAST.Node.Function) {

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