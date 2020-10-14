package me.camdenorrb.crescentvm.project

fun checkEquals(value1: Any, value2: Any) {
    check(value1 == value2) {
        "Check failed, Expected $value2, got $value1"
    }
}