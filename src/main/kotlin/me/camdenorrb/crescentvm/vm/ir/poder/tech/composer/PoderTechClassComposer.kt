package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

data class PoderTechClassComposer(
    val instance: PoderTechBinaryComposer
) {

    fun compile() {
        instance.addClass(this)
    }

    fun addField(fieldComposer: PoderTechFieldComposer) {

    }

    fun addMethod(methodComposer: PoderTechMethodComposer) {

    }
}
