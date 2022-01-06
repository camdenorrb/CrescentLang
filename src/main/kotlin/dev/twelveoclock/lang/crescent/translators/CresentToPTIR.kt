package dev.twelveoclock.lang.crescent.translators

import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import tech.poder.ir.api.CodeFile
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
			val file = CodeFile(projectDir.relativize(nodeFile.path).toString())
			check(!environmentFiles.containsKey(file.name)) {
				"File ${file.name} already exists in environment!"
			}
			environmentFiles[file.name] = file
			nodeFile.variables.forEach { (_, u) ->
				val name = file.name + "V" + u.name
				check(!environmentMethods.containsKey(name)) {
					"Variable $name already exists in environment!"
				}
				val variable = Variable.newGlobal()
				environmentFields[name] = variable
			}
			nodeFile.structs.forEach { (_, u) ->
				val name = file.name + "S" + u.name
				check(!environmentMethods.containsKey(name)) {
					"Struct $name already exists in environment!"
				}
				val types = mutableListOf<PTIR.Type>()
				u.variables.forEach { type ->
					types.add(resolveType(type.type))
				}
				environmentStructs[name] = Struct(types.toTypedArray())
			}
			nodeFile.functions.forEach { (_, u) ->
				val name = file.name + "F" + u.name
				check(!environmentMethods.containsKey(name)) {
					"Function $name already exists in environment!"
				}
				val method = file.addMethodStub()
				environmentMethods[name] = method
			}
			if (nodeFile.mainFunction !== null) {
				val mainMethod = file.addMethodStub()
				val name = file.name + "M" + nodeFile.mainFunction.name
				check(!environmentMethods.containsKey(name)) {
					"Function $name already exists in environment!"
				}
				environmentMethods[name] = mainMethod
			}
			exec[nodeFile.path.toString()] = nodeFile
		}
		val result = mutableListOf<CodeFile>()
		exec.forEach { (t, u) ->
			val file = environmentFiles[t]!!

			TODO()
			result.add(file)
		}

		return result
	}

	private fun resolveType(type: CrescentAST.Node.Type): PTIR.Type {
		return when(type) {
			is CrescentAST.Node.Type.Basic -> {
				TODO(type.name)
			}
			is CrescentAST.Node.Type.Array -> PTIR.Type.ARRAY
			else -> error("Unsupported type: $type")
		}
	}
}