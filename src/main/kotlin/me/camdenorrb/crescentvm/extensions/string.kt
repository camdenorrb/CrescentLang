package me.camdenorrb.crescentvm.extensions

fun String.containsAll(vararg chars: Char): Boolean {
    return chars.all { this.contains(it) }
}