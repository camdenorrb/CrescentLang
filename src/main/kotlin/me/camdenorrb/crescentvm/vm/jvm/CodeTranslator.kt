package me.camdenorrb.crescentvm.vm.jvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentToken
import me.camdenorrb.crescentvm.vm.stack.Variable
import me.camdenorrb.crescentvm.vm.stack.on.OnStack
import me.camdenorrb.crescentvm.vm.stack.on.StackClazz
import me.camdenorrb.crescentvm.vm.stack.on.numbers.StackDouble
import me.camdenorrb.crescentvm.vm.stack.on.numbers.StackFloat
import me.camdenorrb.crescentvm.vm.stack.on.numbers.StackInt
import me.camdenorrb.crescentvm.vm.stack.on.numbers.StackLong
import proguard.classfile.editor.CompactCodeAttributeComposer
import kotlin.math.pow

data class CodeTranslator(val context: CodeContext, val codeBuilder: CompactCodeAttributeComposer, val imported: Map<String, String>) {
    fun codeGenerate(codes: List<CrescentAST.Node>) {
        codeLaunch(*codes.toTypedArray())
        if (context.variables.isNotEmpty()) {
            System.err.println("[WARN] ${context.variables.size} were not deallocated from variables!")
        }
        if (context.stack.isNotEmpty()) {
            System.err.println("[WARN] ${context.stack.size} were not deallocated from stack!")
        }
        context.stack.clear()
        context.variables.clear()
    }

    private fun codeLaunch(vararg codes: CrescentAST.Node) {
        codes.forEach { node ->
            when (node) {
                is CrescentAST.Node.FunctionCall -> functionCall(node)
                is CrescentAST.Node.Operation -> operation(node)
                is CrescentAST.Node.Argument -> argument(node)
                is CrescentAST.Node.Number -> number(node)
                is CrescentAST.Node.String -> string(node)
                is CrescentAST.Node.Expression -> codeLaunch(*node.nodes.toTypedArray())
                is CrescentAST.Node.Return -> return_(node)
                else -> TODO("Node: $node")
            }
        }
    }

    private fun return_(return_: CrescentAST.Node.Return) {
        codeLaunch(*return_.expression.nodes.toTypedArray())
        if (return_.expression.nodes.isEmpty()) {
            codeBuilder.return_()
        } else {
            when (val result = context.stack.pop()) {
                is String -> {
                    codeBuilder.areturn()
                }
                else -> TODO("Return aType \"${result::class.java}\"")
            }
        }
    }

    private fun string(string: CrescentAST.Node.String) {
        codeBuilder.ldc(string.data)
        context.stack.push(string.data)
    }

    private fun number(number: CrescentAST.Node.Number) {
        //todo push on stack and wait for consumption
        // reason: Allows pre-optimization and dead code removal
        context.stack.push(number.number)
    }

    private fun loadOp(varIndex: UByte) {
        val variable = context.getVar(varIndex)
        when (variable.type) {
            is StackDouble -> {
                codeBuilder.dload(variable.startIndex.toInt())
            }
            else -> TODO("Stack Type: ${variable.type::class.java}")
        }

        if (variable.uses > 1) {
            variable.uses = (variable.uses - 1).toByte()
            if (variable.uses == 0.toByte()) {
                variable.delete(context)
            }
        }
    }

    private fun storeOp(op: Any, uses: Byte): UByte {
        if (op !is OnStack) {
            addToStack(op)
        }

        return when (val newResult = context.stack.pop()) {
            is StackDouble -> {
                val variable = Variable.newVar(context, newResult, 2u, uses)
                codeBuilder.dstore(variable.startIndex.toInt())
                variable.startIndex
            }
            else -> TODO("Stack Type: ${newResult::class.java}")
        }
    }

    private fun processOrderedOps(op1: Any, op2: Any): Boolean {
        return if (op1 !is OnStack && op2 !is OnStack) {
            true
        } else {
            var swap = false
            if (op2 is OnStack && op1 !is OnStack) {
               swap = true
            }
            if (op1 !is OnStack) {
                addToStack(op1)
            }
            if (op2 !is OnStack) {
                addToStack(op2)
            }
            if (swap) {
                codeBuilder.swap()
            }
            false
        }
    }

    private fun processUnOrderedOps(op1: Any, op2: Any): Boolean {
        return if (op1 !is OnStack && op2 !is OnStack) {
            true
        } else {
            if (op1 !is OnStack) {
                addToStack(op1)
            }
            if (op2 !is OnStack) {
                addToStack(op2)
            }
            false
        }
    }

    private fun operation(operation: CrescentAST.Node.Operation) {
        codeLaunch(operation.first, operation.second)
        when (operation.operator) {
            CrescentToken.Operator.NOT -> TODO()
            CrescentToken.Operator.ADD -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                val preOpt = processUnOrderedOps(test1, test2)
                when (test1) {
                    is Double -> {
                        if (preOpt) {
                            context.stack.push(test1.toDouble() + (test2 as Number).toDouble())
                        } else {
                            context.stack.push(StackDouble)
                            codeBuilder.dadd()
                        }
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
            }
            CrescentToken.Operator.SUB -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                val preOpt = processOrderedOps(test1, test2)
                when (test1) {
                    is Double -> {
                        if (preOpt) {
                            context.stack.push(test1.toDouble() - (test2 as Number).toDouble())
                        } else {
                            context.stack.push(StackDouble)
                            codeBuilder.dsub()
                        }
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
            }
            CrescentToken.Operator.MUL -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                val preOpt = processUnOrderedOps(test1, test2)
                when (test1) {
                    is Double -> {
                        if (preOpt) {
                            context.stack.push(test1.toDouble() * (test2 as Number).toDouble())
                        } else {
                            context.stack.push(StackDouble)
                            codeBuilder.dmul()
                        }
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
            }
            CrescentToken.Operator.DIV -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                val preOpt = processOrderedOps(test1, test2)
                when (test1) {
                    is Double -> {
                        if (preOpt) {
                            context.stack.push(test1.toDouble() / (test2 as Number).toDouble())
                        } else {
                            context.stack.push(StackDouble)
                            codeBuilder.ddiv()
                        }
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
            }
            CrescentToken.Operator.POW -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                val preOpt = processOrderedOps(test1, test2)
                when (test1) {
                    is Double -> {
                        //double is allowed directly
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
                if (preOpt) {
                    context.stack.push(test1.toDouble().pow((test2 as Number).toDouble()))
                } else {
                    context.stack.push(StackDouble)
                    codeBuilder.invokestatic("java/lang/Math", "pow", "(DD)D")
                }
            }
            CrescentToken.Operator.REM -> TODO()
            CrescentToken.Operator.ASSIGN -> TODO()
            CrescentToken.Operator.ADD_ASSIGN -> TODO()
            CrescentToken.Operator.SUB_ASSIGN -> TODO()
            CrescentToken.Operator.MUL_ASSIGN -> TODO()
            CrescentToken.Operator.DIV_ASSIGN -> TODO()
            CrescentToken.Operator.REM_ASSIGN -> TODO()
            CrescentToken.Operator.OR_COMPARE -> TODO()
            CrescentToken.Operator.AND_COMPARE -> TODO()
            CrescentToken.Operator.EQUALS_COMPARE -> TODO()
            CrescentToken.Operator.LESSER_EQUALS_COMPARE -> TODO()
            CrescentToken.Operator.GREATER_EQUALS_COMPARE -> TODO()
            CrescentToken.Operator.EQUALS_REFERENCE_COMPARE -> TODO()
            CrescentToken.Operator.NOT_EQUALS_COMPARE -> TODO()
            CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE -> TODO()
            CrescentToken.Operator.CONTAINS -> TODO()
            CrescentToken.Operator.RANGE -> TODO()
            CrescentToken.Operator.TYPE_PREFIX -> TODO()
            CrescentToken.Operator.RESULT -> TODO()
            CrescentToken.Operator.COMMA -> TODO()
            else -> TODO()
        }
    }

    private fun numCheck(test1: Any, test2: Any) {
        check(test1 is Number) {
            "Add on non-number! \"${test1::class.java}\""
        }
        check(test2 is Number) {
            "Add on non-number! \"${test2::class.java}\""
        }
        check(test1::class == test2::class) {
            "\"${test1::class.java}\" != \"${test2::class.java}\""
        }
    }

    private fun argument(argument: CrescentAST.Node.Argument) {
        codeLaunch(*argument.value.nodes.toTypedArray())
    }

    private fun functionCall(functionCall: CrescentAST.Node.FunctionCall) {
        when (functionCall.name) {
            "println" -> {
                codeBuilder.getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")
                context.stack.push(StackClazz("java/io/PrintStream"))
                codeLaunch(*functionCall.arguments.toTypedArray())
                var arg = context.stack.pop()
                if (arg !is OnStack) {
                    addToStack(arg)
                    arg = context.stack.pop()
                }
                val desc = genDescriptor(arg, imported)
                codeBuilder.invokevirtual("java/io/PrintStream", "println", "(${desc})V")
                context.stack.pop()
            }
            else -> TODO(functionCall.name)
        }
    }

    private fun addToStack(op: Any) {
        when (op) {
            is Number -> addNumberToStack(op)
            else -> TODO("Parse StackType: ${op::class.java}")
        }
    }

    private fun addNumberToStack(op: Number) {
        when (op) {
            is Double -> {
                when (val x = op.toDouble()) {
                    0.0 -> codeBuilder.dconst_0()
                    1.0 -> codeBuilder.dconst_1()
                    else -> codeBuilder.ldc2_w(x)
                }
                context.stack.push(StackDouble)
            }
            is Float -> {
                when (val x = op.toFloat()) {
                    0f -> codeBuilder.fconst_0()
                    1f -> codeBuilder.fconst_1()
                    2f -> codeBuilder.fconst_2()
                    else -> codeBuilder.ldc(x)
                }
                context.stack.push(StackFloat)
            }
            is Int -> {
                when (val x = op.toInt()) {
                    0 -> codeBuilder.iconst_0()
                    1 -> codeBuilder.iconst_1()
                    2 -> codeBuilder.iconst_2()
                    3 -> codeBuilder.iconst_3()
                    4 -> codeBuilder.iconst_4()
                    5 -> codeBuilder.iconst_5()
                    else -> codeBuilder.ldc(x)
                }
                context.stack.push(StackInt)
            }
            is Long -> {
                when (val x = op.toLong()) {
                    0L -> codeBuilder.lconst_0()
                    1L -> codeBuilder.lconst_1()
                    else -> codeBuilder.ldc2_w(x)
                }
                context.stack.push(StackLong)
            }
            else -> TODO("Parse NumberType: ${op::class.java}")
        }
    }

    companion object {
        fun genDescriptor(type: Any, imported: Map<String, String>): String {
            return when (type) {
                is Double, is StackDouble -> {
                    return "D"
                }
                is CrescentAST.Node.Type.Unit -> "V"
                is CrescentAST.Node.Type.Basic -> {
                    if (type.name == "String") {
                        "Ljava/lang/String;"
                    } else if (imported.containsKey(type.name)) {
                        "L${imported[type.name]};"
                    } else {
                        TODO("Type \"${type.name}\"")
                    }
                }
                is CrescentAST.Node.String, is String -> {
                    "Ljava/lang/String;"
                }
                is CrescentAST.Node.Type.Array -> {
                    val builder = StringBuilder()
                    builder.append("[")
                    builder.append(genDescriptor(type.type, imported))
                    builder.toString()
                }
                else -> TODO("Type \"${type::class.java}\"")
            }
        }
    }
}
