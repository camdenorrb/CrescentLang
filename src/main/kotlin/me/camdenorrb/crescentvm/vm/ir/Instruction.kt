package me.camdenorrb.crescentvm.vm.ir

import java.io.OutputStream
import java.nio.ByteBuffer

interface Instruction {
    val opCode: Byte

    fun size(): Int
    fun write(): ByteArray
    fun write(output: ByteBuffer)
    fun write(output: OutputStream)
}