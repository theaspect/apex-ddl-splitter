plugins {
    application
    kotlin("jvm") version "2.1.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.graalvm.buildtools.native") version "0.10.5"
}

group = "me.blzr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.alllex.parsus:parsus-jvm:0.6.1")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("me.blzr.apex.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
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
