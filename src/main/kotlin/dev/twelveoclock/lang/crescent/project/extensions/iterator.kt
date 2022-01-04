package dev.twelveoclock.lang.crescent.project.extensions


inline fun <T> Iterable<T>.anyIndexed(predicate: (index: Int, value: T) -> Boolean): Boolean {

    if (this is Collection && isEmpty()) {
        return false
    }

    for ((index, element) in this.withIndex()) {
        if (predicate(index, element)) return true
    }

    return false
}