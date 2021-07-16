package tech.poder.test

import me.camdenorrb.crescentvm.vm.ir.poder.tech.PoderTechIR
import me.camdenorrb.crescentvm.vm.ir.poder.tech.composer.PoderTechBinaryComposer
import java.nio.file.Files
import java.nio.file.Paths

object IRCheck {
    @JvmStatic
    fun main(args: Array<String>) {
        val fileBuilder = PoderTechBinaryComposer()
        val classA = fileBuilder.addClass("tech/poder/A", "TODO")
        val methodMain = classA.addMethod("main", "()V")
        methodMain.loadConst(5)
        methodMain.loadConst(5)
        methodMain.add()
        methodMain.returnLast()
        methodMain.compile()
        classA.compile()
        fileBuilder.compile()
        println(fileBuilder.instance)
        Files.write(Paths.get("Example.bin"), fileBuilder.instance.toCode())
        val bytes = Files.readAllBytes(Paths.get("Example.bin"))
        val neutralInstance = PoderTechIR()
        neutralInstance.appendFromFile(bytes)
        println(neutralInstance)
    }
}