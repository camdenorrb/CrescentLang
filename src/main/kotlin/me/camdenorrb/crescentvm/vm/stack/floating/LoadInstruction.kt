package me.camdenorrb.crescentvm.vm.stack.floating

data class LoadInstruction(val moveToStack: () -> Unit)