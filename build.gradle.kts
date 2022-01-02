import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    application
    kotlin("jvm") version "1.6.10"
}

group = "me.camdenorrb.vm"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.capnproto:runtime:0.1.11")
    implementation("com.guardsquare:proguard-core:8.0.3")
    //implementation("tech.poder.ir:PoderTechIR:+")
    testImplementation(kotlin("test-junit5"))
}

application {
    mainClass.set("me.camdenorrb.crescentvm.Main")
    applicationDefaultJvmArgs = listOf(
        "--add-modules=jdk.incubator.foreign"
    )
}

tasks {

    val javaVersion = JavaVersion.VERSION_17.toString()

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
        useJUnitPlatform()
        jvmArgs("--add-modules=jdk.incubator.foreign")
    }
}