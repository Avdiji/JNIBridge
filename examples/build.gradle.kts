import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins { java }
repositories { mavenCentral() }

dependencies {
    implementation(project(":"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

// *********************************************************
// ************************* CMAKE *************************
// *********************************************************
fun registerCMakeBuildTask(presetName: String) {
    tasks.register<Exec>("build-${presetName}") {
        group = "jniCMake"

        dependsOn("generateJNICode")

        val buildDir = project.layout.buildDirectory.dir(presetName).get().asFile.absolutePath

        // Chain configure + build in one command
        commandLine("cmd", "/C", "cmake --preset $presetName && cmake --build \"$buildDir\" --target JNIBridgeExamples")
    }
}

var presets = listOf(
    "windows-x64-release",
    "windows-x64-debug",

    "linux-release",
    "linux-debug"
)
presets.forEach { preset ->
    registerCMakeBuildTask(preset)
}
// *********************************************************
// ************************* CMAKE *************************
// *********************************************************

tasks.register<JavaExec>("generateJNICode") {
    group = "jni_generation"

    mainClass.set("com.jnibridge.examples.mappings.MapJNI")

    classpath = sourceSets.main.get().runtimeClasspath
    workingDir = project.file("${project.projectDir}")
}


