plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.morningmeadow"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.kord:kord-core:0.15.0")
    implementation("com.sksamuel.hoplite:hoplite-core:3.0.0.RC1")
    implementation("com.sksamuel.hoplite:hoplite-yaml:3.0.0.RC1")
    implementation("org.slf4j:slf4j-simple:2.0.17")
}

tasks.test {
    useJUnitPlatform()
}