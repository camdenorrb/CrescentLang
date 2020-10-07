struct Function {
    val name: String
    val parameters: [Parameter]
}

struct Parameter {
    val name: String
    val type: Type
}


impl Function {

    override fun to_string -> String {
        -> "fun $name(${parameters})"
    }

}

impl [Parameter] {

    override fun to_string -> String {
        -> "(${self.mapToString(", ")})"
    }

}