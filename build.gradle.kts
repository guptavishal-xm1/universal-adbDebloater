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

// jpackage task for creating portable app (no installer needed)
// Works on Windows, macOS, and Linux
tasks.register<Exec>("jpackage") {
    dependsOn("build")
    
    val appName = "UniversalADBDebloater"
    val appVersion = project.version.toString().replace("-SNAPSHOT", "")
    val mainClass = application.mainClass.get()
    val modulePath = configurations.runtimeClasspath.get().asPath
    
    doFirst {
        delete("$buildDir/jpackage")
    }
    
    val osName = System.getProperty("os.name").lowercase()
    val isMac = osName.contains("mac")
    val isWindows = osName.contains("win")
    val isLinux = osName.contains("nux")
    
    val baseArgs = mutableListOf(
        "jpackage",
        "--type", "app-image",             // Portable app (no installer)
        "--name", appName,
        "--app-version", appVersion,
        "--vendor", "Universal ADB Debloater Contributors",
        "--description", "Safely debloat Android devices using ADB",
        "--dest", "$buildDir/distributions",
        "--input", "$buildDir/libs",
        "--main-jar", tasks.jar.get().archiveFileName.get(),
        "--main-class", mainClass,
        "--module-path", modulePath,
        "--add-modules", "javafx.controls",
        "--java-options", "-Xmx512m"
    )
    
    // Add platform-specific options
    when {
        isMac -> {
            // macOS creates a .app bundle
            val iconFile = file("$projectDir/resources/icon.icns")
            if (iconFile.exists()) {
                baseArgs.addAll(listOf("--icon", iconFile.absolutePath))
            }
        }
        isWindows -> {
            // Windows creates .exe
            val iconFile = file("$projectDir/resources/icon.ico")
            if (iconFile.exists()) {
                baseArgs.addAll(listOf("--icon", iconFile.absolutePath))
            }
        }
        isLinux -> {
            // Linux creates executable
            val iconFile = file("$projectDir/resources/icon.png")
            if (iconFile.exists()) {
                baseArgs.addAll(listOf("--icon", iconFile.absolutePath))
            }
        }
    }
    
    commandLine(baseArgs)
    
}

// Basic run configuration reminder
println("Build script loaded: JavaFX version=" + (project.extensions.findByName("javafx") != null))