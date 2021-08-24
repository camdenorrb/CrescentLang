package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.vm.*
import kotlin.io.path.*

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        val path = this::class.java.getResource("/crescent/examples/hello_world.moon")?.toURI()?.toPath()
            ?: error("")
        */

        /*path.readText()*/

        val code =
            """
                fun main(args: [String]) {
                    println(args[0])
                    println('M')
                    println("Hello World")
                    println("Hello World")
                    println("Meow")
                }
            """

        val file = CrescentParser.invoke(Path(""), CrescentLexer.invoke(code))
        CrescentVM(listOf(file), file).invoke(listOf("Meowwwwww"))
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