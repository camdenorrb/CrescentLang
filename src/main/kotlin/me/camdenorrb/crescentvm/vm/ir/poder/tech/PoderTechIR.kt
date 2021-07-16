package me.camdenorrb.crescentvm.vm.ir.poder.tech

import me.camdenorrb.crescentvm.vm.ir.IRConverter
import me.camdenorrb.crescentvm.vm.ir.Language
import java.nio.ByteBuffer

class PoderTechIR : Language {
    companion object {
        fun varLongSize(long: Long): Int {
            var remainder = long
            var result = 0
            do {
                result++
                remainder = remainder ushr 7
            } while (remainder != 0L)
            return result
        }

        fun writeVarLong(long: Long, buffer: ByteBuffer) {
            var remainder = long
            while (true) {
                val bits = remainder.toInt() and 0x7f
                remainder = remainder ushr 7
                if (remainder == 0L) {
                    buffer.put(bits.toByte())
                    return
                }
                buffer.put((bits or 0x80).toByte())
            }
        }

        fun readVarLong(input: ByteBuffer): Long {
            var result = 0L
            var shiftSize = 0
            var remainder: Long
            do {
                if (shiftSize >= 64) {
                    throw IndexOutOfBoundsException("varlong too big")
                }
                remainder = input.get().toLong()
                result = result or (remainder and 0x7F shl shiftSize)
                shiftSize += 7
            } while (remainder and 0x80 != 0L)
            return result
        }

        fun varIntSize(int: Int): Int {
            var remainder = int
            var result = 0
            do {
                result++
                remainder = remainder ushr 7
            } while (remainder != 0)
            return result
        }

        fun writeVarInt(int: Int, buffer: ByteBuffer) {
            var remainder = int
            while (true) {
                val bits = remainder and 0x7f
                remainder = remainder ushr 7
                if (remainder == 0) {
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
                    throw IndexOutOfBoundsException("varint too big")
                }
                remainder = input.get().toInt()
                result = result or (remainder and 0x7F shl shiftSize)
                shiftSize += 7
            } while (remainder and 0x80 != 0)
            return result
        }

        const val NAME: String = "PoderTechIR"
        val MAGIC = byteArrayOf(9, 8, 2, 4, *NAME.encodeToByteArray())
    }

    val instructions = mutableListOf<PoderTechInstruction>()
    override val name: String = NAME

    override fun appendFromFile(file: ByteArray) {
        val builder = ByteBuffer.wrap(file)
        val magicCheck = ByteArray(MAGIC.size)
        builder.get(magicCheck)
        check(magicCheck.contentEquals(MAGIC)) {
            "File was not valid!"
        }
        repeat(readVarInt(builder)) {
            instructions.add(PoderTechInstruction.read(builder))
        }
    }

    override fun registerConverter(converter: IRConverter, from: Language, to: Language) {
        TODO("Not yet implemented")
    }

    override fun appendFromOtherLang(language: Language) {
        TODO("Not yet implemented")
    }

    override fun toCode(): ByteArray {
        val size = instructions.sumOf { it.size() } + MAGIC.size + varIntSize(instructions.size) + 1
        val bytes = ByteArray(size)
        val builder = ByteBuffer.wrap(bytes)
        builder.put(MAGIC)
        writeVarInt(instructions.size, builder)
        instructions.forEach {
            it.write(builder)
        }
        return bytes
    }

    override fun convertTo(other: Language): Language {
        TODO("Not yet implemented")
    }

}