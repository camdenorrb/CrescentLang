package me.camdenorrb.crescentvm.vm

import me.camdenorrb.crescentvm.language.ir.CrescentIR
import me.camdenorrb.crescentvm.language.ir.SectionedCrescentIR
import me.camdenorrb.crescentvm.parsers.CrescentIRParser
import me.camdenorrb.crescentvm.project.extensions.minimize
import java.util.*


fun main() {

	/*
	val code =
		"""
			fun main
			push false
			jumpIfFalse 5
			push Boo
			invoke println
			jump 7
			push Yay
			invoke println
			push Meow
			invoke println
		""".trimIndent()
		*/

	val code =
		"""
			fun main
			push 1
			assign index
			pushName index
			push 1
			add
			assign index
			pushName index
			invoke println
			pushName index
			
			jumpIf 2
		""".trimIndent()

	CrescentIRVM(CrescentIRParser.invoke(code)).invoke()
}

// TODO: Memory leak checking
class CrescentIRVM(crescentIR: CrescentIR) {

	val sectionedCrescentIR = SectionedCrescentIR.from(crescentIR)


	fun invoke() {
		runFunction("main", LinkedList<Any>())
	}

	fun runFunction(name: String, stack: LinkedList<Any>) {

		val functionCode = sectionedCrescentIR.sections.getValue(SectionedCrescentIR.Section.FUNCTION).getValue(name)


		// Name -> Value
		val namedValues = mutableMapOf<String, Any>()

		var index = 0

		while (index < functionCode.size) {

			when (val node = functionCode[index]) {

				is CrescentIR.Command.Push -> {
					stack.push(node.value)
				}

				is CrescentIR.Command.Fun -> {
					break
				}

				is CrescentIR.Command.Sub -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					stack.push(((pop2 as Number).toDouble() - (pop1 as Number).toDouble()).minimize())
				}

				is CrescentIR.Command.Div -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					stack.push(((pop2 as Number).toDouble() / (pop1 as Number).toDouble()).minimize())
				}

				is CrescentIR.Command.Mul -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					stack.push(((pop2 as Number).toDouble() * (pop1 as Number).toDouble()).minimize())
				}

				is CrescentIR.Command.Add -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					when (pop2) {
						is Number -> stack.push((pop2.toDouble() + (pop1 as Number).toDouble()).minimize())
						//is String -> stack.push(pop2.toString() + pop1.toString())
						else -> stack.push(pop2.toString() + pop1.toString())
					}
				}

				is CrescentIR.Command.ShiftLeft -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					when (pop2) {
						is Number -> stack.push((pop2.toInt() shl (pop1 as Number).toInt()).minimize())
						else -> error("Unexpected node type: $pop2")
					}
				}

				is CrescentIR.Command.ShiftRight -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					when (pop2) {
						is Number -> stack.push((pop2.toInt() shr (pop1 as Number).toInt()).minimize())
						else -> error("Unexpected node type: $pop2")
					}
				}

				is CrescentIR.Command.UnsignedShiftRight -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					when (pop2) {
						is Number -> stack.push((pop2.toInt() ushr (pop1 as Number).toInt()).minimize())
						else -> error("Unexpected node type: $pop2")
					}
				}

				is CrescentIR.Command.Jump -> {
					index = node.position
					continue
				}

				is CrescentIR.Command.JumpIf -> {

					if (stack.pop() as Boolean) {
						index = node.position
					}

					continue
				}

				is CrescentIR.Command.JumpIfFalse -> {

					if (!(stack.pop() as Boolean)) {
						index = node.position
					}

					continue
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

			index++
		}
	}
}