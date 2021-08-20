package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.CrescentLexer
import me.camdenorrb.crescentvm.vm.CrescentParser
import java.nio.file.Path
import kotlin.system.measureNanoTime

object Bench {

	const val DEFAULT_CYCLES = 1_000_000

	val filePath = Path.of("example.crescent")

	val lexerBenchmark = Benchmark("Lexer")

	val parserBenchmark = Benchmark("Parser")


	@JvmStatic
	fun main(args: Array<String>) {
		benchCode("Hello World", TestCode.helloWorld)
		benchCode("If Statement", TestCode.ifStatement)
		benchCode("Enum", TestCode.enum)
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

		println()
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
                |$name:$subName - $state {
                |   Total: ${totalNS}ns
                |   Total Average: ${averageNS}ns/op
                |}
                """.trimMargin())
			}

		}

		enum class State {
			WARMUP,
			BENCH
		}

	}

}