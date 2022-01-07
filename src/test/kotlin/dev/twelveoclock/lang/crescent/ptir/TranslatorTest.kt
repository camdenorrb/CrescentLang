package dev.twelveoclock.lang.crescent.ptir

import dev.twelveoclock.lang.crescent.data.TestCode
import dev.twelveoclock.lang.crescent.lexers.CrescentLexer
import dev.twelveoclock.lang.crescent.parsers.CrescentParser
import dev.twelveoclock.lang.crescent.translators.CresentToPTIR
import org.junit.jupiter.api.Test
import tech.poder.ir.vm.VirtualMachine
import java.nio.file.Paths
import kotlin.io.path.Path

class TranslatorTest {
	@Test
	fun helloWorld() {
		val file = CrescentParser.invoke(Path("Test", "example.crescent").toAbsolutePath(), CrescentLexer.invoke(TestCode.helloWorlds))
		CresentToPTIR.resetEnv()
		val code = CresentToPTIR.translate(Paths.get("Test").toAbsolutePath(), file)
		println(code.joinToString("\n") { it.asCode().toString() })
		VirtualMachine.exec(code[0].asCode(), 1u)
	}

	@Test
	fun helloWorldWithArgs() {
		val file = CrescentParser.invoke(Path("Test", "example.crescent").toAbsolutePath(), CrescentLexer.invoke(TestCode.argsHelloWorld))
		CresentToPTIR.resetEnv()
		val code = CresentToPTIR.translate(Paths.get("Test").toAbsolutePath(), file)
		println(code.joinToString("\n") { it.asCode().toString() })
		VirtualMachine.exec(code[0].asCode(), 1u, "Hello World!")
	}
}