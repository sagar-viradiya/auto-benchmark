package io.github.sagar.auto_benchmark_plugin

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File

abstract class RunBenchmarkOnGcloudTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val appApk: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val benchmarkApk: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    @get:Optional
    abstract val serviceAccountJson: RegularFileProperty

    @get:Input
    abstract val physicalDevices: MapProperty<String, String>

    @get:OutputDirectory
    abstract val benchmarkResultDir: DirectoryProperty

    @TaskAction
    fun execute() {
        val appApkFile = appApk.get().asFile
        val benchmarkApkFile = benchmarkApk.get().asFile

        if (serviceAccountJson.isPresent) {
             val serviceAccountJsonFile = serviceAccountJson.get().asFile
             if (serviceAccountJsonFile.exists()) {
                 authenticate(serviceAccountJsonFile)
             } else {
                 project.logger.warn("Service account JSON not found at: ${serviceAccountJsonFile.absolutePath}. Assuming gcloud is already authenticated.")
             }
        }

        val deviceString = physicalDevices.get().entries.joinToString(",") { "${it.key}=${it.value}" }

        val gcloudCommand = mutableListOf(
            "gcloud", "firebase", "test", "android", "run",
            "--type", "instrumentation",
            "--app", appApkFile.absolutePath,
            "--test", benchmarkApkFile.absolutePath,
            "--device", deviceString,
            "--directories-to-pull", "/sdcard/Download",
            "--environment-variables", "additionalTestOutputDir=/sdcard/Download,no-isolated-storage=true",
            "--format", "json"
        )

        project.logger.lifecycle("Running gcloud command: ${gcloudCommand.joinToString(" ")}")

        val output = runCommand(gcloudCommand)
        val gcsPath = parseGcsPath(output)

        project.logger.lifecycle("Benchmark results stored at: $gcsPath")

        downloadResults(gcsPath)
    }

    private fun authenticate(serviceAccountJson: File) {
        val command = listOf(
            "gcloud", "auth", "activate-service-account",
            "--key-file", serviceAccountJson.absolutePath
        )
        runCommand(command)
    }

    private fun runCommand(command: List<String>): String {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = project.exec {
            commandLine = command
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            throw GradleException("Command failed: ${command.joinToString(" ")}\nError output: ${stderr.toString().trim()}")
        }
        return stdout.toString().trim()
    }

    private fun parseGcsPath(jsonOutput: String): String {
         try {
             val parsed = JsonSlurper().parseText(jsonOutput)
             // It usually is a List of objects, one per execution (matrix)
             if (parsed is List<*>) {
                  val first = parsed.firstOrNull() as? Map<*, *>
                  val resultStorage = first?.get("resultStorage") as? Map<*, *>
                  val googleCloudStorage = resultStorage?.get("googleCloudStorage") as? Map<*, *>
                  return googleCloudStorage?.get("gcsPath") as? String
                      ?: throw GradleException("Could not find gcsPath in output")
             } else if (parsed is Map<*, *>) {
                  val resultStorage = parsed["resultStorage"] as? Map<*, *>
                  val googleCloudStorage = resultStorage?.get("googleCloudStorage") as? Map<*, *>
                  return googleCloudStorage?.get("gcsPath") as? String
                      ?: throw GradleException("Could not find gcsPath in output")
             }
             throw GradleException("Unexpected JSON output format: ${parsed::class.java}")
         } catch (e: Exception) {
             throw GradleException("Failed to parse gcloud output: ${e.message}. Output: $jsonOutput", e)
         }
    }

    private fun downloadResults(gcsPath: String) {
        val destDir = benchmarkResultDir.get().asFile
        destDir.mkdirs()

        // Use gcloud storage cp to copy.
        val command = listOf(
            "gcloud", "storage", "cp", "-r", gcsPath, destDir.absolutePath
        )

        project.logger.lifecycle("Downloading results: ${command.joinToString(" ")}")
        runCommand(command)
    }
}
