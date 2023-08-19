plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
    id("com.gradle.plugin-publish") version "1.1.0" apply false
}

allprojects {
    group = property("GROUP").toString()
    version = property("VERSION").toString()
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
