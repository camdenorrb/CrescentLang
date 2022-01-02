package dev.twelveoclock.lang.crescent.parsers

import me.camdenorrb.crescentvm.language.ir.CrescentIR
import me.camdenorrb.crescentvm.project.extensions.minimize
import java.nio.file.Path
import kotlin.io.path.bufferedReader

object CrescentIRParser {

	fun invoke(input: String): CrescentIR {
		return invoke(input.lineSequence())
	}

	fun invoke(path: Path): CrescentIR {
		return path.bufferedReader().use {
			invoke(it.lineSequence())
		}
	}

	fun invoke(lineSequence: Sequence<String>): CrescentIR {
		return CrescentIR(lineSequence.mapTo(mutableListOf()) {

			val args = it.split(' ')

			when (args[0]) {
				"isLesserOrEqual" -> CrescentIR.Command.IsLesserOrEqual
				"isGreaterOrEqual" -> CrescentIR.Command.IsGreaterOrEqual
				"isEqual" -> CrescentIR.Command.IsEqual
				"isNotEqual" -> CrescentIR.Command.IsNotEqual
				"andCompare" -> CrescentIR.Command.AndCompare
				"orCompare" -> CrescentIR.Command.OrCompare
				"add" -> CrescentIR.Command.Add
				"sub" -> CrescentIR.Command.Sub
				"div" -> CrescentIR.Command.Div
				"mul" -> CrescentIR.Command.Mul
				"rem" -> CrescentIR.Command.Rem
				"or" -> CrescentIR.Command.Or
				"xor" -> CrescentIR.Command.Xor
				"and" -> CrescentIR.Command.And
				"shl" -> CrescentIR.Command.ShiftLeft
				"shr" -> CrescentIR.Command.ShiftRight
				"ushr" -> CrescentIR.Command.UnsignedShiftRight
				"fun" -> CrescentIR.Command.Fun(args[1])
				"push" -> CrescentIR.Command.Push(args.drop(1).joinToString(" ").asTyped())
				//"pushName" -> CrescentIR.Command.PushNamedValue(args[1])
				"jump" -> CrescentIR.Command.Jump(args[1].toInt())
				"jumpIf" -> CrescentIR.Command.JumpIf(args[1].toInt())
				"jumpIfFalse" -> CrescentIR.Command.JumpIfFalse(args[1].toInt())
				"loadLibrary" -> CrescentIR.Command.LoadLibrary(args[1])
				"invoke" -> CrescentIR.Command.Invoke(args[1])
				"assign" -> CrescentIR.Command.Assign
				else -> error("Unexpected command: ${args[0]}")
			}
		})
	}

	private fun String.asTyped(): Any {
		return toBooleanStrictOrNull()
			?: toDoubleOrNull()?.minimize()
			?: this
	}
}