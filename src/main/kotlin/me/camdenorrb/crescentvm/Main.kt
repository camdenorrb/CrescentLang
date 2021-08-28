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
                    
            fun printCircleLine(size radius: Any) {           

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


        val file = CrescentParser.invoke(Path(""), CrescentLexer.invoke(code).also { println(it) })

        println()

        println(file.functions["printCircleLine"]?.innerCode)
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