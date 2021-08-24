package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.data.TestCode
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentVM
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals


internal class CrescentVMTests {

	val originalSystemOut = System.out


	private inline fun collectSystemOut(block: () -> Unit): String {

		val byteArrayOutputStream = ByteArrayOutputStream()
		val printStream = PrintStream(byteArrayOutputStream)

		System.setOut(printStream)
		block()
		System.setOut(originalSystemOut)

		return byteArrayOutputStream.toString()
	}

	@Test
	fun helloWorld() {

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.helloWorld))

		val output = collectSystemOut {
			CrescentVM(listOf(file), file).invoke()
		}

		assertEquals(output, "Hello World\n")
	}


}