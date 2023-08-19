package io.github.sagar.auto_benchmark_plugin

import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

interface AutoBenchmarkExtension {

    /**
     * A file path to app apk under benchmark
     */
    val appApkFilePath: Property<String>

    /**
     * A file path to benchmark apk
     */
    val benchmarkApkFilePath: Property<String>

    /**
     * Firebase project ID to access firebase test lab
     */
    val firebaseProjectId: Property<String>

    /**
     * Physical device configuration map to run benchmark
     */
    val physicalDevices: MapProperty<String, String>

    /**
     * Tolerance percentage for improvement below which verification will fail
     */
    val tolerancePercentage: Property<Float>

    companion object {
        private const val NAME = "autoBenchmark"

        /**
         * Creates a extension of type [AutoBenchmarkExtension] and returns
         */
        fun create(target: Project) = target.extensions.create<AutoBenchmarkExtension>(NAME)
    }
}
