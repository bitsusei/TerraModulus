/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface CargoExtension {
	val release: Property<Boolean>
	val outputFile: RegularFileProperty
}
