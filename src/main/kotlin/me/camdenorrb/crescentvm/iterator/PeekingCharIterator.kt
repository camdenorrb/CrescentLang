package me.camdenorrb.crescentvm.iterator

import java.lang.StringBuilder

class PeekingCharIterator(val input: String): Iterator<Char> {

    private var index = 0


    override fun hasNext(): Boolean {
        return index < input.length
    }

    override fun next(): Char {
        return input[index++]
    }

    fun next(size: Int): String {
        return input.substring(index, index + size).also {
            index += 5
        }
    }

    fun nextUntil(vararg chars: Char): String {

        val minIndex = chars
            .map { input.indexOf(it, index) }
            .filter { it != -1 }
            .min()
            ?: input.length - 1

        index = minIndex - 1

        return input.substring(index, minIndex)
    }

    fun nextUntil(predicate: (Char) -> Boolean): String {

        val outputBuilder = StringBuilder()

        while (index < input.length) {

            val nextChar = input[index]

            if (predicate(nextChar)) {
                break
            }

            outputBuilder.append(nextChar)
            index++
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
        return input[index]
    }

    fun peekNextUntil(predicate: (Char) -> Boolean): String {

        var nextPeekIndex = index
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