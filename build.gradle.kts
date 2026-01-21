plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = (findProperty("group") as String?) ?: "com.github.Avdiji"
version = (findProperty("version") as String?) ?: "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")

    implementation("org.apache.commons:commons-text:1.14.0")

    implementation("io.github.classgraph:classgraph:4.8.181")
    implementation("org.ow2.asm:asm:9.5")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

java {
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(8)
    }
}


tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("JNIBridge")
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks.jar { enabled = false; }

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = "JNIBridge"
            version = project.version.toString()

            // publish shadow jar as the main artifact
            artifact(tasks.shadowJar.get()) { classifier = null }

            // publish sources/javadoc jars
            artifact(tasks.named("javadocJar"))
        }
    }
}

