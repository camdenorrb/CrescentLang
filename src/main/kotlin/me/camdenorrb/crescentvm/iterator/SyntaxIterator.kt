package me.camdenorrb.crescentvm.iterator

import me.camdenorrb.crescentvm.lexerold.CrescentToken
import me.camdenorrb.crescentvm.vm.CrescentToken

// Maybe turn into a SyntaxOutputStream?
class SyntaxIterator(input: String) {

    private val charIterator = PeekingCharIterator(input)


    fun nextToken(): CrescentToken {

        charIterator.nextUntil {
            !it.isWhitespace()
        }

        val type = charIterator.nextUntil {
            it.isWhitespace()
        }

        when (type) {

            "import" -> {

            }

            "struct" -> {

            }

            "trait" -> {

            }

            "fun" -> {

            }

            "val", "var" -> {

            }

            else -> error("Unknown type: $type")
        }

    }

    private fun readImport() {

    }

}
