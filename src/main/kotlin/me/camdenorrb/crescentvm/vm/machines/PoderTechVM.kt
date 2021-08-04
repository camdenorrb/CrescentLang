package me.camdenorrb.crescentvm.vm.machines

import me.camdenorrb.crescentvm.vm.VM
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechIR

class PoderTechVM(val mode: VM = VM.INTERPRETED) {
    fun load(classPaths: PoderTechIR) {

    }

    fun packageLib() {

    }

    fun invoke(clazzName: String) {
        packageLib()

    }
}