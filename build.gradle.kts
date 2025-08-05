plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.morningmeadow"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
    maven("https://m2.dv8tion.net/releases")
    maven("https://maven.topi.wtf/releases")
    maven("https://maven.lavalink.dev/snapshots")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.kord:kord-core:0.15.0")
    implementation("com.sksamuel.hoplite:hoplite-core:3.0.0.RC1")
    implementation("com.sksamuel.hoplite:hoplite-yaml:3.0.0.RC1")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("dev.schlaubi.lavakord:core:9.1.0")
    implementation("dev.schlaubi.lavakord:lavasrc:9.1.0")
    implementation("dev.schlaubi.lavakord:kord:9.1.0")
}

tasks.test {
    useJUnitPlatform()
}