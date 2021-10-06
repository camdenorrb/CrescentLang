package me.camdenorrb.crescentvm.compiler

import me.camdenorrb.crescentvm.iterator.PeekingNodeIterator
import me.camdenorrb.crescentvm.language.ast.CrescentAST
import me.camdenorrb.crescentvm.language.ast.CrescentAST.Node.*
import me.camdenorrb.crescentvm.language.ir.CrescentIR
import me.camdenorrb.crescentvm.language.token.CrescentToken
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.project.extensions.minimize
import me.camdenorrb.crescentvm.vm.CrescentIRVM
import kotlin.io.path.Path

object CrescentIRCompiler {

	@JvmStatic
	fun main(args: Array<String>) {

		val code =
			"""
				fun thing(mew1 mew2: String) {
					println(mew1)
					println(mew2)
					println(mew1 + mew2)
				}
				
				fun main {
					println("Meow" + 1)
					println(1 + 2 * 4 / 2)
					println("Meow" + 1)
				    println("Meow" + 1)
					thing("Meow1", "Meow2")
				}
			""".trimIndent()

		val crescentIR = invoke(CrescentParser.invoke(Path("meow.crescent"), CrescentLexer.invoke(code)))
		crescentIR.commands.forEach { println(it) }
		println()
		CrescentIRVM(crescentIR).invoke()
	}

	// TODO: Maybe have multiple files of CrescentIR?
	fun invoke(file: File): CrescentIR {

		val commandsOutput = mutableListOf<CrescentIR.Command>()

		file.functions.forEach { (name, function) ->

			commandsOutput.add(CrescentIR.Command.Fun(name))

			function.params.forEach {
				commandsOutput.add(CrescentIR.Command.Assign(it.name))
			}

			function.innerCode.nodes.forEach {
				compileNode(it, commandsOutput)
			}
		}

		return CrescentIR(commandsOutput)
	}


	// TODO: Take in named values in a reassignable way
	private fun compileNode(node: CrescentAST.Node, commandsOutput: MutableList<CrescentIR.Command>) {
		when (node) {

			is Primitive.Char -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.String -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.I8 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.I16 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.I32 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.I64 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.U8 -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.U16 -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.U32 -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.U64 -> commandsOutput.add(CrescentIR.Command.Push(node.data))
			is Primitive.Number.F32 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))
			is Primitive.Number.F64 -> commandsOutput.add(CrescentIR.Command.Push(node.data.minimize()))

			is Identifier -> commandsOutput.add(CrescentIR.Command.PushName(node.name))
			is Expression -> compileExpression(node, commandsOutput)


			is IdentifierCall -> {

				node.arguments.asReversed().forEach {
					compileNode(it, commandsOutput)
				}

				commandsOutput.add(CrescentIR.Command.Invoke(node.identifier))
			}
		}
	}

	private fun compileExpression(expression: Expression, commandsOutput: MutableList<CrescentIR.Command>) {

		val nodeIterator = PeekingNodeIterator(expression.nodes)

		while (nodeIterator.hasNext()) {
			when (val node = nodeIterator.next()) {

				is CrescentToken.Operator -> {
					compileOperator(node, commandsOutput)
				}

				is Primitive.String,
				is Primitive.Number,
				is Primitive.Char,
				is Primitive.Boolean,
				is CrescentAST.Node.Array,
				is Identifier,
				is GetCall,
				is Statement.If,
				is IdentifierCall,
				-> {
					compileNode(node, commandsOutput)
				}

				else -> error("Unexpected node: $node")
			}
		}
	}

	private fun compileOperator(operator: CrescentToken.Operator, commandsOutput: MutableList<CrescentIR.Command>) {
		when (operator) {

			CrescentToken.Operator.NOT -> TODO()
			CrescentToken.Operator.ADD -> commandsOutput.add(CrescentIR.Command.Add)
			CrescentToken.Operator.SUB -> commandsOutput.add(CrescentIR.Command.Sub)
			CrescentToken.Operator.MUL -> commandsOutput.add(CrescentIR.Command.Mul)
			CrescentToken.Operator.DIV -> commandsOutput.add(CrescentIR.Command.Div)

			CrescentToken.Operator.BIT_SHIFT_RIGHT -> commandsOutput.add(CrescentIR.Command.ShiftRight)
			CrescentToken.Operator.BIT_SHIFT_LEFT -> commandsOutput.add(CrescentIR.Command.ShiftLeft)
			CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT -> commandsOutput.add(CrescentIR.Command.UnsignedShiftRight)
			CrescentToken.Operator.BIT_OR -> commandsOutput.add(CrescentIR.Command.Or)
			CrescentToken.Operator.BIT_AND -> commandsOutput.add(CrescentIR.Command.And)
			CrescentToken.Operator.BIT_XOR -> commandsOutput.add(CrescentIR.Command.Xor)


			CrescentToken.Operator.POW -> {
				TODO("Mooo ADD POW?!?!@?! D:")
			}
			CrescentToken.Operator.REM -> {
				TODO("Mooo ADD remainder?!?!@?! D:")
			}

			CrescentToken.Operator.ASSIGN -> {

				//codeBuilder.setField()
				TODO("Figure out")
			}

			CrescentToken.Operator.ADD_ASSIGN -> {
				TODO("Figure out")
			}

			CrescentToken.Operator.SUB_ASSIGN -> TODO("Figure out")
			CrescentToken.Operator.MUL_ASSIGN -> TODO("Figure out")
			CrescentToken.Operator.DIV_ASSIGN -> TODO("Figure out")
			CrescentToken.Operator.REM_ASSIGN -> TODO("Figure out")
			CrescentToken.Operator.POW_ASSIGN -> TODO("Figure out")

			CrescentToken.Operator.OR_COMPARE -> TODO()
			CrescentToken.Operator.AND_COMPARE -> TODO()
			CrescentToken.Operator.EQUALS_COMPARE -> TODO()
			CrescentToken.Operator.LESSER_EQUALS_COMPARE -> TODO()
			CrescentToken.Operator.GREATER_EQUALS_COMPARE -> TODO()
			CrescentToken.Operator.LESSER_COMPARE -> TODO()
			CrescentToken.Operator.GREATER_COMPARE -> TODO()
			CrescentToken.Operator.EQUALS_REFERENCE_COMPARE -> TODO()
			CrescentToken.Operator.NOT_EQUALS_COMPARE -> TODO()
			CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE -> TODO()
			CrescentToken.Operator.CONTAINS -> TODO()
			CrescentToken.Operator.RANGE_TO -> TODO()
			CrescentToken.Operator.TYPE_PREFIX -> TODO()
			CrescentToken.Operator.RETURN -> TODO()
			CrescentToken.Operator.RESULT -> TODO()
			CrescentToken.Operator.INSTANCE_OF -> TODO()
			CrescentToken.Operator.NOT_INSTANCE_OF -> TODO()

			CrescentToken.Operator.AS -> {
				/* Ignore */
			}

			else -> error("Unexpected operator: $operator")
		}
	}

}