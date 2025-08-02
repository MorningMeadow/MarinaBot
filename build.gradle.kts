plugins {
    kotlin("jvm") version "2.0.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.kord:kord-core:0.15.0")
    implementation("com.sksamuel.hoplite:hoplite-core:3.0.0.RC1")
    implementation("com.sksamuel.hoplite:hoplite-yaml:3.0.0.RC1")
}

tasks.test {
    useJUnitPlatform()
}