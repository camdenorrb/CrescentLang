package dev.twelveoclock.lang.crescent.translators

/*
import me.camdenorrb.crescentvm.iterator.PeekingNodeIterator
import me.camdenorrb.crescentvm.language.ast.CrescentAST.Node
import me.camdenorrb.crescentvm.language.ast.CrescentAST.Node.Primitive
import me.camdenorrb.crescentvm.language.token.CrescentToken
import tech.poder.ir.api.Translator
import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.base.Package
import tech.poder.ir.data.storage.NamedType
import tech.poder.ir.data.storage.Type
import tech.poder.ir.metadata.Visibility
import kotlin.io.path.pathString

object PoderTranslator : Translator<Node.File, Package> {

	override val iClass = Node.File::class

	override val oClass = Package::class


	override fun translate(input: Node.File): Package {
		return Package(input.path.pathString, Visibility.PUBLIC).apply {
			input.functions.forEach { (_, function) ->
				newFloatingMethod(function)
			}
		}
	}


	private fun CrescentToken.Visibility.toPoder(): Visibility = when (this) {
		CrescentToken.Visibility.PRIVATE -> Visibility.PRIVATE
		CrescentToken.Visibility.INTERNAL -> Visibility.INTERNAL
		CrescentToken.Visibility.PUBLIC -> Visibility.PUBLIC
	}

	private fun Node.Type.toPoder(): Type? = when (this) {

		Node.Type.unit -> null
		Primitive.Number.I8.type -> Type.Primitive.Byte()
		Primitive.Number.I16.type -> Type.Primitive.Short()
		Primitive.Number.I32.type -> Type.Primitive.Int()
		Primitive.Number.I64.type -> Type.Primitive.Long()
		Primitive.Number.U8.type -> Type.Primitive.Byte()
		Primitive.Number.U16.type -> Type.Primitive.Short()
		Primitive.Number.U32.type -> Type.Primitive.Int()
		Primitive.Number.U64.type -> Type.Primitive.Long()
		Primitive.Number.F32.type -> Type.Primitive.Float()
		Primitive.Number.F64.type -> Type.Primitive.Double()

		is Node.Type.Basic -> Type.Struct(this.name, emptyList())

		else -> error("Unexpected type: $this")
	}

	private fun Node.Parameter.toPoder(): NamedType = when (this) {
		is Node.Parameter.Basic -> NamedType(name, type.toPoder()!!)
		else -> error("Unknown parameter $this")
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

	private fun compileNode(codeBuilder: CodeBuilder, node: Node): Unit = when (node) {

		is Primitive.Char -> codeBuilder.push(node.data)
		is Primitive.String -> codeBuilder.push(node.data)
		is Primitive.Number.I8 -> codeBuilder.push(node.data)
		is Primitive.Number.I16 -> codeBuilder.push(node.data)
		is Primitive.Number.I32 -> codeBuilder.push(node.data)
		is Primitive.Number.I64 -> codeBuilder.push(node.data)
		is Primitive.Number.U8 -> codeBuilder.push(node.data)
		is Primitive.Number.U16 -> codeBuilder.push(node.data)
		is Primitive.Number.U32 -> codeBuilder.push(node.data)
		is Primitive.Number.U64 -> codeBuilder.push(node.data)
		is Primitive.Number.F32 -> codeBuilder.push(node.data)
		is Primitive.Number.F64 -> codeBuilder.push(node.data)

		is Node.Expression -> compileExpression(codeBuilder, node)

		is Node.IdentifierCall -> {

			node.arguments.asReversed().forEach {
				compileNode(codeBuilder, it)
			}

			when (node.identifier) {

				"print" -> {
					codeBuilder.sysCall(SysCommand.PRINT)
				}

				"println" -> {
					// TODO: codeBuilder.push('\n')
					codeBuilder.push("\n")
					codeBuilder.add()
					codeBuilder.sysCall(SysCommand.PRINT)
				}

				else -> {

				}
			}
		}

		else -> error("Unexpected node: $node")
	}

	private fun compileExpression(codeBuilder: CodeBuilder, expression: Node.Expression) {

		val nodeIterator = PeekingNodeIterator(expression.nodes)

		while (nodeIterator.hasNext()) {
			when (val node = nodeIterator.next()) {

				is CrescentToken.Operator -> {
					compileOperator(codeBuilder, node)
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

	fun compileOperator(codeBuilder: CodeBuilder, operator: CrescentToken.Operator) = when (operator) {

		CrescentToken.Operator.NOT -> codeBuilder.neg()
		CrescentToken.Operator.ADD -> codeBuilder.add()
		CrescentToken.Operator.SUB -> codeBuilder.sub()
		CrescentToken.Operator.MUL -> codeBuilder.mul()
		CrescentToken.Operator.DIV -> codeBuilder.div()


		CrescentToken.Operator.BIT_SHIFT_RIGHT -> codeBuilder.signedShiftRight()
		CrescentToken.Operator.BIT_SHIFT_LEFT -> codeBuilder.signedShiftLeft()
		CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT -> codeBuilder.unsignedShiftRight()
		CrescentToken.Operator.BIT_OR -> codeBuilder.or()
		CrescentToken.Operator.BIT_AND -> codeBuilder.and()
		CrescentToken.Operator.BIT_XOR -> codeBuilder.xor()


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


*/