package io.github.sagar.auto_benchmark_plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.osacky.flank.gradle.FlankGradleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoBenchmarkPlugin : Plugin<Project> {
    companion object {
        private const val TASK_NAME = "runBenchmarkAndVerifyProfile"
        private const val FLADLE_CONFIG_NAME = "autoBenchmark"
        private const val LOCAL_RESULT_DIR = "benchmark_result"
        private const val FLADLE_TASK_NAME = "runFlankAutoBenchmark"
        private const val ADDITIONAL_TEST_OUTPUT_DIR = "/sdcard/Download/"
        private const val ENV_ADDITIONAL_TEST_OUTPUT_KEY = "additionalTestOutputDir"
        private const val ENV_ADDITIONAL_NO_ISOLATION_KEY = "no-isolated-storage"
        private const val ENV_ADDITIONAL_NO_ISOLATION_VALUE = "true"
        private const val PLUGIN_APPLY_ERROR_MESSAGE = "This plugin is only applicable for Android modules"
    }

    override fun apply(project: Project) {
        // Apply fladle plugin as dependency
        project.pluginManager.apply("com.osacky.fladle")

        // Fail if plugin is not applied to android app module
        runCatching {
            project.extensions.getByType(AndroidComponentsExtension::class.java)
        }.getOrElse { error(PLUGIN_APPLY_ERROR_MESSAGE) }

        // Get the extensions
        val extension = AutoBenchmarkExtension.create(project)

        // Register Fladle configuration to run tests on firebase test lab and download benchmark result
        configureFladle(project, extension)
        // Register task to verify downloaded benchmark result
        setupMacroBenchmarkVerificationTask(project, extension)
    }

    /**
     * Adds a fladle configuration to be executed for uploading apks to Firebase test lab.
     * Also, configures custom test output directory and download directory from G-Cloud.
     *
     * @param project An instance of gradle project
     * @param extension An instance of [AutoBenchmarkExtension]
     */
    private fun configureFladle(project: Project, extension: AutoBenchmarkExtension) {
        project.extensions.configure(FlankGradleExtension::class.java) {
            configs.register(FLADLE_CONFIG_NAME) {
                with(this@configure) {
                    flakyTestAttempts.set(1)
                    localResultsDir.set(LOCAL_RESULT_DIR)
                    performanceMetrics.set(false)
                    disableSharding.set(true)
                    devices.set(
                        listOf(
                            extension.physicalDevices.get()
                        )
                    )
                    projectId.set(extension.firebaseProjectId.get())
                }

                apply {
                    filesToDownload.set(listOf(".*$ADDITIONAL_TEST_OUTPUT_DIR.*"))
                    directoriesToPull.set(listOf(ADDITIONAL_TEST_OUTPUT_DIR))
                    debugApk.set(project.provider { "${project.rootDir.path}${extension.appApkFilePath.get()}" })
                    instrumentationApk.set(project.provider {
                        "${project.rootDir.path}${extension.benchmarkApkFilePath.get()}"
                    })
                    environmentVariables.set(
                        mapOf(
                            ENV_ADDITIONAL_TEST_OUTPUT_KEY to ADDITIONAL_TEST_OUTPUT_DIR,
                            ENV_ADDITIONAL_NO_ISOLATION_KEY to ENV_ADDITIONAL_NO_ISOLATION_VALUE
                        )
                    )
                }
            }
        }
    }

    /**
     * Register a task for benchmark result verification.
     * This task depends on fladle task created above to run tests on firebase test lab and download result.
     *
     * @param project An instance of gradle project
     * @param extension An instance of [AutoBenchmarkExtension]
     */
    private fun setupMacroBenchmarkVerificationTask(project: Project, extension: AutoBenchmarkExtension) {
        project.tasks.register(
            TASK_NAME,
            BenchmarkJsonParserTask::class.java
        ) {
            buildDirectory.set(project.buildDir)
            tolerancePercentage.set(extension.tolerancePercentage)
            dependsOn(FLADLE_TASK_NAME)
        }
    }
}