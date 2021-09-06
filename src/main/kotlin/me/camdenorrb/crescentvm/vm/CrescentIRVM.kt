package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.language.ir.CrescentIR
import java.util.*

// TODO: Memory leak checking
class CrescentIRVM(val crescentIR: CrescentIR) {

	fun invoke() {
		runFunction("main", LinkedList<Any>())
	}

	fun runFunction(name: String, stack: LinkedList<Any>) {

		// Name -> Value
		val namedValues = mutableMapOf<String, Any>()

		val functionStart = checkNotNull(crescentIR.functions[name]) {
			"Could not find function $name"
		}

		for (i in functionStart + 1 until crescentIR.commands.size) {
			when (val node = crescentIR.commands[i]) {

				is CrescentIR.Command.Push -> {
					stack.push(node.value)
				}

				is CrescentIR.Command.Fun -> {
					break
				}

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

				is CrescentIR.Command.Assign -> {
					namedValues[node.name] = stack.pop()
				}

				is CrescentIR.Command.PushName -> {
					stack.push(checkNotNull(namedValues[node.name]) {
						"Could not find a named value with the name '${node.name}'"
					})
				}

				is CrescentIR.Command.Invoke -> {
					when (node.name) {

						"print" -> print(stack.pop())
						"println" -> println(stack.pop())
						"readLine" -> stack.push(readLine())

						else -> {
							runFunction(node.name, stack)
						}
					}
				}

				else -> error("Unexpected command: $node")
			}
		}
	}

}