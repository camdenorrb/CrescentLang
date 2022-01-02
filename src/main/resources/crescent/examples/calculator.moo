fun main {

    val input1 = readDouble("Enter your first number")
    val input2 = readDouble("Enter your second number")
    val operation = readLine("Enter a operation [+, -, *, /]")

    val result = when(operation) {
        '+' -> input1 + input2
        '-' -> input1 - input2
        '*' -> input1 * input2
        '/' -> input1 / input2
    }

    println(result)
}