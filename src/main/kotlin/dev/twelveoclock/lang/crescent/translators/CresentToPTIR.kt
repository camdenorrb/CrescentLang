package dev.twelveoclock.lang.crescent.translators

import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import tech.poder.ir.api.CodeFile

object CresentToPTIR {
	private val environment = mutableMapOf<String, CodeFile>()

	fun resetEnv() {
		environment.clear()
	}

	fun translate(crescent: CrescentAST.Node.File): CodeFile {
		TODO()
	}
}