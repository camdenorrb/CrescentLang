package me.camdenorrb.crescentvm.vm.ir.poder.tech

import me.camdenorrb.crescentvm.vm.ir.Instruction

sealed class PoderTechInstruction {
    companion object {
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
        const val OP_NUM_CONV: UByte = 8u //convertTo(constIndexType)

        /**
         * These codes are for save/load operations
         */
        const val OP_STORE: UByte = 9u //IndexInstruction: store(index)
        const val OP_LOAD: UByte = 10u //IndexInstruction: load(index)
        const val OP_CONST: UByte = 11u //ConstInstruction: load(constIndexType)
        const val OP_A_LOAD: UByte = 12u //IndexInstruction: Array load(index)
        const val OP_A_STORE: UByte = 13u //IndexInstruction: Array store(index)
        const val OP_POP: UByte = 14u //SimpleInstruction: remove last stack item(deallocate if possible)
        const val OP_DEL_VARIABLE: UByte = 15u //IndexInstruction: remove(index) (deallocate if possible)
        const val OP_SWAP: UByte = 16u //SimpleInstruction: pops 2 items from stack, swaps them, them pushes them back onto the stack

        /**
         * These codes are for jump operations
         * On false for if, goto index
         */
        val OP_NULL: UByte = 17u //SimpleInstruction: loads a null pointer onto the stack
        const val OP_GOTO: UByte = 18u //IndexInstruction: goto(index)
        const val OP_IF_EQ: UByte = 19u //IndexInstruction
        const val OP_IF_NE: UByte = 20u //IndexInstruction
        const val OP_IF_GE: UByte = 21u //IndexInstruction
        const val OP_IF_LE: UByte = 22u //IndexInstruction
        const val OP_IF_GT: UByte = 23u //IndexInstruction
        const val OP_IF_LT: UByte = 24u //IndexInstruction
        const val OP_IF_NULL: UByte = 25u //IndexInstruction
        const val OP_IF_NOT_NULL: UByte = 26u //IndexInstruction
        const val OP_TABLE_SWITCH: UByte = 27u //InfoInstruction: table switch(default_Offset, defaultCase, lowCase, highCase, case1_Offset, case2_Offset, ...)

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
        val OP_RETURN: UByte = 46u //SimpleInstruction
        val OP_RETURN_LAST: UByte = 47u //SimpleInstruction
        val OP_THROW: UByte = 48u //SimpleInstruction: if hit, dump stacktrace using Exception.getTraceString() from last stack var and exit
        val OP_MultiInstruction: UByte = 49u //InfoInstruction: List of Instruction(part1, part2, ...) for op extensions
        val OP_BREAKPOINT: UByte = 50u //SimpleInstruction: if hit, noop function breakpoint should be called in vm, otherwise dump stacktrace
    }

    class SimpleInstruction(opCode: Byte) : Instruction(opCode)
    class ConstInstruction(opCode: Byte, val constIndex: Int) : Instruction(opCode)
    class IndexInstruction(opCode: Byte, val index: UByte) : Instruction(opCode)
    class InfoInstruction(opCode: Byte, vararg val info: UByte) : Instruction(opCode)
}
