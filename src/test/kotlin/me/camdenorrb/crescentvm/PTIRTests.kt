package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.data.TestCode
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentToPTIR
import me.camdenorrb.crescentvm.vm.CrescentVM
import tech.poder.ir.vm.Machine
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.io.path.Path
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PTIRTests {

	val originalSystemOut = System.out

	val originalSystemIn = System.`in`

	private inline fun collectSystemOut(block: () -> Unit): String {

		val byteArrayOutputStream = ByteArrayOutputStream()
		val printStream = PrintStream(byteArrayOutputStream)

		System.setOut(printStream)
		block()
		System.setOut(originalSystemOut)

		return byteArrayOutputStream.toString()
	}

	private inline fun fakeUserInput(input: String, block: () -> Unit) {
		System.setIn(ByteArrayInputStream(input.toByteArray()))
		block()
		System.setIn(originalSystemIn)
	}

	@BeforeTest
	fun cleanMachine() {
		Machine.clear()
	}

	@Test
	fun argsHelloWorld() {
		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.argsHelloWorld))
		val result = CrescentToPTIR.invoke(file)
		Machine.loadCode(*result.toTypedArray())
		assertEquals(
			"Hello World\n",
			collectSystemOut {
				Machine.execute("static.main", "Hello World")
			}
		)
	}

	@Test
	fun helloWorld() {
		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.helloWorld))
		val result = CrescentToPTIR.invoke(file)
		Machine.loadCode(*result.toTypedArray())
		assertEquals(
			"Hello World\n",
			collectSystemOut {
				Machine.execute("static.main")
			}
		)
	}

	@Test
	fun funThing() {
		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.funThing))
		val result = CrescentToPTIR.invoke(file)
		Machine.loadCode(*result.toTypedArray())
		assertEquals(
			"I am a fun thing :)\n",
			collectSystemOut {
				Machine.execute("static.main")
			}
		)
	}

	@Test
	fun ifStatement() {
		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.ifStatement))
		val result = CrescentToPTIR.invoke(file)
		Machine.loadCode(*result.toTypedArray())
		assertEquals(
			"Meow\n",
			collectSystemOut {
				Machine.execute("static.main", true)
			}
		)

		assertEquals(
			"Hiss\n",
			collectSystemOut {
				Machine.execute("static.main", false)
			}
		)
	}

	@Test
	fun ifInputStatement() {

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.ifInputStatement))
		//val result = CrescentToPTIR.invoke(file)
		//Machine.loadCode(*result.toTypedArray())
		assertEquals(
			"""
				Enter a boolean value [true/false]
				Meow
				
			""".trimIndent(),
			collectSystemOut {
				fakeUserInput("true") {
					CrescentVM(listOf(file), file).invoke()
				}
			},
		)

		assertEquals(
			"""
				Enter a boolean value [true/false]
				Hiss
				
			""".trimIndent(),
			collectSystemOut {
				fakeUserInput("false") {
					CrescentVM(listOf(file), file).invoke()
				}
			}
		)
	}


}