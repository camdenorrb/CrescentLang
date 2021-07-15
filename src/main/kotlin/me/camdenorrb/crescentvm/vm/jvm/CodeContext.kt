package me.camdenorrb.crescentvm.vm.jvm

import me.camdenorrb.crescentvm.vm.stack.Variable
import java.util.*

data class CodeContext(val variables: MutableSet<Variable> = mutableSetOf(), val stack: Stack<Any> = Stack()) {
    fun getVar(index: UByte): Variable {
        return variables.first { it.startIndex == index }
    }
}
