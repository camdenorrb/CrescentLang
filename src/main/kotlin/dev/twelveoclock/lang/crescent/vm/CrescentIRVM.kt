package dev.twelveoclock.lang.crescent.vm

import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.ir.CrescentIR
import dev.twelveoclock.lang.crescent.language.ir.SectionedCrescentIR
import dev.twelveoclock.lang.crescent.parsers.CrescentIRParser
import dev.twelveoclock.lang.crescent.project.extensions.minimize
import java.util.*


fun main() {

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

		fun resolveValue(node: Any): Any {
			return if (node is CrescentAST.Node.Identifier) {
				namedValues[node.name]!!
			}
			else {
				node
			}
		}

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

					val pop1 = resolveValue(stack.pop())
					val pop2 = resolveValue(stack.pop())

					stack.push(((pop2 as Number).toDouble() * (pop1 as Number).toDouble()).minimize())
				}

				is CrescentIR.Command.Add -> {

					val pop1 = resolveValue(stack.pop())
					val pop2 = resolveValue(stack.pop())

					when (pop2) {
						is Number -> stack.push((pop2.toDouble() + (pop1 as Number).toDouble()).minimize())
						//is String -> stack.push(pop2.toString() + pop1.toString())
						else -> stack.push(pop2.toString() + pop1.toString())
					}
				}

				is CrescentIR.Command.ShiftLeft -> {

					val pop1 = resolveValue(stack.pop())
					val pop2 = resolveValue(stack.pop())

					when (pop2) {
						is Number -> stack.push((pop2.toInt() shl (pop1 as Number).toInt()).minimize())
						else -> error("Unexpected node type: $pop2")
					}
				}

				is CrescentIR.Command.ShiftRight -> {

					val pop1 = resolveValue(stack.pop())
					val pop2 = resolveValue(stack.pop())

					when (pop2) {
						is Number -> stack.push((pop2.toInt() shr (pop1 as Number).toInt()).minimize())
						else -> error("Unexpected node type: $pop2")
					}
				}

				is CrescentIR.Command.UnsignedShiftRight -> {

					val pop1 = resolveValue(stack.pop())
					val pop2 = resolveValue(stack.pop())

					when (pop2) {
						is Number -> stack.push((pop2.toInt() ushr (pop1 as Number).toInt()).minimize())
						else -> error("Unexpected node type: $pop2")
					}
				}

				is CrescentIR.Command.IsGreater -> {

					val pop1 = resolveValue(stack.pop())
					val pop2 = resolveValue(stack.pop())

					when (pop2) {
						is Number -> stack.push((pop2.toDouble() > (pop1 as Number).toDouble()))
						else -> error("Unexpected node type: $pop2")
					}
				}

				is CrescentIR.Command.IsLesser -> {

					val pop1 = resolveValue(stack.pop())
					val pop2 = resolveValue(stack.pop())

					when (pop2) {
						is Number -> stack.push((pop2.toDouble() < (pop1 as Number).toDouble()))
						else -> error("Unexpected node type: ${pop2::class}")
					}
				}

				is CrescentIR.Command.Jump -> {
					// Minus one since it's sectioned and jump takes into account `fun main`
					index = node.position - 1
					continue
				}

				is CrescentIR.Command.JumpIf -> {
					if (resolveValue(stack.pop()) as Boolean) {
						// Minus one since it's sectioned and jump takes into account `fun main`
						index = node.position - 1
						continue
					}
				}

				is CrescentIR.Command.JumpIfFalse -> {
					if (!(resolveValue(stack.pop()) as Boolean)) {
						// Minus one since it's sectioned and jump takes into account `fun main`
						index = node.position - 1
						continue
					}
				}

				is CrescentIR.Command.AddAssign -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					val pop2Value = resolveValue(pop2)

					namedValues[(pop2 as CrescentAST.Node.Identifier).name] = when (pop2Value) {
						is Number -> (pop2Value.toDouble() + (pop1 as Number).toDouble()).minimize()
						//is String -> stack.push(pop2.toString() + pop1.toString())
						else -> pop2Value.toString() + pop1.toString()
					}
				}

				is CrescentIR.Command.Assign -> {

					val pop1 = stack.pop()
					val pop2 = stack.pop()

					namedValues[(pop2 as CrescentAST.Node.Identifier).name] = pop1
					//namedValues[node.name] = stack.pop()
				}

				/*
				is CrescentIR.Command.PushNamedValue -> {
					stack.push(checkNotNull(namedValues[node.name]) {
						"Could not find a named value with the name '${node.name}'"
					})
				}
				*/

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