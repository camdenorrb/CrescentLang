package me.camdenorrb.crescentvm

/*
import me.camdenorrb.crescentvm.data.TestCode
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.translators.PoderTranslator
import me.camdenorrb.crescentvm.utils.collectSystemOut
import tech.poder.ir.Machine
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PTIRTranslatorTest {

	@Test
	fun helloWorld() {
		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.helloWorlds))
		val result = PoderTranslator.translate(file)
		val vm = Machine()
		vm.loadPackage(result)
		assertEquals(
			"""
                Hello World
                Hello World
                Hello World
                
            """.trimIndent(),
			collectSystemOut {
				vm.execute("example.crescent:main")
			}
		)
	}

	@Test
	fun testing() {

		val code =
			"""
				fun main { println(1 + 1) }
			""".trimIndent()

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(code))
		val result = PoderTranslator.translate(file)
		val vm = Machine()

		vm.loadPackage(result)
		vm.execute("example.crescent:main")

	}




}
*/