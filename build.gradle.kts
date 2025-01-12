plugins {
    kotlin("jvm") version "1.9.10"
    id("maven-publish")
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

val projectVersion = "1.1.0"

tasks.jar {
    manifest {
        attributes["Implementation-Title"] = "kotlin-sbf-parser"
        attributes["Implementation-Version"] = projectVersion
        attributes["Built-By"] = "Need_Not"
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.neednot"
            artifactId = "sbfparser"
            version = projectVersion

            from(components["java"])
        }
    }
}