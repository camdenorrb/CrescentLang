trait Streamable<T> {

    inline fun for_each(on_each: (T))

    inline fun <R> map(on_each: (T) -> R)

}

# Impls for traits can either contain default methods or new methods based on ones from the trait
impl Streamable {

    fun map_to_string(separator: String = "") -> String {
        map {  }
    }

}