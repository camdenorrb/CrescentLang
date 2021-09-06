package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.language.ir.CrescentIR
import java.util.*

class CrescentIRVM(val crescentIR: CrescentIR) {

	fun invoke() {

		val stack = LinkedList<Any>()

		crescentIR.commands.forEach {
			when (it) {

				is CrescentIR.Command.Push -> stack.push(it.value)

				is CrescentIR.Command.Fun -> {/* Ignore */}

				is CrescentIR.Command.Sub -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					stack.push((pop2 as Number).toDouble() - (pop1 as Number).toDouble())
				}

				is CrescentIR.Command.Div -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					stack.push((pop2 as Number).toDouble() / (pop1 as Number).toDouble())
				}

				is CrescentIR.Command.Mul -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					stack.push((pop2 as Number).toDouble() * (pop1 as Number).toDouble())
				}

				is CrescentIR.Command.Add -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					when (pop2) {
						is Number -> stack.push(pop2.toDouble() + (pop1 as Number).toDouble())
						is String -> stack.push(pop2.toString() + pop1.toString())
					}
				}

				is CrescentIR.Command.Invoke -> {
					when (it.name) {
						"print" -> print(stack.pop())
						"println" -> println(stack.pop())
					}
				}

				else -> error("Unexpected command: $it")
			}
		}
	}

}