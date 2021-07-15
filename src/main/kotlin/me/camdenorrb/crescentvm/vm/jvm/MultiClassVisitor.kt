package me.camdenorrb.crescentvm.vm.jvm

import proguard.classfile.Clazz
import proguard.classfile.visitor.ClassVisitor

class MultiClassVisitor(private vararg val classVisitor: ClassVisitor) : ClassVisitor {
    override fun visitAnyClass(clazz: Clazz) {
        classVisitor.forEach {
            clazz.accept(it)
        }
    }
}