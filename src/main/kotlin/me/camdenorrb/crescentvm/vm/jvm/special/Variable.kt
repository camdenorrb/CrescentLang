package me.camdenorrb.crescentvm.vm.jvm.special

import me.camdenorrb.crescentvm.vm.jvm.CodeContext
import me.camdenorrb.crescentvm.vm.jvm.special.numbers.StackInt

data class Variable(val startIndex: Int, val size: Byte, val type: Any, var uses: Int) {
    companion object {
        fun newVar(context: CodeContext, type: Any = StackInt, width: Byte = 1, uses: Int = 1): Variable {
            val result = if (context.variables.isEmpty()) {
                Variable(1, width, type, uses)
            } else {
                val sorted = context.variables.sortedBy { it.startIndex }
                if (sorted.first().startIndex !in 1..width.toInt()) {
                    Variable(1, width, type, uses)
                }
                sorted.forEachIndexed { index, variable ->
                    if (index + 1 >= sorted.size) { //reached end, append to set
                        Variable(variable.nextIndex(), width, type, uses)
                    } else {
                        val next = sorted[index + 1]
                        if (next.startIndex !in variable.nextIndex()..(variable.nextIndex() + width)) {
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

    fun nextIndex(): Int {
        return startIndex + size
    }

    fun delete(context: CodeContext) {
        context.variables.remove(this)
    }
}
