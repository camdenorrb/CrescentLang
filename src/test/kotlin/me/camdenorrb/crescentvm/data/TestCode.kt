package me.camdenorrb.crescentvm.data

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

	const val ifInputStatement =
		"""
            fun main {

                val input = readBoolean("Enter a boolean value [true/false]")

                if (input) {
                    println("Meow")
                }
                else {
                    println("Hiss")
                }
            }
        """

	const val calculator =
		"""
            fun main {
            
                val input1 = readDouble("Enter your first number")
                val input2 = readDouble("Enter your second number")
                val operation = readLine("Enter an operation [+, -, *, /]")
            
                val result = when(operation) {
                    '+' -> input1 + input2
                    '-' -> input1 - input2
                    '*' -> input1 * input2
                    '/' -> input1 / input2
                }
            
                println(result)
            }
        """
	const val constantsAndObject =
		"""
            const thing = "Meow"
            
            object Constants {
            
                const thing = "Meow"
            
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

	const val math =
		"""
            fun main {
                println((1 + 1) + 1 / 10 + 1000 * 10 / 10 ^ 10)
            }
		"""

	const val sealed =
		"""
            sealed Example {
                struct Thing1(val name: String)
                struct Thing2(val id: i32)
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

	const val comments =
		"""
            # Project level comment
            fun main {
                println#("Meow")
                #Meow
                # Meow
                "#meow"
                1 +#Meow
                1 -#Meow
                1 /#Meow
                1 *#Meow
                1 =#Meow
            #}			
		"""

	const val imports =
		"""
            # Current idea, Package -> Type
            import crescent.examples::Thing

            # import crescent.examples as examples
            
            # Short hand method (If in same package)
            import ::Thing	
		"""

}