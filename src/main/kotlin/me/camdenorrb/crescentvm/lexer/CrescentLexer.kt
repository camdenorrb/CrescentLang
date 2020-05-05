package me.camdenorrb.crescentvm.lexer

import me.camdenorrb.crescentvm.iterator.PeekingCharIterator

class CrescentLexer(private val input: String) {

    operator fun invoke(): List<CrescentOpCode> {

        val tokens   = mutableListOf<CrescentToken>()
        val iterator = PeekingCharIterator(input.trimStart())

        iterator.nextUntil {
            it.isLetter()
        }

        // Read structure name
        val structureName = iterator.nextUntil('(', '{')

        // Read Structure parameters
        while (iterator.peekNext().let { it != '{' && it != ')' }) {

            // Go to start of variable
            iterator.nextUntil {
                it.isLetter()
            }

            val identifier = when (val value = iterator.nextUntil(' ')) {

                "val" -> CrescentOpCode.VAL
                "var" -> CrescentOpCode.VAR

                else -> error("Unknown Crescent Type Token: $value")
            }

            // Skip space
            iterator.next()

            val name  = iterator.nextUntil(' ').trim()
            val value = iterator.nextUntil('\n', ',', '/', ')').trimStart()

            // Skip comments
            if (iterator.peekNext() == '/') {
                iterator.nextUntil('\n')
            }

            println("$identifier $name = $value")

            // Get to next character to help while loop
            iterator.nextUntil {
                it != ' ' && it != '\n'
            }
        }

        println(structureName)

        skipToMethod(iterator)

        // Read functions until end
        while (true) {

            // Function Data
            val identifier = iterator.nextUntil(' ').trimStart()
            iterator.next()
            val name = iterator.nextUntil(' ', '(', '{').trimStart()

            val parameters = iterator.nextUntil(')', '{').removePrefix("(")

            iterator.nextUntil('}')
            println("Identifier: $identifier, Name: $name, Parameters: $parameters")
            skipToMethod(iterator)

        }

        return emptyList()
    }

    private fun readVariable() {

    }

    fun skipToMethod(iterator: PeekingCharIterator) {
        iterator.nextUntil {
            it.isLetter() || it.isDigit() || it == '/'
        }
        while (iterator.peekNext() == '/') {
            iterator.nextUntil(',', '\n')

            iterator.nextUntil {
                it.isLetter()
            }

            iterator.nextUntil {
                it.isLetter() || it.isDigit() || it == '/'
            }
        }

    }

}