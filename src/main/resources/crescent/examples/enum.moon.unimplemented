enum Color(name: String) {
    RED("Red")
    GREEN("Green")
    BLUE("Blue")
}

fun main {

    # .random() will be built into the Enum type implementation

    val color = Color.random()

    # Shows off cool Enum shorthand for when statements
    when color {

        is .RED   -> {}
        is .GREEN -> {}

        else -> {}
    }

    when name = color.name {

        "Red"   -> println(name)
        "Green" -> {}

        else -> {}
    }

}