package me.camdenorrb.crescentvm.lexer

enum class CrescentDataType {

    // Numbers
    I8,
    I16,
    I32,
    I64,
    //I128,

    NUMBER,

    // Unsigned Numbers
    U8,
    U16,
    U32,
    U64,

    // Other
    CHAR, // U8
    ARRAY,
    TENSOR,
    MUTABLE_LIST,
    LIST,
    MAP,
    MUTABLE_MAP,
    SET,
    SORTEDSET,
    REFERENCE,
    TEXT,
    FUN

}