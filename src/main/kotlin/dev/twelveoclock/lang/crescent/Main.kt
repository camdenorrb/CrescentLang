package dev.twelveoclock.lang.crescent

import me.camdenorrb.crescentvm.compiler.CrescentIRCompiler
import me.camdenorrb.crescentvm.lexers.CrescentLexer
import me.camdenorrb.crescentvm.parsers.CrescentParser
import me.camdenorrb.crescentvm.vm.CrescentIRVM
import kotlin.io.path.Path


object Main {

    // TODO: Replace all checkEquals in project with check and a custom message
    @JvmStatic
    fun main(args: Array<String>) {

    }


    private fun testCode() {

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
    }
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
