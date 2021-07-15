package me.camdenorrb.crescentvm.vm.ir.poder.tech

import me.camdenorrb.crescentvm.vm.ir.Constant
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

sealed class PoderTechConstants : Constant {
    companion object {
        const val TYPE_STRING: Byte = 0.toByte()

        fun read(input: ByteBuffer): PoderTechConstants {
            return when (input.get()) {
                TYPE_STRING -> {
                    StringConstant.read(input)
                }
                else -> throw IllegalStateException("Malformed Constant Pool!")
            }
        }
    }

    override fun write(): ByteArray {
        val bytes = ByteArray(size())
        val buffer = ByteBuffer.wrap(bytes)
        write(buffer)
        return bytes
    }

    override fun write(output: OutputStream) {
        output.write(write())
    }

    data class StringConstant(val payload: String) : PoderTechConstants() {
        companion object {
            private const val MAX_SIZE: Int = 1024

            fun read(input: ByteBuffer): PoderTechConstants {
                val stringBuilder = mutableListOf<Byte>()
                while (input.hasRemaining()) {
                    when (val char = input.get().toInt()) {
                        0 -> { //null char
                            return StringConstant(stringBuilder.toByteArray().decodeToString())
                        }
                        else -> {
                            stringBuilder.add(char.toByte())
                            check(stringBuilder.size <= MAX_SIZE) {
                                "String too big!"
                            }
                        }
                    }
                }
                throw IllegalStateException("End of buffer reached before string ending!")
            }
        }

        override fun size(): Int {
            return payload.length + 2 //byte for indicator and byte for null terminator
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_STRING)
            output.put(payload.encodeToByteArray())
            output.put(0)
        }
    }
}
