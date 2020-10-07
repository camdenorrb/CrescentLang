struct Example {
    val aNumber: Int          # New lines makes commas redundant
    val aValue1, aValue2 = "" # Multi declaration of same type, can all be set to one or multiple default values
}

main impl Example {

    # All implementation methods

    fun add(value1 value2: Int) -> Int {
        -> value1 + value2
    }

    fun sub(value1 value2: Int) -> Int {
        -> value1 - value2
    }

}


static impl Example {

}