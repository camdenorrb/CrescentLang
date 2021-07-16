package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechConstant
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechInstruction
import me.camdenorrb.crescentvm.vm.jvm.CodeContext
import me.camdenorrb.crescentvm.vm.stack.Variable
import me.camdenorrb.crescentvm.vm.stack.floating.LoadInstruction
import me.camdenorrb.crescentvm.vm.stack.on.OnStack
import me.camdenorrb.crescentvm.vm.stack.on.numbers.*
import java.lang.IllegalStateException

data class PoderTechMethodComposer(
    val instance: PoderTechClassComposer,
    private val name: Int,
    private val description: Int,
    private val context: CodeContext = CodeContext(),
    private val tmpInstructions: MutableMap<Int, PoderTechInstruction> = mutableMapOf()
) {

    init {
        context.stack.clear()
        context.variables.clear()
    }

    fun compile() {
        if (context.stack.isNotEmpty()) {
            System.err.println("[WARN] Stack was not empty! ${context.stack.size} remaining!")
        }
        if (context.variables.isNotEmpty()) {
            System.err.println("[WARN] Variables are not empty! ${context.variables.size} remaining!")
        }
        instance.addMethod(
            PoderTechInstruction.MethodMarker(
                name,
                description,
                Array(tmpInstructions.size) { tmpInstructions[it]!! })
        )
    }

    private fun nextIndex(): Int {
        if (tmpInstructions.isEmpty()) {
            return 0
        }
        return tmpInstructions.keys.maxOrNull()!! + 1
    }

    private fun addInstruction(instruction: PoderTechInstruction) {
        tmpInstructions[nextIndex()] = instruction
    }

    fun loadConst(item: Any) {
        val inst = LoadInstruction {
            val const = when (item) {
                is Number -> PoderTechConstant.NumberConstant(item)
                is String -> PoderTechConstant.StringConstant(item)
                else -> TODO("Const: ${item::class.java}")
            }
            val index = instance.addConstant(
                const
            )
            context.stack.push(const.toStackType())
            addInstruction(PoderTechInstruction.ConstInstruction(PoderTechInstruction.OP_CONST, index))
        }
        context.stack.push(inst)
    }

    fun swap() {
        val a = context.stack.pop()
        val b = context.stack.pop()
        context.stack.push(b)
        context.stack.push(a)
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_SWAP))
    }

    fun deleteVariable(index: UByte) {
        context.getVar(index).delete(context)
        addInstruction(PoderTechInstruction.IndexInstruction(PoderTechInstruction.OP_DEL_VARIABLE, index))
    }

    fun store(uses: Byte = -1): UByte {
        doOp()
        var variable = context.stack.pop()
        if (variable !is OnStack) {
            moveToStack(variable)
            variable = context.stack.pop()
        }
        val length: UByte = when (variable) {
            is StackDouble -> {
                2u
            }
            else -> TODO("Stack Type: ${variable::class.java}")
        }
        val index = Variable.newVar(context, variable, length, uses).startIndex
        addInstruction(PoderTechInstruction.InfoInstruction(PoderTechInstruction.OP_STORE, index, uses.toUByte()))
        return index
    }

    fun load(index: UByte) {
        val variable = context.getVar(index)

        addInstruction(PoderTechInstruction.IndexInstruction(PoderTechInstruction.OP_LOAD, index))
        if (variable.uses > 1) {
            variable.uses = (variable.uses - 1).toByte()
            if (variable.uses == 0.toByte()) {
                deleteVariable(index)
            }
        }
    }

    fun add() {
        doUnOrderedOp()
        val a = context.stack.pop()
        val b = context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_ADD))
        context.stack.push(toStackNumType(a as StackNumber, b as StackNumber))
    }

    fun mul() {
        doUnOrderedOp()
        val a = context.stack.pop()
        val b = context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_MUL))
        context.stack.push(toStackNumType(a as StackNumber, b as StackNumber))
    }

    fun sub() {
        doOrderedOp()
        val a = context.stack.pop()
        val b = context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_SUB))
        context.stack.push(toStackNumType(a as StackNumber, b as StackNumber))
    }

    fun div() {
        doOrderedOp()
        val a = context.stack.pop()
        val b = context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_DIV))
        context.stack.push(toStackNumType(a as StackNumber, b as StackNumber))
    }

    fun neg() {
        doOp()
        val a = context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_NEG))
        context.stack.push(a)
    }

    fun rem() {
        doOrderedOp()
        val a = context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_REM))
        context.stack.push(a)
    }

    fun dec() {
        doOp()
        val a = context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_DEC))
        context.stack.push(a)
    }

    fun toStackType(arg: OnStack): OnStack {
        return when(arg) {
            is StackInt -> StackInt
            else -> TODO("Stack Type: ${arg::class.java}")
        }
    }

    fun toStackNumType(arg1: StackNumber, arg2: StackNumber): StackNumber {
        return when {
            arg1 is StackDouble || arg2 is StackDouble -> StackDouble
            arg1 is StackFloat || arg2 is StackFloat -> StackFloat
            arg1 is StackLong || arg2 is StackLong -> StackLong
            arg1 is StackInt || arg2 is StackInt -> StackInt
            arg1 is StackShort || arg2 is StackShort -> StackShort
            arg1 is StackByte || arg2 is StackByte -> StackByte
            else -> throw IllegalStateException("States: $arg1, $arg2")
        }
    }

    fun inc() {
        doOp()
        val a = context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_INC))
        context.stack.push(a)
    }

    fun numConvert(type: Number) {
        doOp()
        val a = context.stack.pop()
        addInstruction(PoderTechInstruction.ConstInstruction(PoderTechInstruction.OP_NUM_CONV, instance.addConstant(PoderTechConstant.NumberConstant(type))))
        context.stack.push(a)
    }

    fun return_() {
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_RETURN))
    }

    fun returnLast() {
        context.stack.pop()
        addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_RETURN_LAST))
    }

    private fun doOp(): Boolean {
        val a = context.stack.peek()
        if (a !is OnStack) {
            context.stack.pop()
            moveToStack(a)
            return true
        }
        return false
    }

    private fun doOrderedOp() {
        val a = context.stack.pop()
        val b = context.stack.pop()
        var swap = false
        if (b is OnStack && a !is OnStack) {
            swap = true
        }
        if (a !is OnStack) {
            moveToStack(a)
        } else {
            context.stack.push(a)
        }
        if (b !is OnStack) {
            moveToStack(b)
        } else {
            context.stack.push(b)
        }
        if (swap) {
            swap()
        }
    }

    private fun doUnOrderedOp() {
        val a = context.stack.pop()
        val b = context.stack.pop()
        if (a !is OnStack) {
            moveToStack(a)
        } else {
            context.stack.push(a)
        }
        if (b !is OnStack) {
            moveToStack(b)
        } else {
            context.stack.push(b)
        }
    }

    fun moveToStack(a: Any) {
        when(a) {
            is LoadInstruction -> {
                a.moveToStack.invoke()
            }
            else -> TODO("Stack Type: ${a::class.java}")
        }
    }

    fun pop() {
        val arg = context.stack.pop()
        if (arg is OnStack) {
            addInstruction(PoderTechInstruction.SimpleInstruction(PoderTechInstruction.OP_POP))
        }
    }
}
