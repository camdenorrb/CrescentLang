package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import java.util.*

data class PoderTechMethodComposer(
    val instance: PoderTechClassComposer,
    private val stack: Stack<Any> = Stack()
) {

    fun compile() {
        instance.addMethod(this)
    }
}
