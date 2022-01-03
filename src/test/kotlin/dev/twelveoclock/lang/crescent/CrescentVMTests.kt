package dev.twelveoclock.lang.crescent

import dev.twelveoclock.lang.crescent.data.TestCode
import dev.twelveoclock.lang.crescent.lexers.CrescentLexer
import dev.twelveoclock.lang.crescent.parsers.CrescentParser
import dev.twelveoclock.lang.crescent.vm.CrescentVM
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CrescentVMTests {

	val originalSystemOut = System.out

	val originalSystemIn = System.`in`


	private inline fun collectSystemOut(alsoPrintToConsole: Boolean = false, block: () -> Unit): String {


		val byteArrayOutputStream = ByteArrayOutputStream()
		val printStream = PrintStream(byteArrayOutputStream)

		try {
			System.setOut(printStream)
			block()
			System.setOut(originalSystemOut)
		}
		finally {
			if (alsoPrintToConsole) {
				println(byteArrayOutputStream.toString())
			}
		}

		return byteArrayOutputStream.toString()
	}

	private inline fun fakeUserInput(input: String, block: () -> Unit) {
		System.setIn(ByteArrayInputStream(input.toByteArray()))
		block()
		System.setIn(originalSystemIn)
	}


	@Test
	fun helloWorld() {

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.helloWorlds))

		assertEquals(
			"""
				Hello World
				Hello World
				Hello World
				
			""".trimIndent(),
			collectSystemOut {
				CrescentVM(listOf(file), file).invoke()
			}
		)
	}

	@Test
	fun argsHelloWorld() {

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.argsHelloWorld))

		assertEquals(
			"Hello World\n",
			collectSystemOut {
				CrescentVM(listOf(file), file).invoke(listOf("Hello World"))
			}
		)
	}


	@Test
	fun funThing() {

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.funThing))

		assertEquals(
			"""
				I am a fun thing :)
				Meow
				Meow
				Meow
				Meow
				Meow
				
			""".trimIndent(),
			collectSystemOut {
				CrescentVM(listOf(file), file).invoke()
			}
		)
	}

	@Test
	fun ifStatement() {

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.ifStatement))

		assertEquals(
			"Meow\n",
			collectSystemOut {
				CrescentVM(listOf(file), file).invoke(listOf("true"))
			}
		)

		assertEquals(
			"Hiss\n",
			collectSystemOut {
				CrescentVM(listOf(file), file).invoke(listOf("false"))
			}
		)
	}

	@Test
	fun ifInputStatement() {

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.ifInputStatement))

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

	@Test
	fun stringInterpolation() {

		val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.stringInterpolation))

		println(file.mainFunction?.innerCode)
		assertEquals(
			"""
				000
				Hello 000 Hello
				Hello 0 Hello 0 Hello 0 Hello
				000
				Hello 000 Hello
				Hello 0Hello0Hello0 Hello
				$
				${'$'}x
				
			""".trimIndent(),
			collectSystemOut {
				CrescentVM(listOf(file), file).invoke()
			}
		)
	}


}