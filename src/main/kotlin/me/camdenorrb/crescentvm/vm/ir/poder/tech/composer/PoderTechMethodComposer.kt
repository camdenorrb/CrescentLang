package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechConstant
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechInstruction
import java.util.*

data class PoderTechMethodComposer(
    val instance: PoderTechClassComposer,
    private val name: Int,
    private val description: Int,
    private val stack: Stack<Any> = Stack(),
    private val tmpInstructions: MutableMap<Int, PoderTechInstruction> = mutableMapOf()
) {

    init {
        stack.clear()
    }

    fun compile() {
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
        val index = instance.addConstant(
            when (item) {
                is Number -> PoderTechConstant.NumberConstant(item)
                else -> TODO("Const: ${item::class.java}")
            }
        )
        addInstruction(PoderTechInstruction.ConstInstruction(PoderTechInstruction.OP_CONST, index))
    }
}
