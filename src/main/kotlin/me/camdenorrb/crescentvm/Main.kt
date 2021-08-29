package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.vm.*
import kotlin.io.path.Path

object Main {

    // TODO: Replace all checkEquals in project with check and a custom message
    @JvmStatic
    fun main(args: Array<String>) {

        /*
        val path = this::class.java.getResource("/crescent/examples/hello_world.moon")?.toURI()?.toPath()
            ?: error("")
        */

        /*path.readText()*/

        val code =
            """
fun makeCircle(radius: Any, width: Any, symbol: Any) {
        var y = -radius
        var x = -radius

        while(y <= radius) {
                while(x <= radius) {
                        var edge = (x * x + y * y) / radius - radius
                        if(edge > width * 4 / 3 && edge < 1) {
                                print(symbol)
                        } else {
                                print(" ")
                        }
                        x = x + 1
                }
                println("")
                y = y + 1
        }
}

fun main {
        makeCircle(16, 3.0, "*")
}
            
		    """
            /*
            """             
                fun repeatPrint(input: Any, amount: Any) {
                
                  print(input)
                  
                  if (amount > 1) {
                      repeatPrint('*', amount - 1)
                  }
                }
                
                fun printStars(number: Any, countUp: Any) {
                
                  repeatPrint('*', number)
                  println("")
                   
                  if (countUp) {
                    if (number < 10) {
                      printStars(number + 1, true)
                    }
                    else {
                      printStars(number - 1, false)
                    }
                  }
                  else {
                    if (number > 1) {
                      printStars(number - 1, false)
                    }
                  }
                }
                fun main {
                    printStars(1, true)
                }
            """*/


        val file = CrescentParser.invoke(Path(""), CrescentLexer.invoke(code))

        println()

        println(file.functions.forEach { println("\n$it\n") })
        CrescentVM(listOf(file), file).invoke()
    }


    /*
    @JvmStatic
    fun main(args: Array<String>) {

        val file =
            if (args.isNotEmpty()) {
                Paths.get(args[0]).toAbsolutePath()
            }
            else {
                this::class.java.getResource("/crescent/examples/math.moon")?.toURI()?.toPath()?.toAbsolutePath()
            }

        check(file != null && file.exists()) {
            "File '$file' does not exist"
        }

        val files =
            if (file.isDirectory()) {
                Files.walk(file)
                    .filter { !it.isDirectory() && it.extension == "moon" }
                    .collect(Collectors.toList())
            }
            else {
                listOf(file)
            }

        val assemblies = mutableSetOf<CrescentAST.Node.File>()
        val vm = CrescentVM(VM.JVM_BYTECODE)

        files.forEach { path ->
            val tokens = vm.lex(path.readText())
            println(tokens)
            assemblies.addAll(vm.parse(path, tokens))
        }

        vm.invoke(assemblies)
    }*/

}