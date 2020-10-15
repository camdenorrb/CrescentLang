package me.camdenorrb.crescentvm.iterator

import me.camdenorrb.crescentvm.vm.CrescentToken

// TODO: Make a peeking iterator interface
class PeekingTokenIterator(val input: List<CrescentToken>) : Iterator<CrescentToken> {

    @PublishedApi
    internal var index = 0


    override fun hasNext(): Boolean {
        return index < input.size
    }

    override fun next(): CrescentToken {
        return input[index++]
    }


    fun back(): CrescentToken {
        return input[--index]
    }


    fun peekNext(): CrescentToken {
        return input[index]
    }

    fun peekBack(amount: Int = 1): CrescentToken {
        return input[index - amount]
    }


    inline fun peekBackUntil(predicate: (CrescentToken) -> Boolean): List<CrescentToken> {

        var currentIndex = index - 1
        val result = mutableListOf<CrescentToken>()

        while (currentIndex > 0) {

            val token = input[currentIndex--]

            if (predicate(token)) {
                break
            }

            result += token
        }

        return result
    }

}