/*
 * MIT License
 *
 * Copyright (c) 2023 Autobenchmark Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
