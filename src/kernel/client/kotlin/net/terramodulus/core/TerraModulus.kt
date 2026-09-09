/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.core

import net.terramodulus.common.core.AbstractTerraModulus
import net.terramodulus.mui.MuiManager
import net.terramodulus.mui.gui.GuiManager
import net.terramodulus.void.World

class TerraModulus internal constructor() : AbstractTerraModulus() {
	private val muiManager = MuiManager(this)
	internal var world: World? = null

	override var tps: Int
		get() = TODO("Not yet implemented")
		set(value) {}

	override fun run() {
		muiManager.showWindow()
		while (true) {
			muiManager.update()
			Thread.sleep(1)
		}
	}

	override fun close() {
		TODO("Not yet implemented")
	}
}
