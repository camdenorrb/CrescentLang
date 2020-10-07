# Declaring an async function makes it return a future
async fun do_task -> String {
    -> "Things"
}

fun main {

    // Do blocking
    do_task().await

    // Do async simplified
    async(::do_task)

    // Do async expanded
    async { do_task().await }

}