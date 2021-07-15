package me.camdenorrb.crescentvm.vm.ir.poder.tech

import me.camdenorrb.crescentvm.vm.ir.IRConverter
import me.camdenorrb.crescentvm.vm.ir.Language
import java.nio.ByteBuffer

class PoderTechIR : Language {
    companion object {
        fun getVarIntSize(int: Int): Int {
            var i = int
            var result = 0
            do {
                result++
                i = i ushr 7
            } while (i != 0)
            return result
        }

        fun writeVarInt(int: Int, buffer: ByteBuffer) {
            var v = int
            while (true) {
                val bits = v and 0x7f
                v = v ushr 7
                if (v == 0) {
                    buffer.put(bits.toByte())
                    return
                }
                buffer.put((bits or 0x80).toByte())
            }
        }

        fun readVarInt(input: ByteBuffer): Int {
            var result = 0
            var shiftSize = 0
            var remainder: Int
            do {
                if (shiftSize >= 32) {
                    throw IndexOutOfBoundsException("varint too long")
                }
                remainder = input.get().toInt()
                result = result or (remainder and 0x7F shl shiftSize)
                shiftSize += 7
            } while (remainder and 0x80 != 0)
            return result
        }
    }

    val instructions = mutableListOf<PoderTechInstruction>()
    override val name: String = "PoderTechIR"

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