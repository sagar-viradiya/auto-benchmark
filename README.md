# Autobenchmark

A Gradle plugin to automate macro-benchmark on baseline profile. 
Run your macro-benchmark tests for profile verification on Firbase test lab and verify benchmark result JSON.
With this plugin it is possible to integrate baseline profile verification in your CI pipeline to fully automate baseline profile. 

For more information on benchmarking baseline profile visit official [Android guide](https://developer.android.com/topic/performance/baselineprofiles/measure-baselineprofile).

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

## ⚙️ Configuring plugin

Since it is recommended to run macro-benchmark on a physical device this plugin supports running tests on Firebase test lab through [Fladle](https://runningcode.github.io/fladle/). 
Following are the mandatory parameters that you need to configure.

```kotlin
autoBenchmark {
    // A file path to app apk with baseline profile
    appApkFilePath.set("/sample/build/outputs/apk/benchmark/sample-benchmark.apk")
    // A file path to benchmark apk
    benchmarkApkFilePath.set("/benchmark/build/outputs/apk/benchmark/benchmark-benchmark.apk")
    // Firebase project ID to access firebase test lab
    firebaseProjectId.set("firebaseProjectId")
    // Physical device configuration map to run benchmark
    physicalDevices.set(mapOf(
        "model" to "redfin", "version" to "30"
    ))
    // Tolerance percentage for improvement below which verification will fail
    tolerancePercentage.set(10f)
}
```

### 🔐 Authenticate G-Cloud to run tests on Firebase test lab

To run tests on Firebase test lab you need to authenticate to G-Cloud.

#### Authenticating on local machine

1. Run `./gradlew flankAuth`
2. Sign in to web browser.

This will store credentials in ~/.flank directory

#### Authenticating on CI

You will need service account JSON file to setup authentication on CI. Follow the [test lab docs](https://firebase.google.com/docs/test-lab/android/continuous) to create a service account.
Base64 encode this file on CI as GCLOUD_KEY using shell script. 

```shell
base64 -i "$HOME/.config/gcloud/application_default_credentials.json" | pbcopy
```

Then in CI decode the JSON.

```shell
GCLOUD_DIR="$HOME/.config/gcloud/"
mkdir -p "$GCLOUD_DIR"
echo "$GCLOUD_KEY" | base64 --decode > "$GCLOUD_DIR/application_default_credentials.json"
```

For more info refer [this](https://flank.github.io/flank/#authenticate-with-a-service-account) flank guide for authentication as this plugin internally uses Fladle and Flank

## 📈 Running benchmark tests and baseline profile verification

To run benchmark and verify result run following gradle task.

```shell
./gradlew [your_app]:runBenchmarkAndVerifyProfile
```

First, this will run all benchmark tests on Firebase test lab, and it will download benchmark result JSON from G-Cloud.
This JSON file will be analysed next to compare 'No compilation median startup time' with 'baseline profile median startup time'.
If the improvement percentage is below provided `tolerancePercentage` then gradle task will fail.

For example, if no compilation median startup is 233 ms and baseline profile median startup
is 206 ms then startup time is improved by ~11%. If the provided tolerance percentage is 
10 then task will successfully complete. 

Task will also print improvement percentage, no compilation startup time median, and baseline profile startup time median.

```shell
> Task :sample:runBenchmarkAndVerifyProfile
No compilation median : 233.609398
Baseline profile median : 206.635229
Improvement percentage : 11.546700

BUILD SUCCESSFUL in 2m 35s
9 actionable tasks: 3 executed, 6 up-to-date
```

> Note : One important requirement for benchmark result JSON parsing to work properly 
> is to have 'NoCompilation' in the test name targeting no compilation mode, and 'BaselineProfile' in the test name targeting baseline profile mode.

## Sample
Please refer to sample app to see the plugin setup. Before you issue `./gradlew :sample:runBenchmarkAndVerifyProfile` you need to, 
build sample apk and benchmark apk first.

Run following commands to build apks

```shell
./gradlew :sample:assembleBenchmark
```

```shell
./gradlew :benchmark:assembleBenchmark
```

You will need your firebase test lab project ID in local.properties file. Also, you need to authenticate 
G-cloud to run tests on Firebase test lab.

Sample CI setup using github action is coming soon!

## Contribution
Unfortunately it is not ready to accept any contribution
yet. Once this is stable enough contribution guidelines will be updated here. Meanwhile, feel free to open an [issue](https://github.com/sagar-viradiya/auto-benchmark/issues) for feature requests and improvements.

## License

    MIT License
    
    Copyright (c) 2023 Autobenchmark Contributors
    
    Permission is hereby granted, free of charge, to any person obtaining a copy 
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.