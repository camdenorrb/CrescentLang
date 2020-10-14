package me.camdenorrb.crescentvm.iterator

import me.camdenorrb.crescentvm.vm.CrescentToken

class PeekingTokenIterator(val input: List<CrescentToken>) : Iterator<CrescentToken> {

    private var index = 0


    override fun hasNext(): Boolean {
        return index < input.size
    }

    override fun next(): CrescentToken {
        return input[index++]
    }


    fun peekNext(): CrescentToken {
        return input[index]
    }

}