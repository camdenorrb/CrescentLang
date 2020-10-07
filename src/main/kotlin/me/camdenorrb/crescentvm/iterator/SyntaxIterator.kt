package me.camdenorrb.crescentvm.iterator

import me.camdenorrb.crescentvm.vm.CrescentToken

// Maybe turn into a SyntaxOutputStream?
class SyntaxIterator(input: String) {

    private val charIterator = PeekingCharIterator(input)


    fun nextToken(): CrescentToken {

        charIterator.nextUntil {
            !it.isWhitespace()
        }

        val key = charIterator.nextUntil {
            !it.isLetterOrDigit()
        }

        val tokens = mutableListOf<CrescentToken>()

        while (charIterator.hasNext()) {
            tokens += when (key) {

                "import" -> readImport()
                "struct" -> readStruct()
                "trait"  -> readTrait()
                "impl"   -> readImpl(false)
                "static" -> readImpl(true)

                else -> error("Unknown key: $key")
            }
        }

    }

    private fun readImport(): CrescentToken.Import {
        return CrescentToken.Import(charIterator.nextUntil(' ', '\n'))
    }

    private fun readStruct(): CrescentToken.Struct {

    }

    private fun readTrait(): CrescentToken.Trait {

    }

    private fun readImpl(isStatic: Boolean): CrescentToken {

        if (isStatic) {
            check(charIterator.nextUntil(' ') == "impl")
        }

        val name = charIterator.nextUntil(' ')

        // Skip to inner
        charIterator.nextUntilAndSkip('{')

        val implCharIterator = PeekingCharIterator(charIterator.nextUntilAndSkip('}'))
        val functions = mutableListOf<CrescentToken.ImplFunction>()

        implCharIterator.nextUntil { it.isLetter() }

        while (implCharIterator.hasNext()) {

            check(implCharIterator.nextUntil(' ') == "fun")
            val name = implCharIterator.nextUntil(' ')

            functions += readImplFunction(name)
        }

        return CrescentToken.Impl(name, isStatic, functions)
    }

    private fun readStructFunction(): CrescentToken.ImplFunction {

        val name = charIterator.nextUntil(' ', '(', '{')

        val parameters = charIterator.nextUntilAndSkip('{').substringAfter('(', "").substringBefore(')', "").split(",")
            .map { it.trim() }
            .map {
                val (name, type) = it.split(":")
                CrescentToken.FunctionParameter(name, CrescentToken.Type(type))
            }

    }


    private fun readImplFunction(name: String): CrescentToken.ImplFunction {

        val innerCharIterator = PeekingCharIterator(charIterator.nextUntilAndSkip('}'))

        while (innerCharIterator.hasNext()) {

            // Skip whitespace
            innerCharIterator.nextUntil {
                it.isLetter()
            }

            when (val key = innerCharIterator.nextUntil(' ')) {
                "val", "var" -> { }
                "if" -> { }
                "when" -> { }
                "for" -> {}
                "while" -> {}
                "error" -> {}
            }
        }

    }

    private fun readVariable {

    }


}
