package me.camdenorrb.crescentvm.math

import me.camdenorrb.crescentvm.iterator.PeekingNodeIterator
import me.camdenorrb.crescentvm.vm.CrescentAST
import me.camdenorrb.crescentvm.vm.CrescentToken
import java.util.*

object ShuntingYard {

    @JvmStatic
    fun main(args: Array<String>) {
    }

    fun invoke(input: List<CrescentAST.Node>): List<CrescentAST.Node> {

        // Queue -> Add/Remove
        // Stack -> Push/Pop

        val outputQueue   = LinkedList<CrescentAST.Node>()
        val operatorStack = LinkedList<CrescentAST.Node.Operator>()

        val nodeIterator = PeekingNodeIterator(input)

        while (nodeIterator.hasNext()) {
            when (val next = nodeIterator.next()) {

                is CrescentAST.Node.Primitive.Number,
                is CrescentAST.Node.Identifier,
                -> {

                    if (nodeIterator.peekBack() is CrescentAST.Node.Expression && next is Number) {
                        operatorStack.push(CrescentAST.Node.Operator(CrescentToken.Operator.MUL))
                    }

                    outputQueue.add(next)

                    if (nodeIterator.hasNext() && nodeIterator.peekNext() is CrescentAST.Node.Expression && next is Number) {
                        operatorStack.push(CrescentAST.Node.Operator(CrescentToken.Operator.MUL))
                    }
                }

                is CrescentAST.Node.Expression -> {
                    outputQueue.addAll(invoke(next.nodes))
                }

                is CrescentAST.Node.Operator -> {

                    while (operatorStack.isNotEmpty() && precedence(operatorStack.first) >= precedence(next)) {
                        outputQueue.add(operatorStack.pop())
                    }

                    operatorStack.push(next)
                }

                else -> outputQueue.add(next)
            }
        }

        while (operatorStack.isNotEmpty()) {
            outputQueue.add(operatorStack.pop())
        }

        return outputQueue
    }

    fun precedence(operator: CrescentAST.Node.Operator) = when (operator.operator) {

        CrescentToken.Operator.ADD -> 2

        CrescentToken.Operator.MUL,
        CrescentToken.Operator.DIV,
        CrescentToken.Operator.REM,
        -> 3

        CrescentToken.Operator.POW -> 4

        else -> 0
    }

}