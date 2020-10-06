package me.camdenorrb.crescentvm.iterator

import java.lang.StringBuilder

class PeekingCharIterator(val input: String): Iterator<Char> {

    private var nextIndex = 0


    override fun hasNext(): Boolean {
        return nextIndex < input.length
    }

    override fun next(): Char {
        return input[nextIndex++]
    }

    fun next(size: Int): String {
        return input.substring(nextIndex, nextIndex + size).also {
            nextIndex += 5
        }
    }

    fun nextUntil(vararg chars: Char): String {

        val minIndex = (chars.map {
            input.indexOf(it, nextIndex)
        }.min()?.takeIf { it != -1 } ?: input.length) - 1

        nextIndex = minIndex - 1

        println(input[nextIndex])
        return input.substring(nextIndex, minIndex)
    }

    fun nextUntil(predicate: (Char) -> Boolean): String {

        val outputBuilder = StringBuilder()

        while (nextIndex < input.length) {

            val nextChar = input[nextIndex]

            if (predicate(nextChar)) {
                break
            }

            outputBuilder.append(nextChar)
            nextIndex++
        }

        return outputBuilder.toString()
    }


    /*
    fun hasPrev(): Boolean {
        return nextIndex > 0
    }

    fun prev(): Char {
        nextIndex--
        return input[nextIndex]
    }
    */

    fun peekNext(): Char {
        return input[nextIndex]
    }

    fun peekNextUntil(predicate: (Char) -> Boolean): String {

        var nextPeekIndex = nextIndex
        val outputBuilder = StringBuilder()

        while (nextPeekIndex < input.length) {

            val nextChar = input[nextPeekIndex++]

            if (!predicate(nextChar)) {
                break
            }

            outputBuilder.append(nextChar)
        }

        return outputBuilder.toString()
    }

}