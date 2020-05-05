package me.camdenorrb.crescentvm.lexer

data class CrescentToken(
    val name: String?,
    val value: String,
    val type: CrescentOpCode
)