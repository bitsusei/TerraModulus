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
import net.terramodulus.mui.gui.gfx.GuiLine
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.kui.MouseInputHandler

class CheckboxComponent(
	asdHandle: AsdHandle,
	inputStatesHandle: InputStatesHandle,
	canvasHandle: RenderSystem.CanvasHandle,
	callback: (Boolean) -> Unit,
) : AbstractPane(asdHandle) {
	private var checked = false
	private val uncheckedFace = DrawablesComponent(sequenceOf(
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 0, 0, 50, 255, 255, 255, 255)),
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 0, 50, 0, 255, 255, 255, 255)),
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 50, 50, 50, 255, 255, 255, 255)),
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 50, 0, 50, 50, 255, 255, 255, 255)),
	), RectangleD(0.0, 0.0, 50.0, 50.0), ComponentAsdHandleImpl())
	private val checkedFace = DrawablesComponent(sequenceOf(
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 0, 0, 50, 255, 255, 255, 255)),
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 0, 50, 0, 255, 255, 255, 255)),
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 50, 50, 50, 255, 255, 255, 255)),
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 50, 0, 50, 50, 255, 255, 255, 255)),
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 10, 20, 20, 10, 255, 255, 255, 255)),
		DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 20, 10, 45, 45, 255, 255, 255, 255)),
	), RectangleD(0.0, 0.0, 50.0, 50.0), ComponentAsdHandleImpl())
	override val layout = SingletonLayout(this, uncheckedFace, SingletonLayout.Config.Absolute.Full)
// 	override val layout = SingletonLayout(this, DrawablesComponent(object : Sequence<DrawablesComponent.Drawable> {
// 		private val outline = sequenceOf(
// 			DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 0, 0, 50, 255, 255, 255, 255)),
// 			DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 0, 50, 0, 255, 255, 255, 255)),
// 			DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 0, 50, 50, 50, 255, 255, 255, 255)),
// 			DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 50, 0, 50, 50, 255, 255, 255, 255)),
// 		)
// 		private val mark = sequenceOf(
// 			DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 10, 20, 20, 10, 255, 255, 255, 255)),
// 			DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 20, 10, 45, 45, 255, 255, 255, 255)),
// 		)
// 		override fun iterator(): Iterator<DrawablesComponent.Drawable> =
// 			if (checked) { outline + mark } else { outline }.iterator()
// 	}, RectangleD(0.0, 0.0, 50.0, 50.0), ComponentAsdHandleImpl()), SingletonLayout.Config.Absolute.Full)
	private val mouseCtxStates = MouseCtxStates(inputStatesHandle.mouseGlobalStates, asdHandle).apply {
		addListener(listenRectFullClick(MouseInputHandler.Buttons.Left.id) {
			checked = !checked
			layout.update(if (checked) { checkedFace } else { uncheckedFace })
			callback(checked)
		})
	}

	override fun render(renderSystem: RenderSystem) = layout.render(renderSystem)
}
