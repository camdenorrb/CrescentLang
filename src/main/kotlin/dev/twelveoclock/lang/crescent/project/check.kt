package dev.twelveoclock.lang.crescent.project

fun checkEquals(expected: Any, actual: Any) {
	check(actual == expected) {
		"Check failed, Expected $expected, got $actual"
	}
}