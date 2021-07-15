package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechInstruction

data class PoderTechFieldComposer(
    val instance: PoderTechClassComposer,
    private val name: Int,
    private val description: Int,
    private val tmpInstructions: MutableList<PoderTechInstruction> = mutableListOf()
) {

    fun compile() {
        instance.addField(PoderTechInstruction.FieldMarker(name, description))
    }
}
