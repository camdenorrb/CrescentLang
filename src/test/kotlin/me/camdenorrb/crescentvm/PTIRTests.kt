package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.data.TestCode
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentToPTIR
import me.camdenorrb.crescentvm.vm.CrescentVM
import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.special.SpecialCalls
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

    @Test
    fun forcedError() {
        Machine.clear()
		val libA = Method.create("printHelloKat") {
			it.push("Hello Kat\n")
			it.sysCall(SpecialCalls.PRINT)
		}
		val libB = Method.create("printHelloWorld") {
			it.push("\n")
			it.push("Hello World")
			it.add()
			it.sysCall(SpecialCalls.PRINT)
		}
		val main = Method.create("main") {
			val elseLabel = it.newLabel()
			val afterLabel = it.newLabel()
			it.sysCall(SpecialCalls.RANDOM_INT, -1, 1)
			it.push(0)
			it.ifEquals(elseLabel)
			it.invoke(libA)
			it.push("Forced Error Leftover") //Uneven Push/Pop!
			it.jmp(afterLabel)
			it.placeLabel(elseLabel)
			it.invoke(libB)
			it.placeLabel(afterLabel)
			it.return_()
		}
		Machine.loadCode(libA, libB, main)
        repeat(10) {
            Machine.execute("static.main")
        }
    }

    @Test
    fun argsHelloWorld() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.argsHelloWorld))
        val result = CrescentToPTIR.craft(file)
        assertEquals(
            "Hello World\n",
            collectSystemOut {
                CrescentToPTIR.execute("static.main", result, "Hello World")
            }
        )
    }

    @Test
    fun helloWorld() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.helloWorld))
        val result = CrescentToPTIR.craft(file)
        assertEquals(
            "Hello World\n",
            collectSystemOut {
                CrescentToPTIR.execute("static.main", result)
            }
        )
    }

    @Test
    fun funThing() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.funThing))
        val result = CrescentToPTIR.craft(file)
        assertEquals(
            "I am a fun thing :)\n",
            collectSystemOut {
                CrescentToPTIR.execute("static.main", result)
            }
        )
    }

    @Test
    fun ifStatement() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.ifStatement))
        val result = CrescentToPTIR.craft(file)
        assertEquals(
            "Meow\n",
            collectSystemOut {
                CrescentToPTIR.execute("static.main", result, true)
            }
        )

        assertEquals(
            "Hiss\n",
            collectSystemOut {
                CrescentToPTIR.execute("static.main", result, false)
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