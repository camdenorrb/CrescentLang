StructureExample(
    val Int aNumber              // New lines makes commas redundant
    val String aValue1 aValue2   // Multi declaration of same type
) {

    // _ represents the same as default constructor
    // Then we can use that in order to fill in the other params
    constructor(_, “$aNumber”, “$aNumber”)

    init {
        //do things
    }

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

    // Public by default
    sub(Int value2) Int {
        return self.aNumber - value2
    }

    prot doThing1() {
    }

    // () redundant
    priv doThing2 {

    }

}
