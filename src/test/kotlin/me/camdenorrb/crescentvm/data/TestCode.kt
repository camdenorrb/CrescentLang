package me.camdenorrb.crescentvm.data

/*
TODO:
	- Bit shifting
	- Triangles
	- Negative numbers and multiplication through parenthesis
	- Function blocks as parameters and values for variables
	- Function references
*/
internal object TestCode {

	const val helloWorlds =
		"""
			fun main {
                println("Hello World")
                println(("Hello World"))
                println(((((((((("Hello World"))))))))))
            }
		"""

	const val argsHelloWorld =
		"""
			fun main(args: [String]) {
                println(args[0])
            }
		"""

	const val funThing =
		"""         
			
			fun funThing1 {
			    println("I am a fun thing :)")
			}
			
			fun funThing2(input: String) {
			    println(input)
			}
			
			fun funThing3(input1 input2: String) {
			    println(input1 + input2)
			}
			
			fun funThing4(input1: String, input2: String) {
			    println(input1 + input2)
			}
			
			fun funThing5 -> String {
				-> "Meow"
			}
			
			fun funThing6() -> String {
				-> "Meow"
			}
			
			fun main {
				funThing1()
				funThing2("Meow")
				funThing3("Me", "ow")
				funThing4("Me", "ow")
				println(funThing5())
				println(funThing6())
			}
		"""

	val strings =
		"""			
			fun main {
				val number = 1			
				var thing = "Me"
				
				thing += "ow"
				
			    println("Meow ${'$'}number")
				println(thing)
			}
		"""


	const val variables =
		"""
		    fun main {
				
				var thing1 = "Meow"
				var thing2 = 1
				var thing3: Char = 'c'
				var thing4: i32
				
				thing1 = "Mew"
				thing2 += 1
				thing3 = 'a'
				thing4 = 1
				
				println(thing1)
				println(thing2)
				println(thing3)
				println(thing4)
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

	// Can't be constant due to lack of escaping $'s :C
	val stringInterpolation =
		"""
		    fun main {
		        val x, y, z = 0
				println("${'$'}x${'$'}y${'$'}z")
            }
		"""

	// Can't be constant due to lack of escaping $'s :C
	val forLoop =
		"""
		    fun main {
		     
		        val x, y, z = 0
                
				println("${'$'}x${'$'}y${'$'}z")

			    for x, y, z in 0..10 {
			        println("${'$'}x${'$'}y${'$'}z")
			    }
			
			    for x, y, z in 0..10, 0..10, 0..10 {
			        println("${'$'}x${'$'}y${'$'}z")
			    }

                println("Hello World")
            }
		"""

	val whileLoop =
		"""
		    fun main {
		     
		        var x = 1
                
				while (x <= 10) {
					println(x)
					x += 1
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
            const thing1 = "Mew"
            
            object Constants {
            
                const thing2 = "Meow"
            
				fun printThing() {
					println(thing1)
					println(thing2)
				}
            }
			
			fun main {
				Constants.printThings()
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
	
				fun printValues {
					println(aNumber)
					println(aValue1)
					println(aValue2)
				}
				
            }

            # Can't use self in static syntax
            impl static Example {
			
                fun add(value1 value2: Int) -> Int {
	                -> value1 + value2
	            }
	
	            fun sub(value1 value2: Int) -> Int {
	                -> value1 - value2
	            }
				
            }
			
			
			fun main {
				val example = Example(1, "Meow", "Mew")
			    
				example.printValues()
				
				println()
				
				println(example.aNumber)
				println(example.aValue1)
				println(example.aValue2)
				
				println(Example.add(1, 2))
				println(Example.sub(1, 2))
			}
        """

	const val math =
		"""
            fun main {
                println((1.0 + 1) + 1.0 / 10.0 + 1000.0 * 10.0 / 11.0 ^ 10.0)
				println(4(3) + 1)
            }
		"""

	const val triangleRecursion =
		"""
			fun printSpaces(count: Any) {
				print(" ")
				if (count > 0) {
					printSpaces(count - 1)
				}
			}
			
			fun printStars(count: Any) {
				print("* ")
				if (count > 1) {
					printStars(count - 1)
				}
			}
			
			fun printTriangle(size: Any, max: Any) {
				if (size > 0) {
					printSpaces(size)
					printStars((max - size) + 1)
					println("")
					printTriangle(size - 1, max)
				}
			}
			
			fun printer(size: Any) {
				printTriangle(size, size)
			}
			
			fun main {
				printer(100)
			}
		"""

	const val sealed =
		"""
            sealed Example {
                struct Thing1(val name: String)
                struct Thing2(val id: i32)
				object Thing3
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
            
                    .RED   -> { println("Meow") }
                    .GREEN -> {}
            
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
            import crescent.examples::Thing2 as Thing3
            import crescent.examples::*

            # import crescent.examples as examples
            
            # Short hand method (If in same package)
            import ::Thing
            import ::Thing2 as Thing3			
		"""

}