package me.camdenorrb.crescentvm.vm.ir.poder.tech

import me.camdenorrb.crescentvm.vm.ir.IRConverter
import me.camdenorrb.crescentvm.vm.ir.Language

class PoderTechGeneric(override val name: String) : Language {
    val instructions = mutableListOf<PoderTechInstruction>()

    override fun appendFromFile(file: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun appendFromOtherLang(language: Language) {
        TODO("Not yet implemented")
    }


    override fun toCode(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun registerConverter(converter: IRConverter, from: Language, to: Language) {
        TODO("Not yet implemented")
    }

    override fun convertTo(other: Language): Language {
        TODO("Not yet implemented")
    }

}