
# This should be a special see through type
# TODO: Figure out how I want to do delegates, without making it VM side
# Maybe a Delegate trait?
struct Lazy<T> {
   val value: T?
}