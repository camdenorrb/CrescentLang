import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    application
    kotlin("jvm") version "1.5.30"
}

group = "me.camdenorrb.vm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.capnproto:runtime:0.1.10")
    implementation("com.guardsquare:proguard-core:8.0.1")
    implementation("tech.poder.ir:PoderTechIR:+")
    testImplementation(kotlin("test-junit"))
}

application {
    mainClass.set("me.camdenorrb.crescentvm.Main")
    applicationDefaultJvmArgs = listOf(
        "--add-modules=jdk.incubator.foreign"
    )
}

tasks {
    val javaVersion = JavaVersion.VERSION_16.toString()

    withType<KotlinCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        kotlinOptions.jvmTarget = javaVersion
        //kotlinOptions.languageVersion = "1.6"
        //kotlinOptions.apiVersion = "1.6"
        //kotlinOptions.useFir = true
    }

    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    test {
        jvmArgs("--add-modules=jdk.incubator.foreign")
    }
}