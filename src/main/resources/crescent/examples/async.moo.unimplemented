# Declaring an async function makes it return a future
async fun doTask -> String {
    -> "Things"
}

fun main {

    // Do blocking
    doTask()

    // Do async via reference
    launch(::doTask)

    // Do async expanded
    launch { doTask() }

}