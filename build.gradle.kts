plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.dokka") version "2.0.0"
    `maven-publish`
}

group = "com.ejrp.midi"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
}

tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("MidiParser") {
            artifactId = name
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("MidiParser")
                description.set("The parsing and validating MIDI files.")
                url.set("https://github.com/EtiennePinard/MidiParser")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("ejrp")
                        name.set("Etienne Pinard")
                        email.set("Etienne.Pinard@USherbrooke.ca")
                    }
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
