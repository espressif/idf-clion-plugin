plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.9.0"
}

group = "com.espressif.idf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/snapshots")
}

dependencies{
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.21-2")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    plugins.add("com.intellij.java")
    version.set("2022.2")
    type.set("CL") // Target IDE Platform
//    plugins.set(listOf(/* Plugin Dependencies */))
    plugins.set(listOf("com.intellij.clion", "com.intellij.cidr.base"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
