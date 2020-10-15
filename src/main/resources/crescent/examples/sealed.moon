# All structs inside automatically inherit type Example
sealed Example {
    struct Thing1(val name: String)
    struct Thing2(val id: i32)
}