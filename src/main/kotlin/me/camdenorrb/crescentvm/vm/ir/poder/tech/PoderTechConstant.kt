package me.camdenorrb.crescentvm.vm.ir.poder.tech

import me.camdenorrb.crescentvm.vm.ir.Constant
import java.io.OutputStream
import java.nio.ByteBuffer

sealed class PoderTechConstant : Constant {
    companion object {
        const val TYPE_STRING: Byte = 0.toByte()
        const val TYPE_NUMBER: Byte = 1.toByte()

        fun read(input: ByteBuffer): PoderTechConstant {
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

    data class NumberConstant(val num: Number) : PoderTechConstant() {
        companion object {
            private const val BYTE: Byte = 0
            private const val SHORT: Byte = 1
            private const val INT: Byte = 2
            private const val LONG: Byte = 3
            private const val DOUBLE: Byte = 4
            private const val FLOAT: Byte = 5

            fun read(input: ByteBuffer): PoderTechConstant {
                return when (val type = input.get()) {
                    INT -> {
                        NumberConstant(PoderTechIR.readVarInt(input))
                    }
                    SHORT -> {
                        NumberConstant(PoderTechIR.readVarInt(input).toShort())
                    }
                    DOUBLE -> {
                        NumberConstant(Double.fromBits(PoderTechIR.readVarLong(input)))
                    }
                    LONG -> {
                        NumberConstant(PoderTechIR.readVarLong(input))
                    }
                    FLOAT -> {
                        NumberConstant(Float.fromBits(PoderTechIR.readVarInt(input)))
                    }
                    BYTE -> {
                        NumberConstant(input.get())
                    }
                    else -> throw java.lang.IllegalStateException("Unknown Number Type: $type")
                }
            }
        }

        fun getType(num: Number): Byte {
            return when(num) {
                is Int -> {
                    INT
                }
                is Short -> {
                    SHORT
                }
                is Double -> {
                    DOUBLE
                }
                is Long -> {
                    LONG
                }
                is Float -> {
                    FLOAT
                }
                is Byte -> {
                    BYTE
                }
                else -> throw java.lang.IllegalStateException("Unknown Number: ${num::class.java}")
            }
        }

        override fun write(output: ByteBuffer) {
            val type = getType(num)
            output.put(type)
            when(num) {
                is Int -> {
                    PoderTechIR.writeVarInt(num, output)
                }
                is Short -> {
                    PoderTechIR.writeVarInt(num.toInt(), output)
                }
                is Double -> {
                    PoderTechIR.writeVarLong(num.toBits(), output)
                }
                is Long -> {
                    PoderTechIR.writeVarLong(num, output)
                }
                is Float -> {
                    PoderTechIR.writeVarInt(num.toBits(), output)
                }
                is Byte -> {
                    output.put(num)
                }
                else -> throw java.lang.IllegalStateException("Unknown Number: ${num::class.java}")
            }
        }

        override fun size(): Int {
            return 1 + when(num) {
                is Int -> {
                    PoderTechIR.varIntSize(num)
                }
                is Short -> {
                    PoderTechIR.varIntSize(num.toInt())
                }
                is Double -> {
                    PoderTechIR.varLongSize(num.toBits())
                }
                is Long -> {
                    PoderTechIR.varLongSize(num)
                }
                is Float -> {
                    PoderTechIR.varIntSize(num.toBits())
                }
                is Byte -> {
                    1
                }
                else -> throw java.lang.IllegalStateException("Unknown Number: ${num::class.java}")
            }

        }

        override fun matches(other: Constant): Boolean {
            TODO("Not yet implemented")
        }
    }

    data class StringConstant(val payload: String) : PoderTechConstant() {
        companion object {
            private const val MAX_SIZE: Int = 1024

            fun read(input: ByteBuffer): PoderTechConstant {
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

        override fun matches(other: Constant): Boolean {
            if (other !is StringConstant) return false
            return other.payload == payload
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_STRING)
            output.put(payload.encodeToByteArray())
            output.put(0)
        }
    }
}
