package me.camdenorrb.crescentvm.iterator

class PeekingCharIterator(val input: String): Iterator<Char> {

    @PublishedApi
    internal var index = 0


    override fun hasNext(): Boolean {
        return index < input.length
    }

    override fun next(): Char {
        return input[index++]
    }


    fun next(size: Int): String {
        index += size
        return input.substring(index - size, index)
    }

    fun nextUntil(chars: Set<Char>): String {
        return nextUntil {
            it in chars
        }
    }

    fun nextUntil(char: Char): String {
        return nextUntil {
            char == it
        }
    }


    inline fun nextUntil(predicate: (Char) -> Boolean): String {
        return buildString {
            while (index < input.length && !predicate(input[index])) {
                append(input[index])
                index++
            }
        }
    }


    fun nextUntilAndSkip(char: Char): String {
        return nextUntilAndSkip {
            it == char
        }
    }

    fun nextUntilAndSkip(chars: Set<Char>): String {
        return nextUntilAndSkip {
            it in chars
        }
    }

    inline fun nextUntilAndSkip(predicate: (Char) -> Boolean): String {

        val result = nextUntil(predicate)
        next()

        return result
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

    fun peekNext(amount: Int = 0): Char {
        return input[index + amount]
    }

    inline fun peekNextUntil(predicate: (Char) -> Boolean): String {

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