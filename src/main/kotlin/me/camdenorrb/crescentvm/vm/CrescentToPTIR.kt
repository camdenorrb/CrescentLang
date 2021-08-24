package me.camdenorrb.crescentvm.vm

import tech.poder.ir.instructions.common.Method
import tech.poder.ir.instructions.common.special.SpecialCalls
import tech.poder.ir.instructions.simple.CodeBuilder

object CrescentToPTIR {

    fun invoke(astFile: CrescentAST.Node.File): List<Method> {
        val methods = mutableListOf<Method>()
        astFile.functions.forEach { (_, u) ->
            methods.add(Method.create(u.name, u.params.size.toUByte(), u.returnType != CrescentAST.Node.Type.Unit) {
                u.innerCode.nodes.forEach { node ->
                    nodeToCode(it, node)
                }
            })
        }
        return methods
    }

    private fun nodeToCode(builder: CodeBuilder, node: CrescentAST.Node) {
        when (node) {
            is CrescentAST.Node.Return -> {
                builder.return_()
            }
            is CrescentAST.Node.Primitive.String -> {
                builder.push(node.data)
            }
            is CrescentAST.Node.Expression -> {
                node.nodes.forEach {
                    nodeToCode(builder, it)
                }
            }
            is CrescentAST.Node.FunctionCall -> {
                node.arguments.reversed().forEach { arg ->
                    nodeToCode(builder, arg)
                }
                when (node.identifier) {
                    "println" -> {
                        builder.sysCall(SpecialCalls.PRINTLN)
                    }
                    else -> {
                        builder.invoke(node.identifier, node.arguments.size)
                    }
                }
            }
            else -> error("Unknown Node: $node")
        }
    }
}