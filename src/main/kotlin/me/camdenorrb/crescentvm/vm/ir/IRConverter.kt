package me.camdenorrb.crescentvm.vm.ir

interface IRConverter {
    fun convert(fromLang: Language, toLang: Language, withCode: ByteArray): ByteArray
}