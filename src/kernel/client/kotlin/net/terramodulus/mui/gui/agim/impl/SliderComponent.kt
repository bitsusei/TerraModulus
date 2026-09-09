/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec4.Vec4i
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.ColorFilter
import net.terramodulus.mui.gui.gfx.Direction4A
import net.terramodulus.mui.gui.gfx.Direction4AD
import net.terramodulus.mui.gui.gfx.GeneralTransform
import net.terramodulus.mui.gui.gfx.GuiRect
import net.terramodulus.mui.gui.gfx.RectStParams
import net.terramodulus.mui.gui.gfx.Rectangle
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleI
import net.terramodulus.mui.gui.gfx.RenderSystem
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class SliderComponent(
	val dir: Direction4A,
	canvasHandle: RenderSystem.CanvasHandle,
	asdHandle: AsdHandle,
	config: Config,
) : Component(asdHandle) {
	// Though this could be made into a Pane, it would be better to optimize this into a simple Component
	// to reduce complexities and avoid unnecessary layout computations.
	companion object {
		// Kind of like resolution, since parameters of Engine geometries only accept integers at the moment.
		private val BOUNDS = RectangleI(0, 0, 10000, 10000)
	}

	private val bgTransform = GeneralTransform()
	private val fgTransform = GeneralTransform()
	private val background = GuiRect(canvasHandle,
		BOUNDS.x, BOUNDS.y, BOUNDS.width, BOUNDS.height,
		config.bgColor.x, config.bgColor.y, config.bgColor.z, config.bgColor.w,
	).apply { add(bgTransform) }
	private val foreground = GuiRect(canvasHandle,
		BOUNDS.x, BOUNDS.y, BOUNDS.width, BOUNDS.height,
		config.fgColor.x, config.fgColor.y, config.fgColor.z, config.bgColor.w,
	).apply { add(fgTransform) }
	var fraction: Double by Delegates.observable(0.0) { _, _, value ->
		try {
			updateForeground(asdHandle.rect, value)
		} catch (_: UninitializedPropertyAccessException) {}
	}

	class Config(val bgColor: Vec4i, val fgColor: Vec4i)

	init {
		asdHandle.observeRect {
			RectStParams.fromRects(BOUNDS.toDouble(), asdHandle.rect).applyToGeneralTransform(bgTransform)
			updateForeground(asdHandle.rect, fraction)
		}
	}

	fun addFilter(filter: ColorFilter) {
		background.add(filter)
		foreground.add(filter)
	}

	private fun updateForeground(rect: RectangleD, fraction: Double) {
		// Using division by fraction is a quick hack to apply scaling along the axis while using the same BOUNDS.
		RectStParams.fromRects(when (dir) {
			Direction4A.XPos -> // left to right
				Rectangle.withDirection(
					BOUNDS.x, BOUNDS.y, (BOUNDS.width / fraction).roundToInt(), BOUNDS.height,
					Direction4AD.QuadOne,
				)
			Direction4A.XNeg -> // right to left
				Rectangle.withDirection(
					BOUNDS.width, BOUNDS.y, (BOUNDS.width / fraction).roundToInt(), BOUNDS.height,
					Direction4AD.QuadTwo,
				)
			Direction4A.YPos -> // bottom to top
				Rectangle.withDirection(
					BOUNDS.x, BOUNDS.y, BOUNDS.width, (BOUNDS.height / fraction).roundToInt(),
					Direction4AD.QuadOne,
				)
			Direction4A.YNeg -> // top to bottom
				Rectangle.withDirection(
					BOUNDS.x, BOUNDS.height, BOUNDS.width, (BOUNDS.height / fraction).roundToInt(),
					Direction4AD.QuadFour,
				)
		}.toDouble(), rect).applyToGeneralTransform(fgTransform)
	}

	override fun render(renderSystem: RenderSystem) {
		background.render(renderSystem)
		foreground.render(renderSystem)
	}
}
