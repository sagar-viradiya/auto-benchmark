package io.github.sagar.auto_benchmark_plugin

import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

interface AutoBenchmarkExtension {
    val appApkFilePath: Property<String>

    val benchmarkApkFilePath: Property<String>

    val projectId: Property<String>

    val physicalDevices: MapProperty<String, String>

    val tolerancePercentage: Property<Float>

    companion object {
        private const val NAME = "autoBenchmark"

        fun create(target: Project) = target.extensions.create<AutoBenchmarkExtension>(NAME)

        fun get(target: Project) = target.extensions.getByType<AutoBenchmarkExtension>()
    }
}
