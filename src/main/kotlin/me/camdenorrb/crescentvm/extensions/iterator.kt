package me.camdenorrb.crescentvm.extensions

// NOTE: Skips the one that returns true
fun <T> Iterator<T>.nextUntilAndSkip(predicate: (T) -> Boolean): List<T> {

    val elements = mutableListOf<T>()

    while (hasNext()) {

        val next = next()

        if (predicate(next)) {
            break
        }

        elements += next
    }

    return elements
}