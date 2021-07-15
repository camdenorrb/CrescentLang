package me.camdenorrb.crescentvm.vm.ir.poder.tech.composer

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechConstant
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechIR
import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechInstruction

data class PoderTechBinaryComposer(
    val instance: PoderTechIR = PoderTechIR(),
    private val pool: MutableMap<Int, PoderTechConstant> = mutableMapOf()
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

    fun addConstant(constant: PoderTechConstant): Int {
        if (pool.isEmpty()) {
            pool[0] = constant
            return 0
        }
        val possible = pool.filter { it.value.matches(constant) }
        return when {
            possible.size > 1 -> {
                throw IllegalStateException("Pool had multiple matches for \"$constant\"!!")
            }
            possible.isEmpty() -> {
                val index = pool.keys.maxOrNull()!! + 1
                pool[index] = constant
                index
            }
            else -> {
                possible.keys.first()
            }
        }
    }

    fun compile() {
        instance.instructions[1] = PoderTechInstruction.ConstPoolMarker(Array(pool.size) {
            pool[it]!!
        })
    }

    fun addClass(name: String, superName: String = "builtin/Any"): PoderTechClassComposer {
        return PoderTechClassComposer(this, name, superName)
    }

    internal fun addClass(classInstructions: PoderTechInstruction.ClassMarker) {
        instance.instructions.add(classInstructions)
    }
}
