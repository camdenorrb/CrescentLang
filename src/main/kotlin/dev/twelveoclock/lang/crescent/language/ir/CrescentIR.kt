package dev.twelveoclock.lang.crescent.language.ir

// TODO: Keep track of function positions
// TODO: Add line numbers


@JvmInline
value class SectionedCrescentIR(val sections: Map<Section, Map<String, List<CrescentIR.Command>>>) {

	companion object {

		fun from(crescentIR: CrescentIR): SectionedCrescentIR {

			var lastSection: Section? = null
			var lastName: String? = null

			var sectionCommands = mutableListOf<CrescentIR.Command>()
			val sections = mutableMapOf<Section, MutableMap<String, List<CrescentIR.Command>>>()

			crescentIR.commands.forEach {
				when (it) {

					is CrescentIR.Command.Fun -> {

						if (lastSection != null) {
							sections.getOrPut(lastSection!!) { mutableMapOf() }[lastName!!] = sectionCommands
							sectionCommands = mutableListOf()
						}

						lastSection = Section.FUNCTION
						lastName = it.name
					}

					is CrescentIR.Command.Struct -> {

						if (lastSection != null) {
							sections.getOrPut(lastSection!!) { mutableMapOf() }[lastName!!] = sectionCommands
							sectionCommands = mutableListOf()
						}

						lastSection = Section.STRUCT
						lastName = it.name
					}

					else -> {
						sectionCommands.add(it)
					}
				}
			}

			if (lastSection != null) {
				sections.getOrPut(lastSection!!) { mutableMapOf() }[lastName!!] = sectionCommands
				sectionCommands = mutableListOf()
			}

			return SectionedCrescentIR(sections)
		}

	}

	enum class Section {
		STRUCT,
		FUNCTION,
	}

}

@JvmInline
value class CrescentIR(val commands: List<Command>) {

	// Function name -> Position
	/*
	val functions = commands.mapIndexedNotNull { index, command ->
		val function = command as? Command.Fun ?: return@mapIndexedNotNull null
		function.name to index
	}.toMap()
	*/

	sealed interface Command {

		// These aren't needed, in fact loops aren't needed in the IR, just if statements and jumps
		/*
		object Continue : Command {
			override fun toString(): String {
				return "continue"
			}
		}

		object Break : Command {
			override fun toString(): String {
				return "break"
			}
		}
		*/

		object AddAssign : Command {
			override fun toString(): String {
				return "addAssign"
			}
		}

		object IsLesser : Command {
			override fun toString(): String {
				return "isLesser"
			}
		}

		object IsGreater : Command {
			override fun toString(): String {
				return "isGreater"
			}
		}


		object IsLesserOrEqual : Command {
			override fun toString(): String {
				return "isLesserOrEqual"
			}
		}

		object IsGreaterOrEqual : Command {
			override fun toString(): String {
				return "isGreaterOrEqual"
			}
		}

		object IsEqual : Command {
			override fun toString(): String {
				return "isEqual"
			}
		}

		object IsNotEqual : Command {
			override fun toString(): String {
				return "isNotEqual"
			}
		}

		object AndCompare : Command {
			override fun toString(): String {
				return "andCompare"
			}
		}

		object OrCompare : Command {
			override fun toString(): String {
				return "orCompare"
			}
		}

		object Add : Command {
			override fun toString(): String {
				return "add"
			}
		}

		object Sub : Command {
			override fun toString(): String {
				return "sub"
			}
		}

		object Div : Command{
			override fun toString(): String {
				return "div"
			}
		}

		object Mul : Command {
			override fun toString(): String {
				return "mul"
			}
		}

		object Rem : Command {
			override fun toString(): String {
				return "rem"
			}
		}

		object Or : Command {
			override fun toString(): String {
				return "or"
			}
		}

		object Xor : Command {
			override fun toString(): String {
				return "xor"
			}
		}

		object And : Command {
			override fun toString(): String {
				return "and"
			}
		}

		object ShiftLeft : Command {
			override fun toString(): String {
				return "shl"
			}
		}

		object ShiftRight : Command {
			override fun toString(): String {
				return "shr"
			}
		}

		object UnsignedShiftRight : Command {
			override fun toString(): String {
				return "ushr"
			}
		}

		// Value got by popping last value
		object Assign : Command {
			override fun toString(): String {
				return "assign"
			}
		}


		@JvmInline
		value class Fun(
			val name: String,
		) : Command {
			override fun toString(): String {
				return "fun $name"
			}
		}

		@JvmInline
		value class Struct(
			val name: String,
		) : Command {
			override fun toString(): String {
				return "struct $name"
			}
		}

		// Should also be used on return
		// TODO: Take in a list of values
		@JvmInline
		value class Push(
			val value: Any,
		) : Command {
			override fun toString(): String {
				return "push $value"
			}
		}

		/*
		@JvmInline
		value class PushNamedValue(
			val name: String,
		) : Command {
			override fun toString(): String {
				return "pushName $name"
			}
		}
		*/

		@JvmInline
		value class Jump(
			val position: Int,
		) : Command {
			override fun toString(): String {
				return "jump $position"
			}
		}

		@JvmInline
		value class JumpIf(
			val position: Int,
		) : Command {
			override fun toString(): String {
				return "jumpIf $position"
			}
		}

		@JvmInline
		value class JumpIfFalse(
			val position: Int,
		) : Command {
			override fun toString(): String {
				return "jumpIfFalse $position"
			}
		}

		// Maybe take libraries in the constructor for CrescentIR
		@JvmInline
		value class LoadLibrary(
			val name: String,
		) : Command {
			override fun toString(): String {
				return "loadLibrary $name"
			}
		}

		// Gets args from previously pushed values
		@JvmInline
		value class Invoke(
			val name: String,
		) : Command {
			override fun toString(): String {
				return "invoke $name"
			}
		}

		// Uses pushed values to create an instance
		@JvmInline
		value class CreateInstance(
			val structName: String,
		) : Command {
			override fun toString(): String {
				return "createInstance $structName"
			}
		}

	}

}

/*
	fun main
	push "Hello World"
	invoke "println"
*/