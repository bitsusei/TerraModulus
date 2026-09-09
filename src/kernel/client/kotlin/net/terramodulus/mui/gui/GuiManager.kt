/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui

import net.terramodulus.core.TerraModulus
import net.terramodulus.engine.Window
import net.terramodulus.mui.MuiManager
import net.terramodulus.mui.gui.agim.LayoutManager
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.agim.ScreenManager
import net.terramodulus.mui.gui.asd.AsdManager
import net.terramodulus.util.logging.logger
import java.io.Closeable

private val logger = logger {}

/**
 * Graphical User Interface (GUI) Manager
 */
internal class GuiManager internal constructor(private val window: Window, core: TerraModulus) : Closeable {
	val renderSystem = RenderSystem(core, window.canvas)
	val asdManager = AsdManager()
	val inputStatesHandle = InputStatesHandle()
	val screenManager = ScreenManager(window, renderSystem.handle, asdManager.AgimHandle(), inputStatesHandle)
	val layoutManager = LayoutManager(screenManager)

	private var proceeded = false

	/**
	 * Screen updating, targeting as the same as *maximum FPS*,
	 * but the numbers of ticks are not supposed to be compensated when missed,
	 * so it is up to the callers to compensate missed activities.
	 */
	internal fun updateScreens(muiManager: MuiManager) {
		screenManager.update(muiManager)
		if (!proceeded) {
			asdManager.process()
			proceeded = true
		}
		layoutManager.tick()
	}

	/**
	 * Canvas updating, per frame, maximally the *maximum FPS*.
	 * This includes input ticking and canvas rendering.
	 */
	internal fun updateCanvas() {
		window.canvas.clear()
		screenManager.render(renderSystem)
		window.swap()
	}

	override fun close() {
		window.close()
	}
}
