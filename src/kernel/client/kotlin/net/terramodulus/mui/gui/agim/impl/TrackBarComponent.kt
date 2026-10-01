/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec4.Vec4i
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.Dimension2D
import net.terramodulus.mui.gui.gfx.Direction4A
import net.terramodulus.mui.gui.gfx.GeneralTransform
import net.terramodulus.mui.gui.gfx.GuiRect
import net.terramodulus.mui.gui.gfx.ModelTransform
import net.terramodulus.mui.gui.gfx.RectStParams
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleI
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.util.LateInitObservable

/**
 * Track Bar, with marks on the bar.
 */
// TODO Add decoration marks like the handle
class TrackBarComponent(
	canvasHandle: RenderSystem.CanvasHandle,
	asdHandle: AsdHandle,
	config: ConstructEnv.() -> ConstructEnv.Config,
) : Component(asdHandle) {
	private sealed class BarMode private constructor() {
		companion object {
			val BOUNDS = RectangleI(0, 0, 1, 1)
		}

		abstract fun addModel(modelTransform: ModelTransform)

		abstract fun updateBar(fraction: Double)

		abstract fun renderBar(renderSystem: RenderSystem)

		class Singleton(canvasHandle: RenderSystem.CanvasHandle, color: Vec4i) : BarMode() {
			private val bar = GuiRect(canvasHandle,
				BOUNDS.x, BOUNDS.y, BOUNDS.width, BOUNDS.height,
				color.x, color.y, color.z, color.w,
			)

			override fun addModel(modelTransform: ModelTransform) = bar.add(modelTransform)

			override fun updateBar(fraction: Double) {}

			override fun renderBar(renderSystem: RenderSystem) = bar.render(renderSystem)
		}

		class Sectioned(
			canvasHandle: RenderSystem.CanvasHandle,
			private val dir: Direction4A,
			priColor: Vec4i,
			secColor: Vec4i,
		) : BarMode() {
			// Primary section refers to the section uncovered by the fraction and the direction.
			// Secondary section refers to the section directed by the fraction with the direction.
			private val priBar = GuiRect(canvasHandle,
				BOUNDS.x, BOUNDS.y, BOUNDS.width, BOUNDS.height,
				priColor.x, priColor.y, priColor.z, priColor.w,
			)
			private val secBar = GuiRect(canvasHandle,
				BOUNDS.x, BOUNDS.y, BOUNDS.width, BOUNDS.height,
				secColor.x, secColor.y, secColor.z, secColor.w,
			)
			private val priTransform = GeneralTransform().apply { priBar.add(this) }
			private val secTransform = GeneralTransform().apply { secBar.add(this) }

			override fun addModel(modelTransform: ModelTransform) {
				priBar.add(modelTransform)
				secBar.add(modelTransform)
			}

			override fun updateBar(fraction: Double) {
				RectStParams.withScale(BOUNDS.toDouble(), dir.toOppo(), fraction).applyToGeneralTransform(priTransform)
				RectStParams.withScale(BOUNDS.toDouble(), dir, fraction).applyToGeneralTransform(secTransform)
			}

			override fun renderBar(renderSystem: RenderSystem) {
				priBar.render(renderSystem)
				secBar.render(renderSystem)
			}
		}
	}

	private val barMode: BarMode
	private val size: Dimension2D
	private val bounds: RectangleD // calculated from `size` with origin (0, 0)
	private val barRect: RectangleD
	private val dir: Direction4A

	private val barTransform = GeneralTransform()
	private val mainTransform = GeneralTransform()

	var fraction by LateInitObservable<Double> { _, _, new -> update(new) }

	init {
		val config = config(ConstructEnv)
		barMode = when (config.mode) {
			is ConstructEnv.BarMode.Sectioned ->
				BarMode.Sectioned(canvasHandle, config.dir, config.mode.priColor, config.mode.secColor)
			is ConstructEnv.BarMode.Singleton -> BarMode.Singleton(canvasHandle, config.mode.color)
		}
		barMode.addModel(barTransform)
		barMode.addModel(mainTransform)
		size = config.size
		bounds = RectangleD(0.0, 0.0, size.width, size.height)
		barRect = when (config.dir) {
			Direction4A.XPos -> RectangleD(
				config.offMin,
				(size.height - config.breadth) / 2,
				size.width - config.offMin - config.offMax,
				config.breadth,
			)
			Direction4A.XNeg -> RectangleD(
				config.offMax,
				(size.height - config.breadth) / 2,
				size.width - config.offMin - config.offMax,
				config.breadth,
			)
			Direction4A.YPos -> RectangleD(
				(size.width - config.breadth) / 2,
				config.offMin,
				config.breadth,
				size.height - config.offMin - config.offMax,
			)
			Direction4A.YNeg -> RectangleD(
				(size.width - config.breadth) / 2,
				config.offMax,
				config.breadth,
				size.height - config.offMin - config.offMax,
			)
		}
		RectStParams.fromRects(BarMode.BOUNDS.toDouble(), barRect).applyToGeneralTransform(barTransform)
		dir = config.dir
		if (config.fraction != null) fraction = config.fraction

		val dim = IntrinsicDimensionsProperty(size.width.toUInt(), size.height.toUInt())
		asdHandle.properties.putProperty(IntrinsicDimensionsProperty.KEY, dim)
		asdHandle.properties.putProperty(IntrinsicRatioProperty.KEY, dim.computeRatio())
		asdHandle.observeRect {
			RectStParams.fromRects(bounds, asdHandle.rect).applyToGeneralTransform(mainTransform)
		}
	}

	@Suppress("unused")
	object ConstructEnv {
		val XPos = Direction4A.XPos
		val XNeg = Direction4A.XNeg
		val YPos = Direction4A.YPos
		val YNeg = Direction4A.YNeg
		sealed class BarMode {
			data class Singleton(val color: Vec4i) : BarMode()
			data class Sectioned(val priColor: Vec4i, val secColor: Vec4i) : BarMode()
		}

		/**
		 * Offsets must be non-negative, as insets from bounds.
		 * Targets of offsets depend on the direction.
		 * The dimension in the cross axis should be larger or equal than the breadth.
		 */
		class Config(
			val mode: BarMode,
			// TODO Currently fixed sized, but later should be dynamic
			val size: Dimension2D,
			val dir: Direction4A,
			val offMin: Double,
			val offMax: Double,
			val breadth: Double,
			val fraction: Double? = null
		)
	}

	private fun update(fraction: Double) {
		barMode.updateBar(fraction)
	}

	override fun render(renderSystem: RenderSystem) {
		barMode.renderBar(renderSystem)
	}
}
