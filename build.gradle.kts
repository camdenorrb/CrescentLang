plugins {
    kotlin("jvm") version "1.5.21"
}

group = "me.camdenorrb"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.21")
    implementation("org.capnproto:runtime:0.1.9")
    implementation("com.guardsquare:proguard-core:8.0.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.5.21")
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