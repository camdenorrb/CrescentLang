package dev.twelveoclock.lang.crescent.project

// TODO: Remove in the future and add proper debugging messages
fun checkEquals(expected: Any, actual: Any) {
	check(actual == expected) {
		"Check failed, Expected $expected, got $actual"
	}
}