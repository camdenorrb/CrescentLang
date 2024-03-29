package dev.twelveoclock.lang.crescent.data

/*
TODO:
	- Bit shifting
	- Triangles
	- Negative numbers and multiplication through parenthesis
	- Function blocks as parameters and values for variables
	- Function references
	- Arrays
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
			
			fun funThing5(input: I32) {
				println(input)
			}
			
			fun funThing6 -> String {
				-> "Meow"
			}
			
			fun funThing7() -> String {
				-> "Meow"
			}
			
			fun funThing8(input: Any) {
				println(input)
			}
			
			fun main {
				funThing1()
				funThing2("Meow")
				funThing3("Me", "ow")
				funThing4("Me", "ow")
				funThing5(-5)
				println(funThing6())
				println(funThing7())
				println(funThing8("Cats"))
			}
		"""

	const val strings =
		"""			
			fun main {
				val number = 1			
				var thing = "Me"
				
				thing += "ow"
				
			    println("Meow ${'$'}number")
				println(thing)
				
				
				val multiLine1 = \"\"\"
					Meow
					Meow
				\"\"\"
				
				println(multiLine1)
				
				val multiLine2 = 
					\"\"\"
						Meow2
						Meow2
					\"\"\"
				
				println(multiLine2)
			}
		"""


	const val variables =
		"""
		    fun main {
				
				var thing1 = "Meow"
				var thing2 = 1
				var thing3: Char = 'c'
				var thing4: I32
				var thing5 = [1]
				var thing6 = ["Meow"]
				
				thing1 = "Mew"
				thing2 += 1
				thing3 = 'a'
				thing4 = 1
				thing5[0] = 2
				thing6[0] = "Mew"
				
				println(thing1)
				println(thing2)
				println(thing3)
				println(thing4)
				println(thing5)
				println(thing6)
			}
		"""

	const val ifStatement =
		"""
			fun test1(args: [String]) {
				if (args[0] == "true") {
					println("Meow")
                }
                else {
                    println("Hiss")
                }
			}
			
			fun test2(args: [String]) -> String {
				if (args[0] == "true") {
                    -> "Meow"
                }
                else {
                    -> "Hiss"
                }
				
				println("This shouldn't be printed")
			}
			
            fun main(args: [String]) {
               test1(args)
			   println(test2(args))
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
	const val stringInterpolation =
		"""
		    fun main {
		        
				val x, y, z = 0
				
				println("${'$'}x${'$'}y${'$'}z")
				println("Hello ${'$'}x${'$'}y${'$'}z Hello")
				println("Hello ${'$'}x Hello ${'$'}y Hello ${'$'}z Hello")
				
				println("${'$'}{x}${'$'}{y}${'$'}{z}")
				println("Hello ${'$'}{x}${'$'}{y}${'$'}{z} Hello")
				println("Hello ${'$'}{x}Hello${'$'}{y}Hello${'$'}{z} Hello")
			
				# Should printout a dollar sign
				println("${'$'}{${"'$'"}}")
				
				# Should println dollar sign next to the letter x
			    println("\${'$'}x")
				
			    println("$ x")
            }
		"""

	// Can't be constant due to lack of escaping $'s :C
	const val forLoop1 =
		"""
		    fun main {
		     
		        val x, y, z = 0
                
				println("${'$'}x${'$'}y${'$'}z")

 				for x in 0..9 {
			        println("${'$'}x")
			    }
			
			    for x, y, z in 0..9 {
			        println("${'$'}x${'$'}y${'$'}z")
			    }
			
			    for x, y, z in 0..9, 0..9, 0..9 {
			        println("${'$'}x${'$'}y${'$'}z")
			    }

                println("Hello World")
            }
		"""

	const val forLoop2 =
		"""
		    fun main {
		     
		        val x, y, z = 0
                
				println("" + x + y + z)
			
			    for x, y, z in 0..10, 0..10, 0..10 {
			        println("x: " + x + " y: " + y + " z: " + z)
			    }

                println("Hello World")
            }
		"""

	const val whileLoop =
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
            
				fun printThings() {
					println(thing1)
					println(thing2)
				}
            }
			
			fun main {
				Constants.printThings()
				println(thing1)
				println(Constants.thing2)
			}
        """

	const val struct =
		"""
			
	        struct Example(
                val aNumber: I8           # New lines makes commas redundant
                val aValue1 aValue2 = ""  # Multi declaration of same type, can all be set to one or multiple default values
            )
			
			fun main {
				val example = Example(1, "Mew", "Meow")
				println(example)
				println(example.aNumber)
				println(example.aValue1)
				println(example.aValue2)
			}
			
		"""

	// TODO: Add traits and inheriting  impl Example : Cat
	const val impl =
		"""
			
            struct Example(
                val aNumber: I32          # New lines makes commas redundant
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
			
                fun add(value1 value2: I32) -> I32 {
	                -> value1 + value2
	            }
	
	            fun sub(value1 value2: I32) -> I32 {
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
				println(4 * (3) + 1)
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

	const val nateTriangle =
		"""
			fun triangle(n: Any, k: Any){
				if (n >= 0){
				
					triangle(n-1, k+1);
					
					var x: I32 = 0;
					var y: I32 = 0;
					while (x < k){
						print(" ")
						x = x + 1
					}
					
					while (y < n){
						print("* ")
						y = y + 1
					}
					println()
				}	
			}
			
			fun main() {
				triangle(5, 0)
			}
		"""

	const val katCircle =
		"""
			
		"""


	// This breaks Crescent due to the parenthesis in the while
	/*
        val code =
            """

            fun printCircleLine(size radius: Any) {

               print(radius)
               print(" ")
               print(size)

               var count = 0
               while (count < ((radius / 2) - size)) {
                 count = count + 1
               }

               count = 0
               while (count < size) {
                  print('*')
                  count = count + 1
               }
            }

            fun printCircle(radius: Any) {
              var count = 0
              while (count < radius) {
                printCircleLine(count, radius)
              }
            }

            fun main {
              printCircle(10)
            }
		    """
	*/
}