package me.camdenorrb.crescentvm

import jdk.incubator.foreign.MemorySegment
import me.camdenorrb.crescentvm.data.TestCode
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.utils.collectSystemOut
import me.camdenorrb.crescentvm.utils.fakeUserInput
import me.camdenorrb.crescentvm.vm.CrescentToPTIR
import me.camdenorrb.crescentvm.vm.CrescentVM
import tech.poder.ir.Machine
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PTIRTests {

    @Test
    fun argsHelloWorld() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.argsHelloWorld))
        val result = CrescentToPTIR.craft(file)
        val vm = Machine()
        vm.loadPackage(result)
        assertEquals(
            "Hello World\n",
            collectSystemOut {
                vm.execute("static.main", "Hello World")
            }
        )
    }

    @Test
    fun maths() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.math))
        val result = CrescentToPTIR.craft(file)
        val vm = Machine()
        vm.loadPackage(result)
        vm.execute("static.main")
    }

    @Test
    fun tree() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.triangleRecursion))
        val result = CrescentToPTIR.craft(file)
        val vm = Machine()
        vm.loadPackage(result)
        collectSystemOut {
            vm.execute("static.main")
        }
    }

    @Test
    fun helloWorld() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.helloWorlds))
        val result = CrescentToPTIR.craft(file)
        val vm = Machine()
        vm.loadPackage(result)
        assertEquals(
            """
                Hello World
                Hello World
                Hello World
                
            """.trimIndent(),
            collectSystemOut {
                vm.execute("static.main")
            }
        )
    }

    @Test
    fun whileLoop() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.whileLoop))
        val result = CrescentToPTIR.craft(file)
        val vm = Machine()
        vm.loadPackage(result)
        vm.execute("static.main")
    }

    @Test
    fun forLoop() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.forLoop2))
        val result = CrescentToPTIR.craft(file)
        val vm = Machine()
        vm.loadPackage(result)
        vm.execute("static.main")
    }

    @Test
    fun funThing() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.funThing))
        val result = CrescentToPTIR.craft(file)
        val vm = Machine()
        vm.loadPackage(result)
        assertEquals(
            "I am a fun thing :)\n",
            collectSystemOut {
                vm.execute("static.main")
            }
        )
    }

    @Test
    fun ifStatement() {
        val file = CrescentParser.invoke(Path("example.crescent"), CrescentLexer.invoke(TestCode.ifStatement))
        val result = CrescentToPTIR.craft(file)
        val vm = Machine()
        vm.loadPackage(result)
        assertEquals(
            "Meow\n",
            collectSystemOut {
                vm.execute("static.main", "true")
            }
        )

        assertEquals(
            "Hiss\n",
            collectSystemOut {
                vm.execute("static.main", "false")
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

    @Test
    fun incubatorTest() {
        MemorySegment.allocateNative(2)
    }


}