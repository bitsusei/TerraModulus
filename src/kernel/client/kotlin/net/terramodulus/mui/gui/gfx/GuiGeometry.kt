/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

import net.terramodulus.engine.GeomDrawable
import net.terramodulus.engine.SimpleLineGeom
import net.terramodulus.engine.SimpleRectGeom

sealed class GuiGeometry(protected val geom: GeomDrawable) {
	fun add(model: ModelTransform) = geom.add(model)

	fun add(filter: ColorFilter) = geom.add(filter)

	protected fun setPos(pos: FloatArray) = geom.setPos(pos)

	fun render(renderSystem: RenderSystem) = renderSystem.renderGuiGeo(geom)
}

class GuiLine(handle: RenderSystem.CanvasHandle, x0: Int, y0: Int, x1: Int, y1: Int, r: Int, g: Int, b: Int, a: Int) :
	GuiGeometry(SimpleLineGeom(handle.canvas, x0, y0, x1, y1, r, g, b, a)) {
	fun setPos(x0: Int, y0: Int, x1: Int, y1: Int) =
		setPos(floatArrayOf(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat()))
}

class GuiRect(handle: RenderSystem.CanvasHandle, x0: Int, y0: Int, x1: Int, y1: Int, r: Int, g: Int, b: Int, a: Int) :
	GuiGeometry(SimpleRectGeom(handle.canvas, x0, y0, x1, y1, r, g, b, a)) {
	fun setPos(x0: Int, y0: Int, x1: Int, y1: Int) =
		setPos(floatArrayOf(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat()))
}
