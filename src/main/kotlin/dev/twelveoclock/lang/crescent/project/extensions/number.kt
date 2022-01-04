package dev.twelveoclock.lang.crescent.project.extensions


// Make the number into the smallest number type possible
fun Number.minimize(): Number {

	when (this) {

		is Byte -> return this

		is Float -> {
			if (this.toInt().toFloat() == this) {
				/* No operation, proceed to next when */
			}
			else {
				return this
			}
		}

		is Double -> {
			if (this.toLong().toDouble() == this) {
				/* No operation, proceed to next when */
			}
			else if (this.toFloat().toDouble() == this) {
				return this.toFloat()
			}
			else {
				return this
			}
		}
	}

	return when (this.toLong()) {

		in Byte.MIN_VALUE..Byte.MAX_VALUE -> {
			this.toByte()
		}

		in Short.MIN_VALUE..Short.MAX_VALUE -> {
			this.toShort()
		}

		in Int.MIN_VALUE..Int.MAX_VALUE -> {
			this.toInt()
		}

		else -> this
	}

}