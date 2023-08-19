pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "auto-benchmark"
include(":sample")
include(":benchmark")
include(":baseline-profile")
includeBuild("auto-benchmark-plugin")
