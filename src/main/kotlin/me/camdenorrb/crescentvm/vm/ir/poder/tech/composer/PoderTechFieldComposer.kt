package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

data class PoderTechFieldComposer(
    val instance: PoderTechClassComposer
) {

    fun compile() {
        instance.addField(this)
    }
}
