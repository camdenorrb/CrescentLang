# Declaring an async function makes it return a future
async fun do_task -> String {
    -> "Things"
}

fun main {

    // Do blocking
    do_task()

    // Do async via reference
    launch(::do_task)

    // Do async expanded
    launch { do_task() }

}