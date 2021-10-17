package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.compiler.CrescentIRCompiler
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentIRVM
import kotlin.io.path.Path


object Main {

    // TODO: Replace all checkEquals in project with check and a custom message
    @JvmStatic
    fun main(args: Array<String>) {

        //MemorySegment.allocateNative(2, ResourceScope.globalScope())

        /*
        val path = this::class.java.getResource("/crescent/examples/hello_world.moon")?.toURI()?.toPath()
            ?: error("")
        */

        /*path.readText()*/


        /*
        val code =
            """
                fun makeCircle(radius: Any, width: Any, symbol: Any) {
                
                    var y = -1 * radius
                    var x = -1 * radius
            
                    while(y <= radius) {
                            while(x <= radius) {
                                    var edge = (x * x + y * y) / radius - radius
                                    
                                    if(edge > -1 * width * 4 / 3 && edge < 1) {
                                        print(symbol)
                                    } 
                                    else {
                                        print("  ")
                                }
                                x = x + 1
                            }
                            
                            println("")
                            x = -1.0 * radius
                            y = y + 1
                    }
                }
            
                fun main {
                    makeCircle(12, 3.0, "**")
                }
		    """
		    */

        val code =
            """
                fun main {
                    
                    var i = 0
                    
                    while (i < 2) {
                        println("Meow")
                        i += 1
                    }
                }
            """.trimIndent()

        val file = CrescentParser.invoke(Path(""), CrescentLexer.invoke(code))
        CrescentIRVM(CrescentIRCompiler.invoke(file).also { println(it) }).invoke()

        /*
        file.functions.forEach { println("\n$it\n") }
        CrescentVM(listOf(file), file).invoke()
        */
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