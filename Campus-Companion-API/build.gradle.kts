import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    application
}

group = "com.campuscompanion"
version = "1.0.0"

repositories {
    mavenCentral()
}

val ktorVersion = "3.5.2"
val firebaseAdminVersion = "9.10.0"
val logbackVersion = "1.5.16"

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")

    // Firebase Admin SDK (Firestore + Auth token verification)
    implementation("com.google.firebase:firebase-admin:$firebaseAdminVersion")

    // Ktor HTTP client, used server-side to call the OpenWeather API for weather recommendations
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.campuscompanion.api.ApplicationKt")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.test {
    useJUnitPlatform()
}

// Convenience task: run the Firestore seed script (data.SeedDataKt main function)
tasks.register<JavaExec>("seed") {
    group = "application"
    description = "Seeds Firestore with sample campus locations and events/opportunities."
    dependsOn("classes")
    mainClass.set("com.campuscompanion.api.data.SeedDataKt")
    classpath = sourceSets["main"].runtimeClasspath
}
