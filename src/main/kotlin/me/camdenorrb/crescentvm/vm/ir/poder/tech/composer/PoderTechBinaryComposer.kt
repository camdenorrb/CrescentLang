package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechConstants
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechIR
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechInstruction

data class PoderTechBinaryComposer(
    val instance: PoderTechIR = PoderTechIR(),
    private val pool: MutableMap<Int, PoderTechConstants>
) {
    companion object {
        val CURRENT_VERSION = PoderTechInstruction.VersionData(0)
    }

    init {
        if (instance.instructions.isEmpty()) {
            instance.instructions.add(CURRENT_VERSION)
            instance.instructions.add(PoderTechInstruction.ConstPoolMarker(emptyArray()))
        } else {
            pool.putAll((instance.instructions[1] as PoderTechInstruction.ConstPoolMarker).data.mapIndexed { index, poderTechConstants ->
                Pair(
                    index,
                    poderTechConstants
                )
            })
        }
    }

    fun compile() {
        instance.instructions[1] = PoderTechInstruction.ConstPoolMarker(Array(pool.size) {
            pool[it]!!
        })
    }

    fun addClass(classComposer: PoderTechClassComposer) {

    }
}
