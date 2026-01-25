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

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class AutoBenchmarkPlugin : Plugin<Project> {
    companion object {
        private const val TASK_NAME = "runBenchmarkAndVerifyProfile"
        private const val BENCHMARK_TASK_NAME = "runBenchmarkOnGcloud"
        private const val PLUGIN_APPLY_ERROR_MESSAGE = "This plugin is only applicable for Android modules"
    }

    override fun apply(project: Project) {
        // Fail if plugin is not applied to android app module
        runCatching {
            project.extensions.getByType(AndroidComponentsExtension::class.java)
        }.getOrElse { error(PLUGIN_APPLY_ERROR_MESSAGE) }

        // Get the extensions
        val extension = AutoBenchmarkExtension.create(project)

        // Register task to verify downloaded benchmark result
        setupMacroBenchmarkVerificationTask(project, extension)
    }

    /**
     * Register a task for benchmark result verification.
     * This task depends on fladle task created above to run tests on firebase test lab and download result.
     *
     * @param project An instance of gradle project
     * @param extension An instance of [AutoBenchmarkExtension]
     */
    private fun setupMacroBenchmarkVerificationTask(project: Project, extension: AutoBenchmarkExtension) {
        val benchmarkTask = project.tasks.register(BENCHMARK_TASK_NAME, RunBenchmarkOnGcloudTask::class.java) {
            appApk.set(project.layout.file(project.provider { File(project.rootDir, extension.appApkFilePath.get()) }))
            benchmarkApk.set(project.layout.file(project.provider { File(project.rootDir, extension.benchmarkApkFilePath.get()) }))

            if (extension.serviceAccountJsonFilePath.isPresent) {
                serviceAccountJson.set(project.layout.file(extension.serviceAccountJsonFilePath.map { File(project.rootDir, it) }))
            }

            physicalDevices.set(extension.physicalDevices)
            benchmarkResultDir.set(project.layout.buildDirectory.dir("benchmark_results"))
        }

        project.tasks.register(
            TASK_NAME,
            BenchmarkJsonParserTask::class.java
        ) {
            // Set the build directory to the output of the benchmark task
            buildDirectory.set(benchmarkTask.flatMap { it.benchmarkResultDir })
            tolerancePercentage.set(extension.tolerancePercentage)
            dependsOn(benchmarkTask)
        }
    }
}
