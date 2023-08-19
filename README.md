# Autobenchmark

A Gradle plugin to automate macro-benchmark on baseline profile. Run your macro-benchmark tests for profile verification on Firbase test lab and verify them. 
With this plugin, you can automate macro-benchmark for baseline profile on CI.

## Applying plugin

### Using plugin DSL
Apply plugin to app module `build.gradle.kts` file.
```kotlin
plugins {
  id("io.github.sagar.autobenchmark") version "1.0.0.alpha01"
}
```

### Using legacy plugin application

Add this to top project level `build.gradle.kts`
```kotlin
buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("io.github.sagar.autobenchmark:gradle-plugin:1.0.0.alpha01")
  }
}
```
And then apply plugin to app module `build.gradle.kts` file.

```kotlin
apply(plugin = "io.github.sagar.autobenchmark")
```

## Configuring plugin

Since it is recommended to run macro-benchmark on a physical device this plugin supports running tests on Firebase test lab through [Fladle](https://runningcode.github.io/fladle/). 
Following are the mandatory parameters that you need to configure.

```kotlin
autoBenchmark {
    // A file path to app apk under benchmark
    appApkFilePath.set("/sample/build/outputs/apk/benchmark/sample-benchmark.apk")
    // A file path to benchmark apk
    benchmarkApkFilePath.set("/benchmark/build/outputs/apk/benchmark/benchmark-benchmark.apk")
    // Firebase project ID to access firebase test lab
    firebaseProjectId.set(properties.getProperty("firebaseProjectId"))
    // Physical device configuration map to run benchmark
    physicalDevices.set(mapOf(
        "model" to "redfin", "version" to "30"
    ))
    // Tolerance percentage for improvement below which verification will fail
    tolerancePercentage.set(10f)
}
```
