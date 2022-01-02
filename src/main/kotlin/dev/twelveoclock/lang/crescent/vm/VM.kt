package dev.twelveoclock.lang.crescent.vm

/**
 * This class specifies the modes the VM may be able to do
 */
enum class VM {
    /**
     * INTERPRETED is for running immediately in the jvm instance using the AST and is the default
     * todo
     */
    INTERPRETED,

    /**
     * JVM_BYTECODE transforms the AST into .class files for the Java Virtual Machine
     * In Progress
     */
    JVM_BYTECODE,

    /**
     * BINARY transforms the AST into lumps of native code that can be wrapped with an executable or library wrapper
     * May also generate .class files for the JVM and .h for the C compiler to use and link to the binary
     * todo
     */
    BINARY,

    /**
     * C_CODE transforms the AST into .cpp and .h files to allow for c compilers to compile and other c programs to link to
     * todo determine if wanted
     */
    C_CODE, //possible removal
}