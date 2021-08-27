package me.camdenorrb.crescentvm.project.extensions


// Make the number into the smallest number type possible
fun Number.minimize(): Number {

	// Maybe add double minimizing in the future
	if (this is Double || this is Float || this is Byte) {
		return this
	}

	return when(this.toLong()) {

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