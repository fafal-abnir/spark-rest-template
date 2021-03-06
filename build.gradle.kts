/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib", "1.3.11"))
    implementation("com.sparkjava:spark-core:2.7.2")
    implementation("com.mashape.unirest:unirest-java:1.3.1")
    implementation("com.google.code.gson:gson:2.8.2")
    implementation("joda-time:joda-time:2.10.6")
    implementation("com.google.guava:guava:27.0.1-jre")
    // Align versions of all Kotlin components
    implementation(kotlin("stdlib-jdk8", "1.3.11"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.rest-assured:rest-assured:4.0.0")
    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    // Logging
    implementation("org.apache.logging.log4j:log4j-core:2.11.2")
    implementation("io.github.microutils:kotlin-logging:1.6.20")
    implementation("org.slf4j:slf4j-simple:1.7.25")
    // Metrics
    implementation("io.dropwizard.metrics:metrics-core:4.1.0")
    implementation("io.dropwizard.metrics:metrics-healthchecks:4.1.0")
    // Config reader
    implementation("com.typesafe:config:1.3.3")
    implementation(group = "com.uchuhimo", name = "konf", version = "0.20.0")
    implementation(group = "com.uchuhimo", name = "konf-hocon", version = "0.20.0")
    implementation(group = "com.uchuhimo", name = "konf-core", version = "0.20.0")
    // Argument parser
    implementation("com.xenomachina", "kotlin-argparser", "2.0.7")
}

application {
    // Define the main class for the application.
    mainClassName = "coyote.AppKt"
}
