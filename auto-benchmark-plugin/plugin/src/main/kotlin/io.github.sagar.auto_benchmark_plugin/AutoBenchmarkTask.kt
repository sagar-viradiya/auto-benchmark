package io.github.sagar.auto_benchmark_plugin

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class AutoBenchmarkTask : DefaultTask() {

    @get:Input
    abstract val variantName: Property<String>

    @get: Input
    abstract val appModuleName: Property<String>

    @TaskAction
    fun execute() {

    }
}