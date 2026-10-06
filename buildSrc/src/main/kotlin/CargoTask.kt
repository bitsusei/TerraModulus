/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class CargoTask @Inject constructor(private val execOperations: ExecOperations, objectFactory: ObjectFactory) : DefaultTask() {
// 	@get:Input
// 	abstract val environment: Property<Map<String, String>>

	@get:Input
	@get:Optional
	abstract val release: Property<Boolean>

	@get:Input
	@get:Optional
	val args: ListProperty<String> = objectFactory.listProperty(String::class.java)

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	init {
		release.convention(false)
		args.convention(args.empty())
	}

	@TaskAction
	fun build() {
		execOperations.exec {
			executable = "cargo"
			args = listOf("build") + this@CargoTask.args.get()
			if (release.get()) args = args + "--release"
			workingDir = project.projectDir
		}
	}
}
