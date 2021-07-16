package me.camdenorrb.crescentvm.vm.stack

import me.camdenorrb.crescentvm.vm.jvm.CodeContext
import me.camdenorrb.crescentvm.vm.stack.on.numbers.StackInt

data class Variable(val startIndex: UByte, val size: UByte, val type: Any, var uses: Int) {
    companion object {
        fun newVar(context: CodeContext, type: Any = StackInt, width: UByte = 1u, uses: Int = 1): Variable {
            val result = if (context.variables.isEmpty()) {
                Variable(1u, width, type, uses)
            } else {
                val sorted = context.variables.sortedBy { it.startIndex }
                if (sorted.first().startIndex !in 1u.toUByte()..width) {
                    Variable(1u, width, type, uses)
                }
                sorted.forEachIndexed { index, variable ->
                    if (index + 1 >= sorted.size) { //reached end, append to set
                        Variable(variable.nextIndex(), width, type, uses)
                    } else {
                        val next = sorted[index + 1]
                        if (next.startIndex !in variable.nextIndex()..(variable.nextIndex() + width).toUByte()) {
                            Variable(variable.nextIndex(), width, type, uses)
                        }
                    }
                }
                TODO("Stack Failed to init var, how?")
            }
            context.variables.add(result)
            return result
        }
    }

    fun nextIndex(): UByte {
        return (startIndex + size).toUByte()
    }

    fun delete(context: CodeContext) {
        context.variables.remove(this)
    }
}
