/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.InputStatesHandle
import net.terramodulus.mui.gui.MouseCtxStates
import net.terramodulus.mui.gui.agim.AbstractPane
import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.kui.MouseInputHandler

class ButtonComponent(
	asdHandle: AsdHandle,
	inputStatesHandle: InputStatesHandle,
	layout: ButtonComponent.() -> Layout,
) : AbstractPane(asdHandle) {
	override val layout = layout(this)
	private val mouseCtxStates = MouseCtxStates(inputStatesHandle.mouseGlobalStates, asdHandle).apply {
		addListener(listenRectFullClick(MouseInputHandler.Buttons.Left.id) {
			println("Hello World!")
		})
	}

	override fun render(renderSystem: RenderSystem) = layout.render(renderSystem)
}
