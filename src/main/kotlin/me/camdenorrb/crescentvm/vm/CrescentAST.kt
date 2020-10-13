package me.camdenorrb.crescentvm.vm

// https://github.com/cretz/kastree/blob/master/ast/ast-common/src/main/kotlin/kastree/ast/Node.kt
class CrescentAST {

    enum class Visibility {
        PUBLIC,
        LOCAL_SCOPED,
        INTERNAL,
        PRIVATE
    }

    sealed class Node {

        data class String(
            val data: kotlin.String
        ) : Node()

        data class Argument(
            val value: Expression
        ) : Node()

        data class Expression(
            val nodes: List<Node>
        ) : Node()

        data class Parameter(
            val name: String,
            val type: String // Maybe make a Type node?
        ) : Node()

        data class Struct(
            val name: String,
            val variables: List<Variable>
        ) : Node()

        data class Trait(
            val name: String,
            val functionTraits: List<FunctionTrait>
        ) : Node()

        data class Impl(
            val structName: String,
            val functions: List<Function>
        ) : Node()

        data class File(
            val name: String,
            val path: String,
            val imports: List<String>,
            val structs: List<Struct>,
            val traits: List<Trait>,
            val mainFunction: Function?
        ) : Node()

        data class FunctionTrait(
            val name: String,
            val params: List<Parameter>,
        ) : Node()

        data class Function(
            val name: String,
            val isOverride: Boolean,
            val visibility: Visibility,
            val params: List<Parameter>,
            val innerCode: Expression
        ) : Node()

        data class FunctionCall(
            val name: String,
            val arguments: List<Argument>
        ) : Node()

        data class Variable(
            val name: String,
            val isFinal: Boolean,
            val visibility: Visibility,
            val value: Node
        ) : Node()


        sealed class Statement {

            data class Else(
                val block: Expression
            )

            data class When(
                val predicateToBlock: List<Pair<Expression, Expression>>
            ) : Statement()

            data class If(
                val predicate: Expression,
                val block: Expression,
            ) : Statement()

            data class While(
                val predicate: Expression,
                val block: Expression
            ) : Statement()

            data class For(
                val variable: Variable,
                val predicate: Expression,
                val block: Expression
            ) : Statement()

        }

    }

}