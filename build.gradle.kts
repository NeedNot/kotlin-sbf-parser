plugins {
    kotlin("jvm") version "1.9.10" // Use the appropriate Kotlin version
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation("com.fazecast:jSerialComm:2.11.0")
}

java {
    withSourcesJar() // Include source files in the JAR
    withJavadocJar() // Include Javadoc in the JAR
}

tasks.jar {
    manifest {
        attributes["Implementation-Title"] = "kotlin-sbf-parser"
        attributes["Implementation-Version"] = "1.0.0"
        attributes["Built-By"] = "Need_Not"
    }
}

tasks.test {
    useJUnitPlatform()
}