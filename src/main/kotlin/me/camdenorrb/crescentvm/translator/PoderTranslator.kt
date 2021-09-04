package me.camdenorrb.crescentvm.translator

import me.camdenorrb.crescentvm.iterator.PeekingNodeIterator
import me.camdenorrb.crescentvm.project.checkEquals
import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentAST.Node
import me.camdenorrb.crescentvm.vm.CrescentAST.Node.Primitive
import me.camdenorrb.crescentvm.vm.CrescentToken
import me.camdenorrb.crescentvm.vm.CrescentVM
import tech.poder.ir.api.Translator
import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.base.Package
import tech.poder.ir.data.storage.Label
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type
import tech.poder.ir.metadata.Visibility
import java.util.*
import kotlin.io.path.pathString

object PoderTranslator : Translator<Node.File, Package> {

	override val iClass = Node.File::class

	override val oClass = Package::class


	override fun translate(input: Node.File): Package {

		val thePackage = Package(input.path.parent.pathString, Visibility.PUBLIC)

		input.functions.forEach { (_, function) ->
			thePackage.newFloatingMethod(function)
		}

		thePackage.newObject("", Visibility.PUBLIC).

		input.mainFunction
	}


	private fun CrescentToken.Visibility.toPoder(): Visibility {
		return when (this) {
			CrescentToken.Visibility.PRIVATE -> Visibility.PRIVATE
			CrescentToken.Visibility.INTERNAL -> Visibility.INTERNAL
			CrescentToken.Visibility.PUBLIC -> Visibility.PUBLIC
		}
	}

	private fun Node.Type.toPoder(): Type? {
		NamedType("Meow", )
	}

	private fun Node.Parameter.toPoder(): NamedType {
		return when (this) {
			is Node.Parameter.Basic -> NamedType(name, type.toPoder()!!)
			else -> error("Unknown parameter $this")
		}
	}

	private fun List<Node.Parameter>.toPoder(): Set<NamedType> {
		return mapTo(mutableSetOf()) { it.toPoder() }
	}

	private fun Package.newFloatingMethod(function: Node.Function) {
		newFloatingMethod(
			function.name,
			function.visibility.toPoder(),
			function.returnType.toPoder(),
			function.params.toPoder(),
		) {
			compileBlock(it, function.innerCode)
		}
	}

	private fun compileBlock(codeBuilder: CodeBuilder, block: Node.Statement.Block) {
		block.nodes.forEach {
			compileNode(codeBuilder, it)
		}
	}

	private fun compileNode(codeBuilder: CodeBuilder, node: Node) = when (node) {
		is Node.Expression -> compileExpression(node)
		else -> error("Unexpected node: $node")
	}

	private fun compileExpression(codeBuilder: CodeBuilder, expression: Node.Expression) {

		val stack = LinkedList<Node>()
		val nodeIterator = PeekingNodeIterator(expression.nodes)

		while (nodeIterator.hasNext()) {
			when (val node = nodeIterator.next()) {

				is CrescentToken.Operator -> {
					when (node) {

						CrescentToken.Operator.NOT -> TODO()

						// TODO: Override operators for these in Primitive.Number
						CrescentToken.Operator.ADD -> {
							codeBuilder.add()
						}
						CrescentToken.Operator.SUB -> {
							codeBuilder.sub()
						}
						CrescentToken.Operator.MUL -> {
							codeBuilder.mul()
						}
						CrescentToken.Operator.DIV -> {
							codeBuilder.div()
						}
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

						CrescentToken.Operator.OR_COMPARE -> {

						}

						CrescentToken.Operator.AND_COMPARE -> {
						}

						CrescentToken.Operator.EQUALS_COMPARE -> {
							val label = codeBuilder.newLabel()
							codeBuilder.ifEquals(label)
						}
						CrescentToken.Operator.LESSER_EQUALS_COMPARE -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data

							stack.push(Primitive.Boolean(pop2 <= pop1))
						}
						CrescentToken.Operator.GREATER_EQUALS_COMPARE -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data

							stack.push(Primitive.Boolean(pop2 >= pop1))
						}

						CrescentToken.Operator.LESSER_COMPARE -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data

							stack.push(Primitive.Boolean(pop2 < pop1))
						}
						CrescentToken.Operator.GREATER_COMPARE -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toF64().data

							stack.push(Primitive.Boolean(pop2 > pop1))
						}


						CrescentToken.Operator.EQUALS_REFERENCE_COMPARE -> TODO()

						CrescentToken.Operator.NOT_EQUALS_COMPARE -> {

							val pop1 = runNode(stack.pop(), context)
							val pop2 = runNode(stack.pop(), context)

							// TODO: Override !=, ==, >=, <=, <, >, xor, or, and, etc on number, then merging this if statement into one statement and remove pop1 and pop2
							if (pop1 is Primitive.Number && pop2 is Primitive.Number) {
								stack.push(Primitive.Boolean(pop2.toF64().data != pop1.toF64().data))
							}
							else {
								stack.push(Primitive.Boolean(pop2 != pop1))
							}
						}

						CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE -> TODO()
						CrescentToken.Operator.CONTAINS -> TODO()
						CrescentToken.Operator.RANGE_TO -> TODO()
						CrescentToken.Operator.TYPE_PREFIX -> TODO()
						CrescentToken.Operator.RETURN -> TODO()
						CrescentToken.Operator.RESULT -> TODO()
						CrescentToken.Operator.COMMA -> TODO()
						CrescentToken.Operator.DOT -> TODO()
						CrescentToken.Operator.AS -> TODO()
						CrescentToken.Operator.IMPORT_SEPARATOR -> TODO()

						CrescentToken.Operator.INSTANCE_OF -> {

							val pop1 = (runNode(stack.pop(), context) as Node.Identifier)
							val pop2 = runNode(stack.pop(), context)

							stack.push(Primitive.Boolean(pop1.name == Node.Type.any.name || "${findType(pop2)}" == pop1.name))
						}

						CrescentToken.Operator.BIT_SHIFT_RIGHT -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 shr pop1))
						}

						CrescentToken.Operator.BIT_SHIFT_LEFT -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 shl pop1))
						}

						CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 ushr pop1))
						}

						CrescentToken.Operator.BIT_OR -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 or pop1))
						}

						CrescentToken.Operator.BIT_AND -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 and pop1))
						}
						CrescentToken.Operator.BIT_XOR -> {

							val pop1 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data
							val pop2 = (runNode(stack.pop(), context) as Primitive.Number).toI32().data

							stack.push(Primitive.Number.I32(pop2 xor pop1))
						}

						CrescentToken.Operator.NOT_INSTANCE_OF -> TODO()
					}
				}

				is Primitive.String,
				is Primitive.Number,
				is Primitive.Char,
				is Primitive.Boolean,
				is Node.Array,
				is Node.Identifier,
				is Node.GetCall,
				is Node.Statement.If,
				is Node.IdentifierCall,
				-> {
					compileNode(codeBuilder, node)
				}

				else -> error("Unexpected node: $node")
			}
		}
	}


}


