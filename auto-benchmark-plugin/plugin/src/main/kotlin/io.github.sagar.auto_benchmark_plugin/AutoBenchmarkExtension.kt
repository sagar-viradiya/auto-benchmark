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
