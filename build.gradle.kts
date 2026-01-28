plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "com.nickname"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly(files("libs/LuckPerms-Hytale-5.5.26.jar"))
    compileOnly(files("libs/tinymessage-2.0.0.jar"))
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        relocate("com.google.gson", "com.nickname.libs.gson")
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
