package me.camdenorrb.crescentvm.vm.ir

import java.io.OutputStream
import java.nio.ByteBuffer

interface Constant {
    fun size(): Int
    fun matches(other: Constant): Boolean
    fun write(): ByteArray
    fun write(output: ByteBuffer)
    fun write(output: OutputStream)
}