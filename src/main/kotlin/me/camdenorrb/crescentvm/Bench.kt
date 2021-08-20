package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import java.nio.file.Path
import kotlin.system.measureNanoTime

object Bench {

	val lexerBenchmark = Benchmark("Lexer")
	val parserBenchmark = Benchmark("Parser")

	val filePath = Path.of("example.crescent")


	const val helloWorldCode =
		"""
			fun main {
                println("Hello World")
            }
		"""

	const val ifStatementCode =
		"""
            fun main(args: [String]) {
                if (args[0] == "true") {
                    println("Meow")
                }
                else {
                    println("Hiss")
                }
            }
        """

	const val enumCode =
		"""
            enum Color(name: String) {
                RED("Red")
                GREEN("Green")
                BLUE("Blue")
            }
            
            fun main {
            
                # .random() will be built into the Enum type implementation
            
                val color = Color.random()
            
                # Shows off cool Enum shorthand for when statements
                when(color) {
            
                    is .RED   -> { println("Meow") }
                    is .GREEN -> {}
            
                    else -> {}
                }
            
                when(name = color.name) {
            
                    "Red"   -> println(name)
                    "Green" -> {}
            
                    else -> {}
                }
            
            }
        """


	@JvmStatic
	fun main(args: Array<String>) {
		benchCode("Hello World", helloWorldCode)
		benchCode("If Statement", ifStatementCode)
		benchCode("Enum", enumCode)
	}


	fun benchCode(name: String, code: String) {

		lexerBenchmark.apply {
			bench(name) {
				CrescentLexer.invoke(code)
			}
		}

		val tokens = CrescentLexer.invoke(code)

		parserBenchmark.apply {
			bench(name) {
				CrescentParser.invoke(filePath, tokens)
			}
		}
	}


	class Benchmark(val name: String) {

		inline fun bench(subName: String, warmUpCycles: Int = DEFAULT_CYCLES, benchCycles: Int = DEFAULT_CYCLES, block: () -> Unit) {
			println(measureNS(subName, State.WARMUP, warmUpCycles, block))
			println(measureNS(subName, State.BENCH,  benchCycles,  block))
		}

		inline fun measureNS(subName: String, state: State, cycles: Int, block: () -> Unit): Result {

			var totalTimeNS = 0L

			repeat(cycles) {
				totalTimeNS += measureNanoTime(block)
			}

			return Result(name, subName, state, totalTimeNS, totalTimeNS / cycles)
		}


		data class Result(val name: String, val subName: String, val state: State, val totalNS: Long, val averageNS: Long) {

			override fun toString(): String {
				return (
				"""
                |   $name:$subName - $state {
                |       Total: ${totalNS}ns
                |       Total Average: ${averageNS}ns/op
                |   }
                """.trimMargin())
			}

		}

		enum class State {
			WARMUP,
			BENCH
		}

		companion object {
			const val DEFAULT_CYCLES = 1_000_000
		}

	}
}