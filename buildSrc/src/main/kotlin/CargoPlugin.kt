/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

import org.gradle.api.Plugin
import org.gradle.api.Project

class CargoPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		val extension = target.extensions.create("cargoConfig", CargoExtension::class.java)

		extension.release.convention(false)

		target.tasks.withType(CargoTask::class.java).configureEach {
			release.convention(extension.release)
			outputFile.convention(extension.outputFile)
			inputs.dir(project.projectDir)
		}
	}
}
