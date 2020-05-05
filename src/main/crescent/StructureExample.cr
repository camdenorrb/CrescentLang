StructureExample(
    val Int aNumber         // New lines makes commas redundant
    val Int aValue1 aValue2 // Multi declaration of same type
) {

    // Utility isn’t binded to Structure instance

    util add(Int value1 value2) Int {
        return value1 + value2
    }

    util sub(Int value1 value2) Int {
        return value1 - value2
    }

    util default() StructureExample {
        return StructureExample(10, 10, 10)
    }

    // Self are binded to StructureExample instance

    self sub(Int value2) Int {
        return self.aNumber - value2
    }

}


StructureExample(
    val Int aNumber
    val Int aValue1, aValue2
) {

    static {

    }

}