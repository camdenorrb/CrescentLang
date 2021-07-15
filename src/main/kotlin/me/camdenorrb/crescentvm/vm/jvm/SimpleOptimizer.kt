package me.camdenorrb.crescentvm.vm.jvm

import proguard.classfile.ClassConstants
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.editor.*
import proguard.classfile.editor.InstructionSequenceReplacer.INT_A_STRING
import proguard.classfile.util.BranchTargetFinder
import proguard.classfile.visitor.AllMethodVisitor


object SimpleOptimizer {
    private const val STRING_BUFFER: String = ClassConstants.NAME_JAVA_LANG_STRING_BUFFER
    private const val X = InstructionSequenceReplacer.X
    private const val A = InstructionSequenceReplacer.A


    val replacer by lazy {
        val builder = InstructionSequenceBuilder()

        val replacements = arrayOf(
            arrayOf( // ... * -1 = -...
                builder.ldc(-1)
                    .imul().`__`(),
                builder.ineg().`__`()
            ), arrayOf( // ... * -1 = -...
                builder.ldc(-1f)
                    .fmul().`__`(),
                builder.fneg().`__`()
            ), arrayOf( // ... * -1 = -...
                builder.ldc2_w(-1.0)
                    .dmul().`__`(),
                builder.dneg().`__`()
            ), arrayOf( // ... * -1 = -...
                builder.ldc2_w(-1L)
                    .lmul().`__`(),
                builder.lneg().`__`()
            ), arrayOf( // ... + 0 = ...
                builder.iconst_0()
                    .iadd().`__`()
            ), arrayOf( // ... + 0 = ...
                builder.dconst_0()
                    .dadd().`__`()
            ), arrayOf( // ... + 0 = ...
                builder.fconst_0()
                    .fadd().`__`()
            ), arrayOf( // ... + 0 = ...
                builder.lconst_0()
                    .ladd().`__`()
            ), arrayOf( // i=i = nothing
                builder.iload(X)
                    .istore(X).`__`()
            ), arrayOf( // i=i = nothing
                builder.lload(X)
                    .lstore(X).`__`()
            ), arrayOf( // i=i = nothing
                builder.dload(X)
                    .dstore(X).`__`()
            ), arrayOf( // i=i = nothing
                builder.fload(X)
                    .fstore(X).`__`()
            ), arrayOf( // putstatic/getstatic = dup/putstatic
                builder.putstatic(X)
                    .getstatic(X).`__`(),
                builder.dup()
                    .putstatic(X).`__`()
            ), arrayOf( // new StringBuffer().append(I) = new StringBuffer("....")
                builder.invokespecial(STRING_BUFFER, "<init>", "()V")
                    .iconst(A)
                    .invokevirtual(STRING_BUFFER, "append", "(I)Ljava/lang/StringBuffer;").`__`(),
                builder.ldc_(INT_A_STRING)
                    .invokespecial(STRING_BUFFER, "<init>", "(Ljava/lang/String;)V").`__`()
            )
        )
        val constants = builder.constants()
        val branchTargetFinder = BranchTargetFinder()
        val codeAttributeEditor = CodeAttributeEditor()
        AllMethodVisitor(
            AllAttributeVisitor(
                PeepholeEditor(
                    branchTargetFinder, codeAttributeEditor,
                    InstructionSequencesReplacer(
                        constants,
                        replacements,
                        branchTargetFinder,
                        codeAttributeEditor
                    )
                )
            )
        )
    }
}