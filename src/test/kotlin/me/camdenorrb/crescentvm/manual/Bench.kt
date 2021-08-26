package me.camdenorrb.crescentvm.manual

import me.camdenorrb.crescentvm.data.TestCode
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.system.measureNanoTime

internal object Bench {

	const val DEFAULT_CYCLES = 5_000_000

	val filePath = Path.of("example.crescent")

	val lexerBenchmark = Benchmark("Lexer")

	val parserBenchmark = Benchmark("Parser")

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


	@JvmStatic
	fun main(args: Array<String>) {

		benchCode("Hello World", TestCode.helloWorlds)
		benchCode("If Statement", TestCode.ifStatement)
		benchCode("If Input Statement", TestCode.ifInputStatement)
		benchCode("Calculator", TestCode.calculator)
		benchCode("Constants and Objects", TestCode.constantsAndObject)
		benchCode("Impl", TestCode.impl)
		benchCode("Math", TestCode.math)
		benchCode("Sealed", TestCode.sealed)
		benchCode("Enum", TestCode.enum)
		benchCode("Comments", TestCode.comments)
		benchCode("Imports", TestCode.imports)

		Benchmark("")
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


		data class Result(
			val name: String,
			val subName: String,
			val state: State,
			val totalNS: Long,
			val averageNS: Long
		) {

			override fun toString(): String {
				return (
				"""
                |$name - $subName - $state Average: ${averageNS}ns/op
                """.trimMargin())
			}

		}

		enum class State {
			WARMUP,
			BENCH
		}

	}

}