plugins {
    kotlin("jvm") version "1.5.21"
}

group = "me.camdenorrb"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.capnproto:runtime:0.1.8")
    implementation("com.guardsquare:proguard-core:7.1.0")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        sourceCompatibility = JavaVersion.VERSION_16.toString()
        targetCompatibility = JavaVersion.VERSION_16.toString()
        kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
        kotlinOptions.languageVersion = "1.6"
        kotlinOptions.apiVersion = "1.6"
    }

    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_16.toString()
        targetCompatibility = JavaVersion.VERSION_16.toString()
    }
}