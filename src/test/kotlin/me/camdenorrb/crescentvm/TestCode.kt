package me.camdenorrb.crescentvm

object TestCode {

	const val helloWorld =
		"""
			fun main {
                println("Hello World")
            }
		"""

	const val ifStatement =
		"""
            fun main(args: [String]) {
                if (args[0] == "true") {
                    println("Meow")
                }
                else {
                    println("Hiss")
                }
            }
        """

	const val enum =
		"""
            enum Color(name: String) {
                RED("Red")
                GREEN("Green")
                BLUE("Blue")
            }
            
            fun main {
            
                # .random() will be built into the Enum type implementation
            
                val color = Color.random()
            
                # Shows off cool Enum shorthand for when statements
                when(color) {
            
                    is .RED   -> { println("Meow") }
                    is .GREEN -> {}
            
                    else -> {}
                }
            
                when(name = color.name) {
            
                    "Red"   -> println(name)
                    "Green" -> {}
            
                    else -> {}
                }
            
            }
        """


	const val impl =
		"""
            struct Example(
                val aNumber: Int          # New lines makes commas redundant
                val aValue1 aValue2 = ""  # Multi declaration of same type, can all be set to one or multiple default values
            )

            impl Example {

	            # All implementation methods
	
	            fun add(value1 value2: Int) -> Int {
	                -> value1 + value2
	            }
	
	            fun sub(value1 value2: Int) -> Int {
	                -> value1 - value2
	            }

            }

            # Can't use self in static syntax
            impl static Example {

            }
        """

}