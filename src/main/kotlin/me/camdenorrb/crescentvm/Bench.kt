package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import java.nio.file.Path
import kotlin.system.measureNanoTime

object Bench {

	val filePath = Path.of("example.crescent")


	const val helloWorldCode =
		"""
			fun main {
                println("Hello World")
            }
		"""


	@JvmStatic
	fun main(args: Array<String>) {

		val lexerBench = Benchmark("Lexer")
		lexerBench.bench("Hello World") {
			CrescentLexer.invoke(helloWorldCode)
		}

		val helloWorldTokens = CrescentLexer.invoke(helloWorldCode)

		val parserBench = Benchmark("Parser")
		parserBench.bench("Hello World") {
			CrescentParser.invoke(filePath, helloWorldTokens)
		}

	}

	/*
	inline fun benchmark(name: String, warmUpCycles: Int = DEFAULT_CYCLES, benchCycles: Int = DEFAULT_CYCLES, block: Benchmark.() -> Unit) {
		block(Benchmark(name))
	}
	*/

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
			const val DEFAULT_CYCLES = 10_000_000
		}

	}
}