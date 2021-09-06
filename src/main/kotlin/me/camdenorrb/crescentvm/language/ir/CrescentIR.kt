package me.camdenorrb.crescentvm.language.ir

@JvmInline
value class CrescentIR(val commands: List<Command>) {

	sealed interface Command {

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


		@JvmInline
		value class Fun(
			val name: String
		) : Command {
			override fun toString(): String {
				return "fun $name"
			}
		}

		// Should also be used on return
		@JvmInline
		value class Push(
			val value: Any,
		) : Command {
			override fun toString(): String {
				return "push $value"
			}
		}

		@JvmInline
		value class Jump(
			val position: Int,
		) : Command {
			override fun toString(): String {
				return "jmp $position"
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

	}

}

/*
	fun main
	push "Hello World"
	invoke "println"
*/