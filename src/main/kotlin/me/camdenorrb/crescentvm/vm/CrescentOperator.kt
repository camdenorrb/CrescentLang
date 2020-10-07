package me.camdenorrb.crescentvm.vm

enum class CrescentOperator(val literal: String) {
    NOT("!"),
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    REM("%"),
    ADD_ASSIGN("+="),
    SUB_ASSIGN("-="),
    MUL_ASSIGN("*="),
    DIV_ASSIGN("/="),
    REM_ASSIGN("%="),
    OR_COMPARE("||"),
    AND_COMPARE("&&"),
    EQUALS_COMPARE("=="),
    EQUALS_REFERENCE_COMPARE("==="),
    NOT_EQUALS_COMPARE("!="),
    NOT_EQUALS_REFERENCE_COMPARE("!=="),
    CONTAINS("in"),
    RANGE("..")
}