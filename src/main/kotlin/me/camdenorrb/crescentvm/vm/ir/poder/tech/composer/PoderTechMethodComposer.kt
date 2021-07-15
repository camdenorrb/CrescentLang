package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechInstruction
import java.util.*

data class PoderTechMethodComposer(
    val instance: PoderTechClassComposer,
    private val name: Int,
    private val description: Int,
    private val stack: Stack<Any> = Stack(),
    private val tmpInstructions: MutableMap<Int, PoderTechInstruction> = mutableMapOf()
) {

    fun compile() {
        instance.addMethod(
            PoderTechInstruction.MethodMarker(
                name,
                description,
                Array(tmpInstructions.size) { tmpInstructions[it]!! })
        )
    }

    internal fun nextIndex(): Int {
        return tmpInstructions.keys.maxOrNull()!! + 1
    }
}
