package dev.twelveoclock.lang.crescent.vm

/*
import dev.twelveoclock.lang.crescent.language.ast.CrescentAST
import dev.twelveoclock.lang.crescent.language.token.CrescentToken
import tech.poder.ir.commands.SysCommand
import tech.poder.ir.data.CodeBuilder
import tech.poder.ir.data.base.Package
import tech.poder.ir.data.storage.Label
import tech.poder.ir.metadata.Visibility
import java.util.*

object CrescentToPTIR {

    fun craft(astFile: CrescentAST.Node.File): Package {
        val package_ = Package(astFile.path.fileName.toString(), Visibility.PUBLIC)
        astFile.functions.forEach { (_, u) ->
            package_.newFloatingMethod(u.name, Visibility.PUBLIC) {
                TODO()
            }
            package_.objects
        }
        return package_
    }

    private fun nodeToCode(
	    builder: CodeBuilder,
	    node: CrescentAST.Node,
	    before: Label?,
	    after: Label?,
	    identifierStack: Stack<String>
    ) {
        when (node) {
            is CrescentAST.Node.Return -> {
                builder.return_()
            }
            is CrescentAST.Node.Statement.Block -> {
                node.nodes.forEach {
                    nodeToCode(builder, it, before, after, identifierStack)
                }
            }
            is CrescentToken.Operator -> {
                when (node) {
                    CrescentToken.Operator.ADD -> {
                        builder.add()
                    }
                    CrescentToken.Operator.SUB -> {
                        builder.sub()
                    }
                    CrescentToken.Operator.MUL -> {
                        builder.mul()
                    }
                    CrescentToken.Operator.DIV -> {
                        builder.div()
                    }
                    CrescentToken.Operator.GREATER_COMPARE -> {
                        val afterL = builder.newLabel()
                        val elseL = builder.newLabel()
                        builder.ifGreaterThan(elseL)
                        builder.push(1)
                        builder.jmp(afterL)
                        builder.placeLabel(elseL)
                        builder.push(0)
                        builder.placeLabel(afterL)
                    }
                    CrescentToken.Operator.GREATER_EQUALS_COMPARE -> {
                        val afterL = builder.newLabel()
                        val elseL = builder.newLabel()
                        builder.ifGreaterThanEqual(elseL)
                        builder.push(1)
                        builder.jmp(afterL)
                        builder.placeLabel(elseL)
                        builder.push(0)
                        builder.placeLabel(afterL)
                    }
                    CrescentToken.Operator.LESSER_COMPARE -> {
                        val afterL = builder.newLabel()
                        val elseL = builder.newLabel()
                        builder.ifLessThan(elseL)
                        builder.push(1)
                        builder.jmp(afterL)
                        builder.placeLabel(elseL)
                        builder.push(0)
                        builder.placeLabel(afterL)
                    }
                    CrescentToken.Operator.LESSER_EQUALS_COMPARE -> {
                        val afterL = builder.newLabel()
                        val elseL = builder.newLabel()
                        builder.ifLessThanEqual(elseL)
                        builder.push(1)
                        builder.jmp(afterL)
                        builder.placeLabel(elseL)
                        builder.push(0)
                        builder.placeLabel(afterL)
                    }
                    CrescentToken.Operator.NOT -> {
                        val afterL = builder.newLabel()
                        val elseL = builder.newLabel()
                        builder.push(false)
                        builder.ifEquals(elseL)
                        builder.push(0)
                        builder.jmp(afterL)
                        builder.placeLabel(elseL)
                        builder.push(1)
                        builder.placeLabel(afterL)
                    }
                    CrescentToken.Operator.NOT_EQUALS_COMPARE -> {
                        val afterL = builder.newLabel()
                        val elseL = builder.newLabel()
                        builder.ifNotEquals(elseL)
                        builder.push(1)
                        builder.jmp(afterL)
                        builder.placeLabel(elseL)
                        builder.push(0)
                        builder.placeLabel(afterL)
                    }
                    CrescentToken.Operator.EQUALS_COMPARE -> {
                        val afterL = builder.newLabel()
                        val elseL = builder.newLabel()
                        builder.ifEquals(elseL)
                        builder.push(1)
                        builder.jmp(afterL)
                        builder.placeLabel(elseL)
                        builder.push(0)
                        builder.placeLabel(afterL)
                    }
                    CrescentToken.Operator.BIT_SHIFT_RIGHT -> builder.signedShiftRight()
                    CrescentToken.Operator.BIT_SHIFT_LEFT -> builder.signedShiftLeft()
                    CrescentToken.Operator.UNSIGNED_BIT_SHIFT_RIGHT -> builder.unsignedShiftRight()
                    CrescentToken.Operator.BIT_OR -> builder.or()
                    CrescentToken.Operator.BIT_XOR -> builder.xor()
                    CrescentToken.Operator.BIT_AND -> builder.and()
                    else -> error("Unknown operator: $node")
                }
            }
            is CrescentAST.Node.Primitive.Char -> {
                builder.push(node.data)
            }
            is CrescentAST.Node.Primitive.String -> {
                builder.push(node.data)
            }
            is CrescentAST.Node.Primitive.Boolean -> {
                builder.push(node.data)
            }
            is CrescentAST.Node.Primitive.Number -> {
                builder.push(node.toKotlinNumber())
            }
            is CrescentAST.Node.Expression -> {
                node.nodes.forEach {
                    nodeToCode(builder, it, before, after, identifierStack)
                }
            }
            is CrescentAST.Node.GetCall -> {
                node.arguments.forEach {
                    nodeToCode(builder, it, before, after, identifierStack)
                }
                builder.getVar(node.identifier)
                builder.getArrayItem()
            }
            is CrescentAST.Node.IdentifierCall -> {
                node.arguments.reversed().forEach { arg ->
                    nodeToCode(builder, arg, before, after, identifierStack)
                }

                when (node.identifier) {
                    "println" -> {
                        builder.push("\n")
                        builder.add()
                        builder.sysCall(SysCommand.PRINT)
                    }
                    "print" -> {
                        builder.sysCall(SysCommand.PRINT)
                    }
                    else -> {
                        TODO() //builder.invokeMethod("static." + node.identifier, node.arguments.size, false)
                    }
                }
            }
            is CrescentAST.Node.Statement.If -> {
                val afterLabel =
                    if (node.elseBlock == null) {
                        null
                    } else {
                        builder.newLabel()
                    }
                val elseLabel = builder.newLabel()
                nodeToCode(builder, node.predicate, before, after, identifierStack)
                builder.push(0)
                builder.ifNotEquals(elseLabel)
                nodeToCode(builder, node.block, before, after, identifierStack)
                if (afterLabel != null) {
                    builder.jmp(afterLabel)
                }
                builder.placeLabel(elseLabel)
                if (afterLabel != null) {
                    nodeToCode(builder, node.elseBlock!!, before, after, identifierStack)
                    builder.placeLabel(afterLabel)
                }
            }
            is CrescentAST.Node.Statement.While -> {
                val afterLabel = builder.newLabel()
                val beforeLabel = builder.newLabel()
                builder.placeLabel(beforeLabel)
                nodeToCode(builder, node.predicate, before, after, identifierStack)
                builder.push(0)
                builder.ifNotEquals(afterLabel)
                nodeToCode(builder, node.block, beforeLabel, afterLabel, identifierStack)
                builder.jmp(beforeLabel)
                builder.placeLabel(afterLabel)
            }
            is CrescentAST.Node.Statement.For -> {
                val data = Array(node.identifiers.size) {
                    Triple<Label?, Label?, String>(null, null, "")
                }
                repeat(node.identifiers.size) {
                    val name = node.identifiers[it].name
                    val range = node.ranges[it]
                    nodeToCode(builder, range.start, before, after, identifierStack)
                    builder.setVar(name)
                    nodeToCode(builder, range.start, before, after, identifierStack)
                    nodeToCode(builder, range.end, before, after, identifierStack)
                    val tmpElse = builder.newLabel()
                    builder.push(true)
                    val goesUp = "\$_Goes-Up%$name"
                    builder.setVar(goesUp)
                    builder.ifGreaterThan(tmpElse)
                    builder.push(false)
                    builder.setVar(goesUp)
                    builder.placeLabel(tmpElse)
                    val afterLabel = builder.newLabel()
                    val beforeLabel = builder.newLabel()
                    builder.placeLabel(beforeLabel)
                    builder.getVar(name)
                    nodeToCode(builder, range.end, before, after, identifierStack)
                    builder.ifNotEquals(afterLabel)
                    data[it] = Triple(beforeLabel, afterLabel, goesUp)
                }
                val last = data.last()
                nodeToCode(builder, node.block, last.first, last.second, identifierStack)
                repeat(node.identifiers.size) {
                    val backwards = (node.identifiers.size - it) - 1
                    val name = node.identifiers[backwards].name
                    val storage = data[backwards]
                    builder.getVar(name)
                    val tmpElse = builder.newLabel()
                    val tmpAfter = builder.newLabel()
                    builder.getVar(storage.third)
                    builder.push(0)
                    builder.ifNotEquals(tmpElse)
                    builder.inc()
                    builder.jmp(tmpAfter)
                    builder.placeLabel(tmpElse)
                    builder.dec()
                    builder.placeLabel(tmpAfter)
                    builder.setVar(name)
                    builder.jmp(storage.first!!)
                    builder.placeLabel(storage.second!!)
                }
            }
            is CrescentAST.Node.Identifier -> {
                identifierStack.push(node.name)
            }
            is CrescentAST.Node.Variable.Basic -> {
                nodeToCode(builder, node.value, before, after, identifierStack)
                builder.setVar(node.name)
            }
            else -> error("Unknown Node: ${node::class.java}")
        }
    }
}*/