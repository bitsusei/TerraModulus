/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.core

import net.terramodulus.common.core.AbstractTerraModulus
import net.terramodulus.mui.MuiManager
import net.terramodulus.mui.gui.GuiManager
import net.terramodulus.void.World
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class TerraModulus internal constructor() : AbstractTerraModulus() {
	private val muiManager = MuiManager(this)
	internal var world: World? = null

	var tps = 0
		private set

	override fun run() {
		muiManager.showWindow()
		val timeSource = TimeSource.Monotonic
		var lastTick = timeSource.markNow()
		var ticks = 0
		while (true) {
			muiManager.update()
			ticks++
			val now = timeSource.markNow()
			if (now - lastTick >= 1.seconds) {
				lastTick = now
				tps = ticks
				ticks = 0
			}
			Thread.sleep(0)
		}
	}

	override fun close() {
		TODO("Not yet implemented")
	}
}
