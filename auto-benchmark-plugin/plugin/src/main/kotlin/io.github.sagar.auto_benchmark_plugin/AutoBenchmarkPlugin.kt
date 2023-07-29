package io.github.sagar.auto_benchmark_plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.osacky.flank.gradle.FlankGradleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

class AutoBenchmarkPlugin : Plugin<Project> {

    private lateinit var flankCredentialsDir: Provider<Directory>
    private lateinit var flankCredentials: Provider<RegularFile>
    private lateinit var rootDir: String
    private lateinit var rootFileTree: FileTree

    override fun apply(target: Project) {
        target.pluginManager.apply("com.osacky.fladle")
        runCatching {
            target.extensions.getByType(AndroidComponentsExtension::class.java)
        }.getOrElse { error("This plugin is only applicable for Android modules") }

        val extension = AutoBenchmarkExtension.create(target)

        target.extensions.configure(FlankGradleExtension::class.java) {
            with(this) {
                defaultClassTestTime.set(30.0)
                defaultTestTime.set(30.0)
                flakyTestAttempts.set(1)
                flankVersion.set("23.06.0")
                localResultsDir.set("${target.buildDir}/reports/flank/")
                performanceMetrics.set(false)
                shardTime.set(150)
                testTimeout.set("6m")
                useOrchestrator.set(true)
                devices.set(
                    listOf(
                        mapOf("model" to "Pixel2", "version" to "33")
                    )
                )
                projectId.set("auto-benchmark-3e0c")
                variant.set("benchmark")
                dependOnAssemble.set(true)
            }

            configs.register("autoBenchmark") {
                apply {
                    filesToDownload.set(listOf(".*/sdcard/screenshots/.*"))
                    debugApk.set(extension.appApkFilePath)
                    instrumentationApk.set(extension.benchmarkApkFilePath)
                }
            }
        }

        target.tasks.register("autoBenchmark") {
            dependsOn("runFlankAutoBenchmark")
        }
    }
}