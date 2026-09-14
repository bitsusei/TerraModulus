/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec2.Vec2d
import com.cout970.math.vec4.Vec4i
import net.terramodulus.mui.gui.InputStatesHandle
import net.terramodulus.mui.gui.MouseCtxStates
import net.terramodulus.mui.gui.MouseState
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.Direction4A
import net.terramodulus.mui.gui.gfx.GuiRect
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.kui.MouseInputHandler
import kotlin.math.roundToInt

/**
 * Interactive Slider
 *
 * Caveat: [fraction] must not be externally modified.
 */
class SliderComponent private constructor(
	canvasHandle: RenderSystem.CanvasHandle,
	inputStatesHandle: InputStatesHandle,
	asdHandle: AsdHandle,
	config: Config,
) : ScaledBarComponent(canvasHandle, asdHandle, config) {
	constructor(
		canvasHandle: RenderSystem.CanvasHandle,
		inputStatesHandle: InputStatesHandle,
		asdHandle: AsdHandle,
		config: ConstructEnv.() -> Config,
	) : this(canvasHandle, inputStatesHandle, asdHandle, config(ConstructEnv))

	sealed class SliderMode {
		/**
		 * Translates `fraction` to a desired value point when necessary.
		 */
		internal abstract fun translate(fraction: Double): Double?

		internal abstract fun react(fraction: Double)

		// Hard to specify type here, so Any is used instead, but should be careful
		internal abstract fun getFraction(value: Any): Double

		/**
		 * @param length must be positive
		 */
		class Points(val length: Int, val reaction: (Int) -> Unit) : SliderMode() {
			override fun translate(fraction: Double) =
				(fraction * length).roundToInt().coerceIn(0, length).toDouble() / length

			override fun react(fraction: Double) = reaction((fraction * length).roundToInt().coerceIn(0, length))

			/**
			 * @param value must be [Int] in `0`..[length]
			 */
			override fun getFraction(value: Any): Double {
				require(value is Int && value in 0..length)
				return value / length.toDouble()
			}
		}

		class Ranged(val range: ClosedFloatingPointRange<Double>, val reaction: (Double) -> Unit) : SliderMode() {
			override fun translate(fraction: Double) = null

			override fun react(fraction: Double) = reaction(fraction * (range.endInclusive - range.start) + range.start)

			/**
			 * @param value must be [Double] in [range]
			 */
			override fun getFraction(value: Any): Double {
				require(value is Double && value in range)
				return (value - range.start) / (range.endInclusive - range.start)
			}
		}
	}

	private val sliderMode = config.mode

	private val background = GuiRect(canvasHandle,
		BOUNDS.x, BOUNDS.y, BOUNDS.width, BOUNDS.height,
		config.bgColor.x, config.bgColor.y, config.bgColor.z, config.bgColor.w,
	).apply { add(boundsTransform) }

	object ConstructEnv : ScaledBarComponent.ConstructEnv {
		fun withPoints(length: Int, init: Int, reaction: (Int) -> Unit) =
			SliderMode.Points(length, reaction).let { it to it.getFraction(init) }
		fun withRanged(range: ClosedFloatingPointRange<Double>, init: Double, reaction: (Double) -> Unit) =
			SliderMode.Ranged(range, reaction).let { it to it.getFraction(init) }
		fun config(mode: Pair<SliderMode, Double>, dir: Direction4A, bgColor: Vec4i, fgColor: Vec4i) =
			Config(mode.first, dir, bgColor, fgColor, mode.second)
	}

	class Config(val mode: SliderMode, dir: Direction4A, val bgColor: Vec4i, fgColor: Vec4i, fraction: Double) :
		ScaledBarComponent.Config(dir, fgColor, fraction)

	private val mouseCtxStates = MouseCtxStates(inputStatesHandle.mouseGlobalStates, asdHandle).apply {
		var reacting = false
		addListener(MouseState.Listener(
			setOf(MouseState.Trigger(MouseState.Key.ButtonJustDown(MouseInputHandler.Buttons.Left.id)) { true })
		) {
			assert(it is MouseState.ButtonJustDown && MouseInputHandler.Buttons.Left.matches(it.id))
			assert(!reacting)
			if (ctxRange.rect.contains((it as MouseState.ButtonJustDown).pos)) reacting = true
		})
		addListener(MouseState.Listener(setOf(
			MouseState.Trigger(MouseState.Key.Movement) { reacting },
			MouseState.Trigger(MouseState.Key.ButtonJustUp(MouseInputHandler.Buttons.Left.id)) { reacting },
		)) {
			assert(reacting)
			val pos: Vec2d
			when (it) {
				is MouseState.Movement -> {
					pos = it.pos
				}
				is MouseState.ButtonJustUp -> {
					assert(MouseInputHandler.Buttons.Left.matches(it.id))
					pos = it.pos
					reacting = false
				}
				else -> throw AssertionError()
			}
			val rect = asdHandle.rect
			val fraction = when (dir) {
				Direction4A.XPos -> ((pos.x - rect.x) / rect.width).coerceIn(0.0, 1.0)
				// optimized from: (rect.x + rect.width - pos.x) / rect.width
				Direction4A.XNeg -> ((rect.x - pos.x) / rect.width + 1).coerceIn(0.0, 1.0)
				Direction4A.YPos -> ((pos.y - rect.y) / rect.height).coerceIn(0.0, 1.0)
				// optimized from: (rect.y + rect.height - pos.y) / rect.height
				Direction4A.YNeg -> ((rect.y - pos.y) / rect.height + 1).coerceIn(0.0, 1.0)
			}
			this@SliderComponent.fraction = sliderMode.translate(fraction) ?: fraction
			sliderMode.react(fraction)
		})
	}

	override fun render(renderSystem: RenderSystem) {
		background.render(renderSystem)
		super.render(renderSystem)
	}
}
