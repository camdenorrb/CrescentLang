package me.camdenorrb.crescentvm.vm

// https://github.com/cretz/kastree/blob/master/ast/ast-common/src/main/kotlin/kastree/ast/Node.kt
class CrescentAST {

    enum class Visibility {
        LOCAL_SCOPE,
        PRIVATE,
        INTERNAL,
        PUBLIC
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

        data class Return(
            val expression: Expression
        ) : Node()

        data class Struct(
            val name: kotlin.String,
            val variables: List<Variable>
        ) : Node()

        data class Trait(
            val name: kotlin.String,
            val functionTraits: List<FunctionTrait>
        ) : Node()

        data class Object(
            val name: kotlin.String,
            val functions: List<Function>
        ) : Node()

        data class Impl(
            val type: Type,
            val functions: List<Function>
        ) : Node()

        data class Enum(
            val name: kotlin.String,
            val variables: List<Variable>,
            val structs: List<Struct>
        )

        data class FunctionTrait(
            val name: kotlin.String,
            val params: List<Parameter>,
            val returnType: Type
        ) : Node()

        data class FunctionCall(
            val name: kotlin.String,
            val arguments: List<Argument>
        ) : Node()

        data class Variable(
            val name: kotlin.String,
            val isFinal: Boolean,
            val visibility: Visibility,
            val type: Type,
            val value: Node,
        ) : Node()

        data class Function(
            val name: kotlin.String,
            val modifiers: List<CrescentToken.Modifier>,
            val visibility: Visibility,
            val params: List<Parameter>,
            val returnType: Type,
            val innerCode: Expression,
        ) : Node()

        // TODO: Make a better toString
        // TODO: Support project level functions
        data class File(
            val name: kotlin.String,
            val path: kotlin.String,
            val imports: List<kotlin.String>,
            val structs: List<Struct>,
            val impls: List<Impl>,
            val traits: List<Trait>,
            val objects: List<Object>,
            val enums: List<Enum>,
            val mainFunction: Function?
        ) : Node()


        sealed class Parameter {

            abstract val name: kotlin.String


            data class Basic(
                override val name: kotlin.String,
                val type: Type
            ) : Parameter()

            data class WithDefault(
                override val name: kotlin.String,
                val defaultValue: Expression
            ) : Parameter()

        }

        // TODO: Add toStrings
        sealed class Type : Node() {

            // Should only be used for variables
            object Implicit : Type()

            // No return type
            object Unit : Type()


            // Should only be used for function return types
            data class Result(val type: Type) : Type()

            data class Basic(val name: kotlin.String) : Type()

            data class Array(val type: Type) : Type()


            data class Generic(
                val type: Basic,
                val parameters: List<Type>
            ) : Type()

        }

        sealed class Statement {

            data class When(
                val predicateToBlock: List<Pair<Expression, Expression>>
            ) : Statement()

            data class Else(
                val block: Expression
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