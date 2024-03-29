import ::Type

struct Function(
    val name: String
   #val parameters: [Parameter]
)

struct Parameter(
    val name: String
    val type: Type
)

impl Function {

    override fun toString -> String {
        -> "fun $name(${parameters})"
    }

}

impl [Parameter] {

    # This should be the default output for any array
    override fun toString -> String {
        -> self.joinToString(", ")
    }

}