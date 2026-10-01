/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.asd.AsdProcessor
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleF

abstract class AbstractPane(asdHandle: AsdHandle) : Component(asdHandle), Container {
	protected inner class ComponentAsdHandleImpl : AsdHandle.Container() {
		override lateinit var rect: RectangleD
		override fun registerAsdProcessor(processor: AsdProcessor<*>) = asdHandle.registerAsdProcessor(processor)
	}

	final override fun update(muiIoI: ScreenManager.MuiIoI) {
		super.update(muiIoI)
		layout.update()
		layout.components.forEach { it.update(muiIoI) }
	}
}
