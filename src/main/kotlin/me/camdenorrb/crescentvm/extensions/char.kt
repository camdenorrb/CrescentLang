package me.camdenorrb.crescentvm.extensions

fun Char.equalsAny(vararg chars: Char): Boolean {
    return chars.any { this == it }
}