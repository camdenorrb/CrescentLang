package me.camdenorrb.crescentvm.vm

import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.special.SpecialCalls
import tech.poder.ir.instructions.simple.CodeBuilder
import tech.poder.ir.std.Basic
import tech.poder.ir.vm.Machine

object CrescentToPTIR {

    fun craft(astFile: CrescentAST.Node.File): Set<Method> {
        val methods = mutableSetOf<Method>()
        astFile.functions.forEach { (_, u) ->
            methods.add(
                Method.create(
                    u.name,
                    u.params.size.toUByte(),
                    u.returnType != CrescentAST.Node.Type.Unit
                ) { builder ->
                    builder.idArgs(*u.params.map { it.name }.toTypedArray())
                    u.innerCode.nodes.forEach { node ->
                        nodeToCode(builder, node, methods)
                    }
                })
        }
        return methods
    }

    fun execute(target: String = "static.main", methods: Set<Method>, vararg args: Any) {
        Machine.clear()
        Machine.loadCode(*methods.toTypedArray())
        Machine.execute(target, *args)
    }

    private fun nodeToCode(builder: CodeBuilder, node: CrescentAST.Node, methods: MutableSet<Method>) {
        when (node) {
            is CrescentAST.Node.Return -> {
                builder.return_()
            }
            is CrescentAST.Node.Statement.Block -> {
                node.nodes.forEach {
                    nodeToCode(builder, it, methods)
                }
            }
            is CrescentAST.Node.Operator -> {
                when (node.operator) {
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
                    CrescentToken.Operator.POW -> {
                        methods.add(Basic.math.methods.first { it.name.endsWith("pow") })
                        builder.invoke(Basic.math.methods.first { it.name.endsWith("pow") })
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
                    CrescentToken.Operator.NOT -> {
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
                    else -> error("Unknown operator: ${node.operator}")
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
                builder.push(node.data)
            }
            is CrescentAST.Node.Expression -> {
                node.nodes.forEach {
                    nodeToCode(builder, it, methods)
                }
            }
            is CrescentAST.Node.GetCall -> {
                node.arguments.forEach {
                    nodeToCode(builder, it, methods)
                }
                builder.getVar(node.identifier)
                builder.getArrayItem()
            }
            is CrescentAST.Node.IdentifierCall -> {
                node.arguments.reversed().forEach { arg ->
                    nodeToCode(builder, arg, methods)
                }

                when (node.identifier) {
                    "println" -> {
                        builder.push("\n")
                        builder.add()
                        builder.sysCall(SpecialCalls.PRINT)
                    }
                    "print" -> {
                        builder.sysCall(SpecialCalls.PRINT)
                    }
                    else -> {
                        builder.invoke("static." + node.identifier, node.arguments.size, false)
                        //todo no item for checking return type!
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
                nodeToCode(builder, node.predicate, methods)
                builder.push(0)
                builder.ifNotEquals(elseLabel)
                nodeToCode(builder, node.block, methods)
                if (afterLabel != null) {
                    builder.jmp(afterLabel)
                }
                builder.placeLabel(elseLabel)
                if (afterLabel != null) {
                    nodeToCode(builder, node.elseBlock!!, methods)
                    builder.placeLabel(afterLabel)
                }
            }
            is CrescentAST.Node.Statement.While -> {
                val after = builder.newLabel()
                val before = builder.newLabel()
                builder.placeLabel(before)
                nodeToCode(builder, node.predicate, methods)
                builder.push(0)
                builder.ifNotEquals(after)
                nodeToCode(builder, node.block, methods)
                builder.jmp(before)
                builder.placeLabel(after)
            }
            is CrescentAST.Node.Identifier -> {
                builder.getArg(node.name)
            }
            else -> error("Unknown Node: ${node::class.java}")
        }
    }
}