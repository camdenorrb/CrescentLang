package me.camdenorrb.crescentvm.iterator

import java.lang.StringBuilder

class PeekingCharIterator(val input: String): Iterator<Char> {

    private var index = 0


    override fun hasNext(): Boolean {
        return index < input.length - 1
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

        val minIndex = input.indexOfAny(chars, index).takeUnless { it == -1 }
            ?: input.length

        val output = input.substring(index, minIndex)

        index = minIndex

        return output
    }


    fun nextUntil(predicate: (Char) -> Boolean): String {
        return buildString {
            while (index < input.length) {

                val nextChar = input[index]
                println(nextChar)
                if (predicate(nextChar)) {
                    break
                }

                index++
                append(nextChar)
            }
        }
    }

    /*

    fun nextUntilAndSkip(vararg chars: Char): String {

        val read = nextUntil(*chars)
        index = (index + 1).coerceAtMost(input.length)

        return read
    }

    fun nextUntilAndSkip(predicate: (Char) -> Boolean): String {

        val read = nextUntil(predicate)
        index = (index + 1).coerceAtMost(input.length)

        return read
    }

    fun nextUntilAndKeep(vararg chars: Char): String {

        val minIndex = chars
            .map { input.indexOf(it, index) }
            .filter { it != -1 }
            .min()
            ?: input.length

        index = minIndex - 1

        return input.substring(index, minIndex)
    }

    fun nextUntilAndKeep(predicate: (Char) -> Boolean): String {

        val outputBuilder = StringBuilder()

        while (index < input.length) {

            val nextChar = input[index++]
            outputBuilder.append(nextChar)

            if (predicate(nextChar)) {
                break
            }
        }

        return outputBuilder.toString()
    }*/


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