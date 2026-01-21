plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

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

if (project.group.toString().isBlank()) {
    group = "com.github.Avdiji"
}
if (project.version.toString() == "unspecified") {
    version = "0.0.0-SNAPSHOT"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = "JNIBridge"
            version = project.version.toString()

            artifact(tasks.shadowJar.get()) { classifier = null }
            artifact(tasks.named("javadocJar"))
        }
    }
}

