plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "com.uadb"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

javafx {
    version = "21.0.1"
    modules = listOf("javafx.controls")
}

application {
    mainClass.set("app.Main")
}

sourceSets {
    named("main") {
        java.srcDirs("app", "core", "persistence", "scripts", "updater")
        resources.srcDirs("resources")
    }
    named("test") {
        java.srcDirs("test")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Basic run configuration reminder
println("Build script loaded: JavaFX version=" + (project.extensions.findByName("javafx") != null))