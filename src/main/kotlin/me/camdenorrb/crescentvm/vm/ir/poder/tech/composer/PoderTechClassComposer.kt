package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechConstant
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechInstruction

data class PoderTechClassComposer(
    private val instance: PoderTechBinaryComposer,
    val name: String,
    val superName: String,
    private val tmpInstructions: MutableList<PoderTechInstruction.ClassItems> = mutableListOf()
) {

    fun compile() {
        tmpInstructions.sortBy {
            if (it is PoderTechInstruction.FieldMarker) { //Fields First!
                0
            } else {
                1
            }
        }
        instance.addClass(
            PoderTechInstruction.ClassMarker(
                instance.addConstant(PoderTechConstant.StringConstant(name)),
                tmpInstructions.toTypedArray()
            )
        )
    }

    fun addField(name: String, description: String) {
        PoderTechFieldComposer(
            this,
            instance.addConstant(PoderTechConstant.StringConstant(name)),
            instance.addConstant(PoderTechConstant.StringConstant(description))
        ).compile()
    }

    internal fun addConstant(constant: PoderTechConstant): Int {
        return instance.addConstant(constant)
    }

    fun addMethod(name: String, description: String): PoderTechMethodComposer {
        return PoderTechMethodComposer(
            this,
            instance.addConstant(PoderTechConstant.StringConstant(name)),
            instance.addConstant(PoderTechConstant.StringConstant(description))
        ) //todo add code composer
    }

    internal fun addField(fieldInstructions: PoderTechInstruction.FieldMarker) {
        tmpInstructions.add(fieldInstructions)
    }

    internal fun addMethod(methodInstructions: PoderTechInstruction.MethodMarker) {
        tmpInstructions.add(methodInstructions)
    }
}
