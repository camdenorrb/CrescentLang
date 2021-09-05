package me.camdenorrb.crescentvm.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

val originalSystemOut = System.out

val originalSystemIn = System.`in`


inline fun collectSystemOut(block: () -> Unit): String {

	val byteArrayOutputStream = ByteArrayOutputStream()
	val printStream = PrintStream(byteArrayOutputStream)

	System.setOut(printStream)
	block()
	System.setOut(originalSystemOut)

	return byteArrayOutputStream.toString()
}

inline fun fakeUserInput(input: String, block: () -> Unit) {
	System.setIn(ByteArrayInputStream(input.toByteArray()))
	block()
	System.setIn(originalSystemIn)
}