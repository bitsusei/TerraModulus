/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec4.Vec4i
import net.terramodulus.engine.GeneralTransform
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.ColorFilter
import net.terramodulus.mui.gui.gfx.Direction4A
import net.terramodulus.mui.gui.gfx.GuiRect
import net.terramodulus.mui.gui.gfx.RectStParams
import net.terramodulus.mui.gui.gfx.RectangleI
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.util.LateInitObservable

/**
 * A single scaled bar with the provided dimensions as the bounds of the bar.
 */
open class ScaledBarComponent protected constructor(
	canvasHandle: RenderSystem.CanvasHandle,
	asdHandle: AsdHandle,
	config: Config,
) : Component(asdHandle) {
	constructor(
		canvasHandle: RenderSystem.CanvasHandle,
		asdHandle: AsdHandle,
		config: ConstructEnvImpl.() -> Config,
	) : this(canvasHandle, asdHandle, config(ConstructEnvImpl))

	companion object {
		@JvmStatic
		protected val BOUNDS = RectangleI(0, 0, 1, 1)
	}

	private val scaleTransform = GeneralTransform()
	protected val boundsTransform = GeneralTransform()
	protected val dir = config.dir
	protected val bar = GuiRect(canvasHandle,
		BOUNDS.x, BOUNDS.y, BOUNDS.width, BOUNDS.height,
		config.color.x, config.color.y, config.color.z, config.color.w,
	).apply {
		add(scaleTransform)
		add(boundsTransform)
	}

	var fraction by LateInitObservable<Double> { _, _, new -> update(new) }

	init {
		if (config.fraction != null) fraction = config.fraction
		asdHandle.observeRect {
			RectStParams.fromRects(BOUNDS.toDouble(), asdHandle.rect).applyToGeneralTransform(boundsTransform)
		}
	}

	fun addFilter(filter: ColorFilter) = bar.add(filter)

	@Suppress("unused")
	interface ConstructEnv {
		val xPos get() = Direction4A.XPos
		val xNeg get() = Direction4A.XNeg
		val yPos get() = Direction4A.YPos
		val yNeg get() = Direction4A.YNeg
	}

	object ConstructEnvImpl : ConstructEnv {
		fun config(dir: Direction4A, color: Vec4i, fraction: Double? = null) =
			Config(dir, color, fraction)
	}

	open class Config(
		val dir: Direction4A,
		val color: Vec4i,
		val fraction: Double? = null
	)

	private fun update(fraction: Double) {
		RectStParams.withScale(BOUNDS.toDouble(), dir, fraction).applyToGeneralTransform(scaleTransform)
	}

	override fun render(renderSystem: RenderSystem) = bar.render(renderSystem)
}
