package me.camdenorrb.crescentvm.vm.jvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import proguard.classfile.ClassPool
import proguard.io.DataEntryClassWriter
import proguard.io.FixedFileWriter
import proguard.io.JarWriter
import proguard.io.ZipWriter
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists

object JVMGenerator {

    fun generate(input: List<CrescentAST.Node.File>) {
        input.forEach { assembly ->
            val pool = ClassPool()
            val output = Paths.get(assembly.name).toAbsolutePath()
            assembly.enums.forEach {

            }
            output.deleteIfExists()
            val jarWriter = JarWriter(
                ZipWriter(
                    FixedFileWriter(
                        output.toFile()
                    )
                )
            )
            pool.classesAccept(DataEntryClassWriter(jarWriter))
            jarWriter.close()
        }
    }
}