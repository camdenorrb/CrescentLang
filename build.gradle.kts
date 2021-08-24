plugins {
    kotlin("jvm") version "1.5.30-RC"
}

group = "me.camdenorrb.vm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.capnproto:runtime:0.1.9")
    implementation("com.guardsquare:proguard-core:8.0.1")
    implementation("tech.poder.ir:PoderTechIR:+")
    testImplementation(kotlin("test-junit"))
}

tasks {
    compileKotlin {
        sourceCompatibility = JavaVersion.VERSION_16.toString()
        targetCompatibility = JavaVersion.VERSION_16.toString()
        kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
        //kotlinOptions.languageVersion = "1.6"
        //kotlinOptions.apiVersion = "1.6"
        //kotlinOptions.useFir = true
    }

    compileJava {
        sourceCompatibility = JavaVersion.VERSION_16.toString()
        targetCompatibility = JavaVersion.VERSION_16.toString()
    }
}