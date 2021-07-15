package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechConstant
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechInstruction
import me.camdenorrb.crescentvm.vm.jvm.CodeContext
import me.camdenorrb.crescentvm.vm.stack.floating.LoadInstruction
import java.util.*

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
}
