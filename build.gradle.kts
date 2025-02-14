import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream

plugins {
    java
    id("maven-publish")
    id("com.github.johnrengelman.shadow").version("6.0.0")
}

repositories {
    flatDir { dirs("lib") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("http://maven.enginehub.org/repo/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://repo.aikar.co/content/groups/aikar/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/central/") }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

group = "org.polydev.gaea"
val versionObj = Version("1", "15", "0", false)
version = versionObj

dependencies {
    compileOnly("org.jetbrains:annotations:20.1.0") // more recent.
    compileOnly("org.spigotmc:spigot:1.19-R0.1-SNAPSHOT")
    implementation("com.googlecode.json-simple:json-simple:1.1")
    implementation("commons-io:commons-io:2.4")
    implementation("org.apache.commons:commons-rng-core:1.3")
    implementation("net.jafama:jafama:2.3.2")
    implementation("co.aikar:taskchain-bukkit:3.7.2")
    implementation("com.esotericsoftware:reflectasm:1.11.9")
    implementation("org.bstats:bstats-bukkit:3.0.0")

    // JUnit.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")

    testImplementation("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()

    maxHeapSize = "1G"
    ignoreFailures = false
    failFast = true
    maxParallelForks = 12
}


tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    archiveBaseName.set("Gaea")
    setVersion(project.version)
    relocate("org.apache.commons", "org.polydev.gaea.libs.commons")
    relocate("org.bstats.bukkit", "org.polydev.gaea.libs.bstats")
    relocate("co.aikar.taskchain", "org.polydev.gaea.libs.taskchain")
    relocate("com.esotericsoftware", "org.polydev.gaea.libs.reflectasm")
    relocate("net.jafama", "org.polydev.gaea.libs.jafama")
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["sourcesJar"])
            artifact(tasks["jar"])
        }
    }

    repositories {
        val mavenUrl = "https://repo.codemc.io/repository/maven-releases/"
        val mavenSnapshotUrl = "https://repo.codemc.io/repository/maven-snapshots/"

        maven((if (versionObj.preRelease) mavenSnapshotUrl else mavenUrl)) {
            val mavenUsername: String? by project
            val mavenPassword: String? by project
            if (mavenUsername != null && mavenPassword != null) {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
}

/**
 * Version class that does version stuff.
 */
class Version(val major: String, val minor: String, val revision: String, val preRelease: Boolean = false) {

    override fun toString(): String {
        return if (preRelease)
            "$major.$minor.$revision-BETA+${getGitHash()}"
        else //Only use git hash if it's a prerelease.
            "$major.$minor.$revision"
    }
}

fun getGitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = mutableListOf("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}
