plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"

}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0");

    implementation("org.apache.commons:commons-text:1.14.0")

    implementation("org.reflections:reflections:0.9.12")
    implementation("org.javassist:javassist:3.29.2-GA")



    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

java {
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("JNIBridge")
    archiveVersion.set("")
    archiveClassifier.set("")
}
