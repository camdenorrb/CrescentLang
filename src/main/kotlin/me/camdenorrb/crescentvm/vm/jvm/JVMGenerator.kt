package me.camdenorrb.crescentvm.vm.jvm

import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentToken
import proguard.classfile.*
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.editor.*
import proguard.classfile.util.ClassInitializer
import proguard.classfile.util.StringSharer
import proguard.classfile.visitor.ClassCleaner
import proguard.classfile.visitor.ClassPoolFiller
import proguard.classfile.visitor.MultiClassVisitor
import proguard.io.*
import proguard.preverify.CodePreverifier
import java.lang.IllegalStateException
import java.net.URI
import java.nio.file.*
import kotlin.io.path.deleteIfExists

data class JVMGenerator(val context: CodeContext = CodeContext()) {
    companion object {
        const val CLASS_VERSION =
            VersionConstants.CLASS_VERSION_16_MAJOR shl 16 or VersionConstants.CLASS_VERSION_15_MINOR

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
    }

    fun generate(input: List<CrescentAST.Node.File>) {
        input.forEach { assembly ->
            try {
                val pool = ClassPool()
                val output = Paths.get("${assembly.name}.jar").toAbsolutePath()
                assembly.objects.forEach { p -> //Singleton
                    val builder = ClassBuilder(
                        CLASS_VERSION,
                        AccessConstants.PUBLIC or AccessConstants.FINAL,
                        "${assembly.name}/objects/${p.name}",
                        ClassConstants.NAME_JAVA_LANG_OBJECT
                    )
                    p.functions.forEach {
                        makeFunction(builder, it)
                    }
                    pool.addClass(builder.programClass)
                }
                if (assembly.mainFunction != null) {
                    val builder = ClassBuilder(
                        CLASS_VERSION,
                        AccessConstants.PUBLIC or AccessConstants.FINAL,
                        "${assembly.name}/Main",
                        ClassConstants.NAME_JAVA_LANG_OBJECT
                    )

                    val newFunction = CrescentAST.Node.Function(
                        "main",
                        listOf(CrescentToken.Modifier.PUBLIC, CrescentToken.Modifier.STATIC),
                        assembly.mainFunction.visibility,
                        listOf(
                            CrescentAST.Node.Parameter.Basic(
                                "args",
                                CrescentAST.Node.Type.Array(CrescentAST.Node.Type.Basic("String"))
                            )
                        ),
                        CrescentAST.Node.Type.Unit,
                        assembly.mainFunction.innerCode
                    )
                    makeFunction(builder, newFunction)
                    pool.addClass(builder.programClass)
                }

                assembly.enums.forEach { enum ->
                    TODO()
                }
                val classes = mutableMapOf<String, ClassBuilder>()

                assembly.structs.forEach { struct -> //Class
                    val builder = ClassBuilder(
                        CLASS_VERSION,
                        AccessConstants.PUBLIC or AccessConstants.FINAL,
                        "${assembly.name}/structs/${struct.name}",
                        ClassConstants.NAME_JAVA_LANG_OBJECT
                    )
                    check(!classes.contains(struct.name)) {
                        "Duplicate struct: ${struct.name}!"
                    }
                    struct.variables.forEach {
                        makeVariable(builder, it)
                    }
                    classes[struct.name] = builder
                    pool.addClass(builder.programClass)
                }

                assembly.impls.forEach { impl ->
                    getImpl(impl, classes)
                }

                assembly.traits.forEach { trait -> //Interface
                    val builder = ClassBuilder(
                        CLASS_VERSION,
                        AccessConstants.PUBLIC or AccessConstants.INTERFACE,
                        "${assembly.name}/interfaces/${trait.name}",
                        ClassConstants.NAME_JAVA_LANG_OBJECT
                    )
                    trait.functionTraits.forEach {
                        makeFunction(
                            builder,
                            CrescentAST.Node.Function(
                                it.name,
                                listOf(CrescentToken.Modifier.PUBLIC),
                                CrescentAST.Visibility.PUBLIC,
                                it.params,
                                it.returnType,
                                CrescentAST.Node.Expression(emptyList())
                            )
                        )
                    }
                    pool.addClass(builder.programClass)
                }
                check(pool.size() > 0) {
                    "No classes were generated for \"${assembly.name}\""
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
                if (assembly.mainFunction != null) {
                    val manifest = "Manifest-Version: 1.0\nMain-Class: ${assembly.name}.Main\n"
                    val fs = createFileSystem(output)
                    addFile(fs, "META-INF/MANIFEST.MF", manifest.toByteArray())
                    fs.close()
                }
            } catch (ex: IllegalStateException) {
                ex.printStackTrace()
            }
        }
    }

    private fun getImpl(impl: CrescentAST.Node.Impl, map: MutableMap<String, ClassBuilder>) {
        when (impl.type) {
            is CrescentAST.Node.Type.Basic -> {
                val realType = impl.type.name
                check(map[realType] != null) {
                    "Struct $realType is missing!"
                }
                val clazz = map[realType]!!
                impl.functions.forEach {
                    makeFunction(clazz, it)
                }
            }
            is CrescentAST.Node.Type.Array -> {
                println("Arrays classes not implemented!")
            }
            else -> TODO(impl.type::class.java.name)
        }

    }

    private fun addFile(fs: FileSystem, path: String, byteArray: ByteArray) {
        val nf: Path = fs.getPath(path)
        try {
            val parent = nf.parent
            if (parent != null) {
                Files.createDirectories(parent)
            }
        } catch (ex: Exception) {
            println(path)
            ex.printStackTrace()
        }
        Files.write(
            nf,
            byteArray,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    private fun createFileSystem(file: Path): FileSystem {
        val env: MutableMap<String, String> = HashMap()
        env["create"] = "true"
        val uri: URI = URI.create("jar:" + file.toUri())
        return FileSystems.newFileSystem(uri, env)
    }

    private fun makeFunction(classBuilder: ClassBuilder, code: CrescentAST.Node.Function): ProgramMethod {
        var access = 0
        code.modifiers.forEach {
            when (it) {
                CrescentToken.Modifier.PUBLIC -> access = access or AccessConstants.PUBLIC
                CrescentToken.Modifier.PRIVATE -> access =
                    access or AccessConstants.PRIVATE or AccessConstants.SYNTHETIC
                CrescentToken.Modifier.STATIC -> access = access or AccessConstants.STATIC or AccessConstants.FINAL
                else -> {
                    println("Unknown Modifier: $it")
                }
            }
        }
        if (access and AccessConstants.PUBLIC == 0 && access and AccessConstants.PRIVATE == 0) {
            access = AccessConstants.PUBLIC
        }
        val description = StringBuilder("(")
        code.params.forEach {
            when (it) {
                is CrescentAST.Node.Parameter.Basic -> {
                    description.append(genDescriptor(it.type))
                }
                else -> {
                    TODO("Parse Parameter \"${it::class.java}\"")
                }
            }
        }
        description.append(")")
        //var isType = true
        description.append(genDescriptor(code.returnType))
        /*when (code.returnType) {
            is CrescentAST.Node.Type.Basic -> {
                description.append(genDescriptor(code.returnType))
            }
            is CrescentAST.Node.Type.Array -> TODO()
            is CrescentAST.Node.Type.Generic -> TODO()
            CrescentAST.Node.Type.Implicit -> TODO()
            is CrescentAST.Node.Type.Result -> TODO()
            CrescentAST.Node.Type.Unit -> {
                isType = false
                description.append("V")
            }
        }*/
        return if (code.innerCode.nodes.isEmpty()) {
            TODO()
        } else {
            classBuilder.addAndReturnMethod(access, code.name, description.toString(), 50) { codeBuilder ->

                codeList(codeBuilder, code.innerCode.nodes)
                if (code.innerCode.nodes.last() !is CrescentAST.Node.Return) {
                    codeBuilder.return_()
                }
                /*if (isType) {
                    codeBuilder.areturn()
                } else {
                    codeBuilder.return_()
                }*/
            }
        }
    }

    private fun codeList(codeBuilder: CompactCodeAttributeComposer, codes: List<CrescentAST.Node>) {
        codes.forEach {
            codeLaunch(codeBuilder, it)
        }
    }

    private fun codeLaunch(codeBuilder: CompactCodeAttributeComposer, node: CrescentAST.Node) {
        when (node) {
            is CrescentAST.Node.FunctionCall -> functionCall(codeBuilder, node)
            is CrescentAST.Node.Operation -> operation(codeBuilder, node)
            is CrescentAST.Node.Argument -> argument(codeBuilder, node)
            is CrescentAST.Node.Number -> number(codeBuilder, node)
            is CrescentAST.Node.String -> string(codeBuilder, node)
            is CrescentAST.Node.Expression -> codeList(codeBuilder, node.nodes)
            is CrescentAST.Node.Return -> return_(codeBuilder, node)
            else -> TODO("Node: $node")
        }
    }

    private fun return_(codeBuilder: CompactCodeAttributeComposer, return_: CrescentAST.Node.Return) {
        codeList(codeBuilder, return_.expression.nodes)
        if (return_.expression.nodes.isEmpty()) {
            codeBuilder.return_()
        } else {
            when (val result = context.stack.pop()) {
                is String -> {
                    codeBuilder.areturn()
                }
                else -> TODO("Return aType \"${result::class.java}\"")
            }
        }
    }

    private fun string(codeBuilder: CompactCodeAttributeComposer, string: CrescentAST.Node.String) {
        codeBuilder.ldc(string.data)
        context.stack.push(string.data)
    }

    private fun number(codeBuilder: CompactCodeAttributeComposer, number: CrescentAST.Node.Number) {
        when (val num = number.number) {
            is Double -> {
                context.stack.push(num)
                codeBuilder.ldc2_w(num)
            }
            else -> TODO("Parse NumberType: ${num::class.java}")
        }
    }

    private fun operation(codeBuilder: CompactCodeAttributeComposer, operation: CrescentAST.Node.Operation) {
        codeList(codeBuilder, listOf(operation.first, operation.second))
        when (operation.operator) {
            CrescentToken.Operator.NOT -> TODO()
            CrescentToken.Operator.ADD -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                when (test1) {
                    is Double -> {
                        codeBuilder.dadd()
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
                context.stack.push(test2)
            }
            CrescentToken.Operator.SUB -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                when (test1) {
                    is Double -> {
                        codeBuilder.dsub()
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
                context.stack.push(test2)
            }
            CrescentToken.Operator.MUL -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                when (test1) {
                    is Double -> {
                        codeBuilder.dmul()
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
                context.stack.push(test2)
            }
            CrescentToken.Operator.DIV -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                when (test1) {
                    is Double -> {
                        codeBuilder.ddiv()
                    }
                    else -> TODO("Number type: \"${test1::class.java}\" unrecognized")
                }
                context.stack.push(test2)
            }
            CrescentToken.Operator.POW -> {
                val test1 = context.stack.pop()
                val test2 = context.stack.pop()
                numCheck(test1, test2)
                val builder = StringBuilder("(")
                builder.append(genDescriptor(test1))
                builder.append(genDescriptor(test2))
                builder.append(")")
                builder.append(genDescriptor(test1))
                codeBuilder.invokestatic("java/lang/Math", "pow", builder.toString())
                context.stack.push(test2)
            }
            CrescentToken.Operator.REM -> TODO()
            CrescentToken.Operator.ASSIGN -> TODO()
            CrescentToken.Operator.ADD_ASSIGN -> TODO()
            CrescentToken.Operator.SUB_ASSIGN -> TODO()
            CrescentToken.Operator.MUL_ASSIGN -> TODO()
            CrescentToken.Operator.DIV_ASSIGN -> TODO()
            CrescentToken.Operator.REM_ASSIGN -> TODO()
            CrescentToken.Operator.OR_COMPARE -> TODO()
            CrescentToken.Operator.AND_COMPARE -> TODO()
            CrescentToken.Operator.EQUALS_COMPARE -> TODO()
            CrescentToken.Operator.LESSER_EQUALS_COMPARE -> TODO()
            CrescentToken.Operator.GREATER_EQUALS_COMPARE -> TODO()
            CrescentToken.Operator.EQUALS_REFERENCE_COMPARE -> TODO()
            CrescentToken.Operator.NOT_EQUALS_COMPARE -> TODO()
            CrescentToken.Operator.NOT_EQUALS_REFERENCE_COMPARE -> TODO()
            CrescentToken.Operator.CONTAINS -> TODO()
            CrescentToken.Operator.RANGE -> TODO()
            CrescentToken.Operator.VARIABLE_TYPE_PREFIX -> TODO()
            CrescentToken.Operator.RESULT -> TODO()
            CrescentToken.Operator.COMMA -> TODO()
            else -> TODO()
        }
    }

    private fun numCheck(test1: Any, test2: Any) {
        check(test1 is Number) {
            "Add on non-number! \"${test1::class.java}\""
        }
        check(test2 is Number) {
            "Add on non-number! \"${test2::class.java}\""
        }
        check(test1::class == test2::class) {
            "\"${test1::class.java}\" != \"${test2::class.java}\""
        }
    }

    private fun argument(codeBuilder: CompactCodeAttributeComposer, argument: CrescentAST.Node.Argument) {
        codeList(codeBuilder, argument.value.nodes)
    }

    private fun functionCall(codeBuilder: CompactCodeAttributeComposer, functionCall: CrescentAST.Node.FunctionCall) {
        when (functionCall.name) {
            "println" -> {
                codeBuilder.getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")
                context.stack.push(System.out)
                codeList(codeBuilder, functionCall.arguments)

                codeBuilder.invokevirtual("java/io/PrintStream", "println", "(${genDescriptor(context.stack.pop())})V")
                context.stack.pop()
            }
            else -> TODO(functionCall.name)
        }
    }

    private fun genDescriptor(type: Any): String {
        return when (type) {
            is Double -> {
                return "D"
            }
            is CrescentAST.Node.Type.Unit -> "V"
            is CrescentAST.Node.Type.Basic -> {
                if (type.name == "String") {
                    "Ljava/lang/String;"
                } else {
                    TODO("Type \"${type.name}\"")
                }
            }
            is CrescentAST.Node.Type.Array -> {
                val builder = StringBuilder()
                builder.append("[")
                builder.append(genDescriptor(type.type))
                builder.toString()
            }
            is CrescentAST.Node.String, is String -> {
                "Ljava/lang/String;"
            }
            else -> TODO("Type \"${type::class.java}\"")
        }
    }

    private fun makeVariable(classBuilder: ClassBuilder, variable: CrescentAST.Node.Variable): ProgramField {
        var access = 0
            when (variable.visibility) {
                CrescentAST.Visibility.PUBLIC -> access = access or AccessConstants.PUBLIC
                CrescentAST.Visibility.PRIVATE -> access =
                    access or AccessConstants.PRIVATE or AccessConstants.SYNTHETIC
                CrescentAST.Visibility.INTERNAL -> access =
                    access or AccessConstants.PROTECTED or AccessConstants.SYNTHETIC
                else -> {
                    println("Unknown Modifier: ${variable.visibility}")
                }
            }
        if (access and AccessConstants.PUBLIC == 0 && access and AccessConstants.PRIVATE == 0 && access and AccessConstants.PROTECTED == 0) {
            access = AccessConstants.PUBLIC
        }
        return classBuilder.addAndReturnField(access, variable.name, genDescriptor(variable.type))
    }
}