package dev.twelveoclock.lang.crescent.translators

import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import tech.poder.ir.api.CodeFile
import java.nio.file.Path

object CresentToPTIR {
	private val environmentMethods = mutableMapOf<String, UInt>()
	private val environmentFiles = mutableMapOf<String, CodeFile>()

	fun resetEnv() {
		environmentFiles.clear()
		environmentMethods.clear()
	}

	fun translate(projectDir: Path, vararg crescent: CrescentAST.Node.File): List<CodeFile> {
		val exec = mutableMapOf<String, CrescentAST.Node.File>()
		crescent.forEach {
			val file = CodeFile(projectDir.relativize(it.path).toString())
			check(!environmentFiles.containsKey(file.name)) {
				"File ${file.name} already exists in environment!"
			}
			environmentFiles[file.name] = file
			it.functions.forEach { (_, u) ->
				val name = file.name + "F" + u.name
				check(!environmentMethods.containsKey(name)) {
					"Function $name already exists in environment!"
				}
				val method = file.addMethodStub()
				environmentMethods[name] = method
			}
			if (it.mainFunction !== null) {
				val mainMethod = file.addMethodStub()
				val name = file.name + "M" + it.mainFunction.name
				check(!environmentMethods.containsKey(name)) {
					"Function $name already exists in environment!"
				}
				environmentMethods[name] = mainMethod
			}
			exec[it.path.toString()] = it
		}
		val result = mutableListOf<CodeFile>()
		exec.forEach { t, u ->
			val file = environmentFiles[t]!!
			TODO()
			result.add(file)
		}

		return result
	}
}