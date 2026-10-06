/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.asd

import net.terramodulus.engine.BaseAsdProcessor

// TODO in reality, only binary data are consumed, but
//   in testing environment, objects are created directly
//   - most likely, each AGIMO and Layout register necessary Processor
//     to consume input data and configure its managed instance of AGIMO/Layout
abstract class AsdProcessor<T> {
	companion object {
		fun get(): BaseAsdProcessor = TODO("This processor's only use is to provide InputStream")
	}

	// TODO Temporary implementation to configure ASD without binary data but produced configs
	abstract fun processDefined(definitions: T)

	// TODO Temporary implementation to produce defined configurations for testing and demo
	abstract fun produceDefined(): T
}
