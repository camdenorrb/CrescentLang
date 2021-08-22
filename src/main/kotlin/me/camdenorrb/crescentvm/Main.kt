package me.camdenorrb.crescentvm

import me.camdenorrb.crescentvm.vm.*
import kotlin.io.path.*

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val path = this::class.java.getResource("/crescent/examples/hello_world.moon")?.toURI()?.toPath()?.toAbsolutePath()
            ?: error("")

        CrescentVM().invoke(CrescentParser.invoke(path, CrescentLexer.invoke(path.readText())))
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