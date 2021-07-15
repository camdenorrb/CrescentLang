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

    data class NumberConstant(val payload: Number) : PoderTechConstant() {
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

        fun getType(): Byte {
            return when(payload) {
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
                else -> throw java.lang.IllegalStateException("Unknown Number: ${payload::class.java}")
            }
        }

        override fun write(output: ByteBuffer) {
            val type = getType()
            output.put(type)
            when(payload) {
                is Int -> {
                    PoderTechIR.writeVarInt(payload, output)
                }
                is Short -> {
                    PoderTechIR.writeVarInt(payload.toInt(), output)
                }
                is Double -> {
                    PoderTechIR.writeVarLong(payload.toBits(), output)
                }
                is Long -> {
                    PoderTechIR.writeVarLong(payload, output)
                }
                is Float -> {
                    PoderTechIR.writeVarInt(payload.toBits(), output)
                }
                is Byte -> {
                    output.put(payload)
                }
                else -> throw java.lang.IllegalStateException("Unknown Number: ${payload::class.java}")
            }
        }

        override fun size(): Int {
            return 1 + when(payload) {
                is Int -> {
                    PoderTechIR.varIntSize(payload)
                }
                is Short -> {
                    PoderTechIR.varIntSize(payload.toInt())
                }
                is Double -> {
                    PoderTechIR.varLongSize(payload.toBits())
                }
                is Long -> {
                    PoderTechIR.varLongSize(payload)
                }
                is Float -> {
                    PoderTechIR.varIntSize(payload.toBits())
                }
                is Byte -> {
                    1
                }
                else -> throw java.lang.IllegalStateException("Unknown Number: ${payload::class.java}")
            }

        }

        override fun matches(other: Constant): Boolean {
            if (other !is NumberConstant) return false
            if (other.getType() != getType()) return false
            return other.payload == payload
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
