plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("com.gradle.plugin-publish")
}

group = "io.github.sagar"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
