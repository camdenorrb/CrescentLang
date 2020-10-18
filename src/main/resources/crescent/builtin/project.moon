# For project level builtins

inline fun repeat(times: Int, block: (Int)) {
    for i in 0 until times {
        block(i)
    }
}