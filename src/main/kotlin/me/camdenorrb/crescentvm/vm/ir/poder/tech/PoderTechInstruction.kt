package me.camdenorrb.crescentvm.vm.ir.poder.tech

import me.camdenorrb.crescentvm.vm.ir.Instruction
import java.io.OutputStream
import java.nio.ByteBuffer

sealed class PoderTechInstruction : Instruction {
    companion object {

        /**
         * These codes are for info based loading, should be read before interpreting or compiling(compilers should move these to the front of the instruction set in order of these codes!)
         */
        const val TYPE_MARKER_CONSTANT_POOL: Byte = 1.toByte()
        const val TYPE_MARKER_CLASS: Byte = 2.toByte()
        const val TYPE_MARKER_FIELD: Byte = 3.toByte()
        const val TYPE_MARKER_METHOD: Byte = 4.toByte()

        const val TYPE_SIMPLE: Byte = 5.toByte()
        const val TYPE_CONSTANT: Byte = 6.toByte()
        const val TYPE_INDEX: Byte = 7.toByte()
        const val TYPE_INFO: Byte = 8.toByte()


        /**
         * Make 0 reserved for version so we can check this fast and not use if breacking change has happened(instruction reordering, type code changes, ect)
         */
        const val VERSION_DATA: Byte = 0.toByte()

        fun read(input: ByteBuffer): PoderTechInstruction {
            return when (input.get()) {
                TYPE_SIMPLE -> {
                    SimpleInstruction.read(input)
                }
                TYPE_CONSTANT -> {
                    ConstInstruction.read(input)
                }
                TYPE_INDEX -> {
                    IndexInstruction.read(input)
                }
                TYPE_INFO -> {
                    InfoInstruction.read(input)
                }
                TYPE_MARKER_METHOD -> {
                    MethodMarker.read(input)
                }
                TYPE_MARKER_FIELD -> {
                    FieldMarker.read(input)
                }
                TYPE_MARKER_CLASS -> {
                    ClassMarker.read(input)
                }
                TYPE_MARKER_CONSTANT_POOL -> {
                    ConstPoolMarker.read(input)
                }
                VERSION_DATA -> {
                    VersionData.read(input)
                }
                else -> throw IllegalStateException("Malformed Instruction Set!")
            }
        }

        /**
         * These codes are for number only operations
         */
        const val OP_ADD: UByte = 0u //SimpleInstruction
        const val OP_SUB: UByte = 1u //SimpleInstruction
        const val OP_MUL: UByte = 2u //SimpleInstruction
        const val OP_DIV: UByte = 3u //SimpleInstruction
        const val OP_REM: UByte = 4u //SimpleInstruction
        const val OP_NEG: UByte = 5u //SimpleInstruction
        const val OP_INC: UByte = 6u //IndexInstruction: inc(index)
        const val OP_DEC: UByte = 7u //IndexInstruction: dec(index)
        const val OP_NUM_CONV: UByte = 8u //ConstInstruction convertTo(constIndexType)

        /**
         * These codes are for save/load operations
         */
        const val OP_STORE: UByte = 9u //InfoInstruction: store(index, uses)
        const val OP_LOAD: UByte = 10u //IndexInstruction: load(index)
        const val OP_CONST: UByte = 11u //ConstInstruction: load(constIndexType)
        const val OP_A_LOAD: UByte = 12u //IndexInstruction: Array load(index)
        const val OP_A_STORE: UByte = 13u //IndexInstruction: Array store(index)
        const val OP_POP: UByte = 14u //SimpleInstruction: remove last stack item(deallocate if possible)
        const val OP_DEL_VARIABLE: UByte = 15u //IndexInstruction: remove(index) (deallocate if possible)
        const val OP_SWAP: UByte =
            16u //SimpleInstruction: pops 2 items from stack, swaps them, them pushes them back onto the stack

        /**
         * These codes are for jump operations
         * On false for if, goto index
         */
        const val OP_NULL: UByte = 17u //SimpleInstruction: loads a null pointer onto the stack
        const val OP_GOTO: UByte = 18u //IndexInstruction: goto(index)
        const val OP_IF_EQ: UByte = 19u //IndexInstruction
        const val OP_IF_NE: UByte = 20u //IndexInstruction
        const val OP_IF_GE: UByte = 21u //IndexInstruction
        const val OP_IF_LE: UByte = 22u //IndexInstruction
        const val OP_IF_GT: UByte = 23u //IndexInstruction
        const val OP_IF_LT: UByte = 24u //IndexInstruction
        const val OP_IF_NULL: UByte = 25u //IndexInstruction
        const val OP_IF_NOT_NULL: UByte = 26u //IndexInstruction
        const val OP_TABLE_SWITCH: UByte =
            27u //InfoInstruction: table switch(default_Offset, defaultCase, lowCase, highCase, case1_Offset, case2_Offset, ...)

        /**
         * These codes are for check operations
         */
        const val OP_INSTANCE_OF: UByte = 28u //ConstInstruction: instanceOf(constIndexType)
        const val OP_POINTER_EQ: UByte = 29u //SimpleInstruction: ===
        const val OP_CMP: UByte = 30u //SimpleInstruction: if eq push 0, if gt push 1, if lt push -1

        /**
         * These codes are for class based operations
         */
        const val OP_INVOKE_METHOD: UByte = 31u //ConstInstruction: invoke(constIndexDescriptor)
        const val OP_INVOKE_STATIC: UByte = 32u //ConstInstruction: invoke(constIndexDescriptor)
        const val OP_GET_STATIC: UByte = 33u //ConstInstruction: get(constIndexDescriptor)
        const val OP_GET_FIELD: UByte = 34u //ConstInstruction: get(constIndexDescriptor)
        const val OP_PUT_STATIC: UByte = 35u //ConstInstruction: set(constIndexDescriptor)
        const val OP_PUT_FIELD: UByte = 36u //ConstInstruction: set(constIndexDescriptor)
        const val OP_NEW: UByte = 37u //ConstInstruction: new(constIndexDescriptor)
        const val OP_NEW_ARRAY: UByte = 38u //InfoInstruction: new Array(size, constIndexDescriptor)

        /**
         * These codes are bitwise operators
         */

        const val OP_BITWISE_OR: UByte = 39u //SimpleInstruction: or
        const val OP_BITWISE_AND: UByte = 40u //SimpleInstruction: and
        const val OP_BITWISE_SHIFT_LEFT: UByte = 41u //SimpleInstruction: shl
        const val OP_BITWISE_SHIFT_RIGHT: UByte = 42u //SimpleInstruction: shr
        const val OP_BITWISE_LOGICAL_SHIFT_LEFT: UByte = 43u //SimpleInstruction: ushl
        const val OP_BITWISE_LOGICAL_SHIFT_RIGHT: UByte = 44u //SimpleInstruction: ushr
        const val OP_BITWISE_XOR: UByte = 45u //SimpleInstruction: xor

        /**
         * These codes are misc
         */
        const val OP_RETURN: UByte = 46u //SimpleInstruction
        const val OP_RETURN_LAST: UByte = 47u //SimpleInstruction
        const val OP_THROW: UByte =
            48u //SimpleInstruction: if hit, dump stacktrace using Exception.getTraceString() from last stack var and exit
        const val OP_MultiInstruction: UByte = 49u //InfoInstruction: List of Instruction(part1, part2, ...) for op extensions
        const val OP_DUP: UByte = 50u //SimpleInstruction
        const val OP_BREAKPOINT: UByte =
            51u //SimpleInstruction: if hit, noop function breakpoint should be called in vm, otherwise dump stacktrace
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

    data class SimpleInstruction(override val opCode: UByte) : PoderTechInstruction() {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                return SimpleInstruction(input.get().toUByte())
            }
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_SIMPLE).put(opCode.toByte())
        }

        override fun size(): Int {
            return 2 //Type and opCode
        }
    }

    data class ConstInstruction(override val opCode: UByte, val constIndex: Int) : PoderTechInstruction() {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                return ConstInstruction(input.get().toUByte(), PoderTechIR.readVarInt(input))
            }
        }

        override fun size(): Int {
            return 2 + PoderTechIR.varIntSize(constIndex) ////Type, opCode, index
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_CONSTANT).put(opCode.toByte())
            PoderTechIR.writeVarInt(constIndex, output)
        }
    }

    data class IndexInstruction(override val opCode: UByte, val index: UByte) : PoderTechInstruction() {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                return IndexInstruction(input.get().toUByte(), input.get().toUByte())
            }
        }

        override fun size(): Int {
            return 3 //Type, opCode, index
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_INDEX).put(opCode.toByte()).put(index.toByte())
        }
    }

    class InfoInstruction(override val opCode: UByte, vararg val info: UByte) : PoderTechInstruction() {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                val code = input.get().toUByte()
                val info = UByteArray(PoderTechIR.readVarInt(input)) {
                    input.get().toUByte()
                }
                return InfoInstruction(code, *info)
            }
        }

        override fun size(): Int {
            return 2 + PoderTechIR.varIntSize(info.size) + info.size //Type, opCode, sizeOf(info), info
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_INFO).put(opCode.toByte())
            PoderTechIR.writeVarInt(info.size, output)
            info.forEach {
                output.put(it.toByte())
            }
        }
    }

    interface ClassItems

    data class ClassMarker(val name: Int, val descriptor: Int, val instructions: Array<ClassItems>, override val opCode: UByte = 0u) :
        PoderTechInstruction() {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                return ClassMarker(PoderTechIR.readVarInt(input), PoderTechIR.readVarInt(input), Array(PoderTechIR.readVarInt(input)) {
                    PoderTechInstruction.read(input) as ClassItems
                })
            }
        }

        override fun size(): Int {
            return 1 + PoderTechIR.varIntSize(name) + PoderTechIR.varIntSize(descriptor) + PoderTechIR.varIntSize(instructions.size) + instructions.sumOf { (it as PoderTechInstruction).size() }
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_MARKER_CLASS)
            PoderTechIR.writeVarInt(name, output)
            PoderTechIR.writeVarInt(descriptor, output)
            PoderTechIR.writeVarInt(instructions.size, output)
            instructions.forEach {
                (it as PoderTechInstruction).write(output)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ClassMarker) return false

            return name == other.name
        }

        override fun hashCode(): Int {
            return name
        }
    }

    data class MethodMarker(
        val name: Int,
        val descriptor: Int,
        val instructions: Array<PoderTechInstruction>,
        override val opCode: UByte = 0u
    ) :
        PoderTechInstruction(), ClassItems {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                return MethodMarker(
                    PoderTechIR.readVarInt(input),
                    PoderTechIR.readVarInt(input),
                    Array(PoderTechIR.readVarInt(input)) {
                        PoderTechInstruction.read(input)
                    })
            }
        }

        override fun size(): Int {
            return 1 + PoderTechIR.varIntSize(name) + PoderTechIR.varIntSize(descriptor) + PoderTechIR.varIntSize(
                instructions.size
            ) + instructions.sumOf { it.size() }
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_MARKER_METHOD)
            PoderTechIR.writeVarInt(name, output)
            PoderTechIR.writeVarInt(descriptor, output)
            PoderTechIR.writeVarInt(instructions.size, output)
            instructions.forEach {
                it.write(output)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MethodMarker) return false

            if (name != other.name) return false
            return descriptor == other.descriptor
        }

        override fun hashCode(): Int {
            var result = name
            result = 31 * result + descriptor
            return result
        }
    }

    data class FieldMarker(val name: Int, val descriptor: Int, override val opCode: UByte = 0u) : PoderTechInstruction(),
        ClassItems {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                return FieldMarker(PoderTechIR.readVarInt(input), PoderTechIR.readVarInt(input))
            }
        }

        override fun size(): Int {
            return 1 + PoderTechIR.varIntSize(name) + PoderTechIR.varIntSize(descriptor)
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_MARKER_FIELD)
            PoderTechIR.writeVarInt(name, output)
            PoderTechIR.writeVarInt(descriptor, output)
        }
    }

    data class ConstPoolMarker(val data: Array<PoderTechConstant>, override val opCode: UByte = 0u) :
        PoderTechInstruction() {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                return ConstPoolMarker(Array(PoderTechIR.readVarInt(input)) {
                    PoderTechConstant.read(input)
                })
            }
        }

        override fun size(): Int {
            return 1 + PoderTechIR.varIntSize(data.size) + data.sumOf { it.size() }
        }

        override fun write(output: ByteBuffer) {
            output.put(TYPE_MARKER_CONSTANT_POOL)
            PoderTechIR.writeVarInt(data.size, output)
            data.forEach {
                it.write(output)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ConstPoolMarker) return false

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result *= 31
            return result
        }
    }

    data class VersionData(val version: Int, override val opCode: UByte = 0u) : PoderTechInstruction() {
        companion object {
            fun read(input: ByteBuffer): PoderTechInstruction {
                return VersionData(PoderTechIR.readVarInt(input))
            }
        }

        override fun size(): Int {
            return 1 + PoderTechIR.varIntSize(version)
        }

        override fun write(output: ByteBuffer) {
            output.put(VERSION_DATA)
            PoderTechIR.writeVarInt(version, output)
        }
    }
}
