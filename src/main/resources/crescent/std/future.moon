trait Future {

    fun await(suspend: Suspend)

    fun is_ready -> boolean

}