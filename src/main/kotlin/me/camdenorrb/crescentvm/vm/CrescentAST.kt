package me.camdenorrb.crescentvm.vm

class CrescentAST {

    sealed class Node {

        data class Struct(
            val imports: List<String>,
            val name: String
        )

        data class Function(
            val name: String,
            val visibility: Visibility,
            val params: List<Parameter>
        ) : Node()

        data class Variable(
            val name: String,
            val isFinal: Boolean,
            val visibility: Visibility,
            val value: Node
        ) : Node()

        data class Text(
            val string: String
        ) : Node()

        data class Parameter(
            val value: Node
        ) : Node()

    }

    enum class Visibility {
        PUBLIC,
        INTERNAL,
        PRIVATE
    }
    //data class Tree(left: Node?, right: Node?)

}