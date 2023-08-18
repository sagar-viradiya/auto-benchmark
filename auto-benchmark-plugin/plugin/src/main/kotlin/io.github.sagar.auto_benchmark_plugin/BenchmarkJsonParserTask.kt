package io.github.sagar.auto_benchmark_plugin

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.math.BigDecimal

abstract class BenchmarkJsonParserTask : DefaultTask() {

    @get: InputDirectory
    abstract val buildDirectory: DirectoryProperty

    @get: Input
    abstract val tolerancePercentage: Property<Float>

    @TaskAction
    @Suppress("UNCHECKED_CAST")
    fun execute() {
        val jsonFile = buildDirectory.dir("fladle/benchmark_result").get().asFileTree.filter { file ->
            file.name.contains("benchmarkData")
        }.singleFile
        val json: Map<String, Any> = JsonSlurper().parse(jsonFile) as Map<String, Any>
        val benchmarkResult: List<Map<String, Any>> = json["benchmarks"] as List<Map<String, Any>>
        var noCompilationMedian = BigDecimal(0)
        var baselineProfileMedian = BigDecimal(0)
        benchmarkResult.forEach { benchmark ->
            if (benchmark["name"].toString().contains("NoCompilation", true)) {
                noCompilationMedian =
                    ((benchmark["metrics"] as Map<String, Any>)["timeToInitialDisplayMs"] as Map<String, Any>)["median"] as BigDecimal
            }

            if (benchmark["name"].toString().contains("BaselineProfile", true)) {
                baselineProfileMedian =
                    ((benchmark["metrics"] as Map<String, Any>)["timeToInitialDisplayMs"] as Map<String, Any>)["median"] as BigDecimal
            }
        }

        if (noCompilationMedian <= baselineProfileMedian) {
            throw GradleException("Negative improvements : No compilation mode is better than baseline profile based compilation")
        }

        println("No compilation median : $noCompilationMedian")
        println("Baseline profile median : $baselineProfileMedian")
        val improvement = ((noCompilationMedian - baselineProfileMedian) / noCompilationMedian) * BigDecimal(100)
        println("Improvement percentage : $improvement")
        if (improvement < BigDecimal(tolerancePercentage.get().toDouble())) {
            throw GradleException("Improvement is below tolerance percentage, time to check baseline profile")
        }
    }
}
