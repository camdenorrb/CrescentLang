package me.camdenorrb.crescentvm.vm.jvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import proguard.classfile.AccessConstants
import proguard.classfile.ClassPool
import proguard.classfile.VersionConstants
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.editor.*
import proguard.classfile.util.ClassInitializer
import proguard.classfile.util.StringSharer
import proguard.classfile.visitor.ClassCleaner
import proguard.classfile.visitor.ClassPoolFiller
import proguard.classfile.visitor.MultiClassVisitor
import proguard.io.*
import proguard.preverify.CodePreverifier
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists

object JVMGenerator {
    const val CLASS_VERSION = VersionConstants.CLASS_VERSION_16_MAJOR shl 16 or VersionConstants.CLASS_VERSION_15_MINOR

    val jmodPool by lazy {
        val jmods = Paths.get(
            Paths.get(
                ProcessHandle.current()
                    .info()
                    .command()
                    .orElseThrow()
            ).toAbsolutePath().parent.parent.toString(), "jmods"
        ).toAbsolutePath()
        val source = DirectorySource(jmods.toFile())
        val pool = ClassPool()
        source.pumpDataEntries {
            if (!it.isDirectory) {
                JarReader(
                    true,
                    ClassFilter(
                        ClassReader(
                            true, false, false, false, null,
                            ClassPoolFiller(pool)
                        )
                    )
                )
            }
        }
        pool.classesAccept(ClassInitializer(pool, pool))
        pool
    }

    fun generate(input: List<CrescentAST.Node.File>) {
        input.forEach { assembly ->
            val pool = ClassPool()
            val output = Paths.get(assembly.name).toAbsolutePath()
            assembly.objects.forEach { p -> //Singleton
                val builder = ClassBuilder(CLASS_VERSION, AccessConstants.PUBLIC or AccessConstants.FINAL, "${assembly.name}.${p.name}", null)
                p.functions.forEach {
                    makeFunction(builder, it)
                }
                pool.addClass(builder.programClass)
            }
            if (assembly.mainFunction != null) {
                val builder = ClassBuilder(CLASS_VERSION, AccessConstants.PUBLIC or AccessConstants.FINAL, "${assembly.name}.Main", null)
                makeFunction(builder, assembly.mainFunction)
                pool.addClass(builder.programClass)
            }

            assembly.enums.forEach { enum ->
                val builder = ClassBuilder(CLASS_VERSION, AccessConstants.PUBLIC or AccessConstants.FINAL or AccessConstants.ENUM, "${assembly.name}.${enum.name}", null)
                enum.structs.forEach {
                    makeStruct(builder, it)
                }
                enum.variables.forEach {
                    makeVariable(builder, it)
                }
                pool.addClass(builder.programClass)
            }
            assembly.impls.forEach { impl ->

            }
            assembly.structs.forEach { struct -> //Data Class?

            }
            assembly.traits.forEach { trait -> //Interface?

            }
            check (pool.size() > 0) {
                "No classes were generated!"
            }
            pool.classesAccept(ClassInitializer(pool, jmodPool))
            pool.classesAccept(
                MultiClassVisitor(
                    AllAttributeVisitor(true, CodePreverifier(false)),
                    AccessFixer(),
                    InnerClassesAccessFixer(),
                    StringSharer(),
                    NameAndTypeShrinker(),
                    BootstrapMethodsAttributeShrinker(),
                    ConstantPoolShrinker(),
                    InterfaceSorter(),
                    AttributeSorter(),
                    ClassMemberSorter(),
                    ClassElementSorter(),
                    ConstantPoolSorter(),
                    ClassCleaner()
                )
            )
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

    private fun makeFunction(classBuilder: ClassBuilder, code: CrescentAST.Node.Function) {

    }

    private fun makeStruct(classBuilder: ClassBuilder, struct: CrescentAST.Node.Struct) {

    }

    private fun makeVariable(classBuilder: ClassBuilder, variable: CrescentAST.Node.Variable) {

    }
}