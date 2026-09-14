/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.AbstractPane
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.InsetsD
import net.terramodulus.mui.gui.gfx.RenderSystem

class BorderedPane(canvasHandle: RenderSystem.CanvasHandle, asdHandle: AsdHandle, component: Component, config: Config) : AbstractPane(asdHandle) {
	private val _layout = (config.breadth + config.gap).let {
		SingletonLayout(this, component, SingletonLayout.Config.Absolute.Insets(InsetsD(it, it, it, it)))
	}
	override val layout: Layout = CompositeLayout(this).apply {
		update {
			// TODO incompatible with current implementation of OutlineComponent
// 			add(SingletonLayout(this, OutlineComponent(), SingletonLayout.Config.Absolute.Full))
			add(_layout)
		}
	}

	fun update(component: Component) = _layout.update(component)

	class Config(val breadth: Double, val gap: Double)

	override fun render(renderSystem: RenderSystem) {
		layout.render(renderSystem)
	}
}
