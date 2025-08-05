plugins {
    kotlin("jvm") version "2.2.0"
}

group = "com.brokenfdreams"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // core
    implementation("se.michaelthelin.spotify:spotify-web-api-java:9.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

tasks.register<JavaExec>("runAuth") {
    group = "application"
    description = "Run the GetRefreshToken script to obtain a refresh token"
    mainClass.set("com/brokenfdreams/spotifyscripts/GetRefreshTokenKt")
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("runNewReleases") {
    group = "application"
    description = "Run the SpotifyNewReleases script to create a weekly playlist"
    mainClass.set("com/brokenfdreams/spotifyscripts/CustomReleaseRadarKt")
    classpath = sourceSets.main.get().runtimeClasspath
}

kotlin {
    jvmToolchain(21)
}