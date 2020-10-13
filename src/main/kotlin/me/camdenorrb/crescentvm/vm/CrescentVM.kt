package me.camdenorrb.crescentvm.vm

class CrescentVM {

    fun lex(input: String): List<CrescentToken> {
        return CrescentLexer.invoke(input)
    }

    fun parse(input: List<CrescentToken>): CrescentAST.Node.File {
        return TODO()//CrescentParser.invoke(input)

    }

    fun invoke(input: List<CrescentAST.Node.File>) {

    }

}