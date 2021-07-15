package me.camdenorrb.crescentvm.vm.ir

interface Language {
    val name: String
    fun appendFromFile(file: ByteArray)
    fun appendFromOtherLang(language: Language)
    fun toCode(): ByteArray
    fun registerConverter(converter: IRConverter, from: Language, to: Language)
    fun convertTo(other: Language): Language
}