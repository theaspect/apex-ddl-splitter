plugins {
    application
    kotlin("jvm") version "2.1.10"
    kotlin("kapt") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.graalvm.buildtools.native") version "0.10.5"
}

group = "me.blzr"
version = "1.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.alllex.parsus:parsus-jvm:0.6.1")

    implementation("info.picocli:picocli:4.7.6")
    kapt("info.picocli:picocli-codegen:4.7.6")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")

}

application {
    mainClass.set("me.blzr.apex.Main")
}

tasks.test {
    useJUnitPlatform()
}


kotlin {
    jvmToolchain(17)
}

//kapt {
//    arguments {
//        arg("project", "${project.group}/${project.name}")
//    }
//}
//
//task<JavaExec>("generateGraalResourceConfig") {
//    dependsOn("classes")
//    mainClass = "picocli.codegen.aot.graalvm.ResourceConfigGenerator"
//    classpath = /*configurations.get("generateConfig") +*/ sourceSets["main"].runtimeClasspath
//    val outputFile =
//        "${layout.buildDirectory}/resources/main/META-INF/native-image/${project.group}/${project.name}/resource-config.json"
//    args = listOf("--output=$outputFile", "me.blzr.apex.Main")
//}
//
//tasks["assemble"].dependsOn("generateGraalResourceConfig")

// https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html#configure-native-image
// Install visual studio https://www.graalvm.org/latest/getting-started/windows/
// Run
// set JAVA_HOME=%USERPROFILE%\.jdks\graalvm-jdk-23.0.2
// call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat" > nul
// gradlew nativeCompile
// On linux run java -jar -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image build/libs/xlsx2json-1.0-SNAPSHOT-all.jar src/test/resources/input.xlsx out.json
graalvmNative {
    // toolchainDetection.set(true)
    binaries {
        named("main") {
            useFatJar.set(true)
            buildArgs.add("-H:-CheckToolchain")
            // CP1252 is missing
            // https://github.com/apache/poi/blob/trunk/poi/src/main/java/org/apache/poi/poifs/filesystem/FileMagic.java#L133
            buildArgs.add("-H:+AddAllCharsets")
            configurationFileDirectories.from(file("src/main/resources/META-INF/native-image/"))
        }
    }
}
