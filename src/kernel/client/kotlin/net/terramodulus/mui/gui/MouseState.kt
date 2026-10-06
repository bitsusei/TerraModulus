/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui

import com.cout970.math.vec2.Vec2d
import net.terramodulus.mui.uid.MouseDevice
import kotlin.time.Duration

sealed class MouseState : InputState() {
	sealed class Key private constructor() {
		data object Movement : Key()
		data class ButtonJustDown(val id: MouseDevice.ButtonId) : Key()
		data class ButtonJustUp(val id: MouseDevice.ButtonId) : Key()
		data object ButtonKeepDown : Key()
		data object WheelYNeg : Key()
		data object WheelYPos : Key()
		data object WheelXNeg : Key()
		data object WheelXPos : Key()
	}

	data class Movement(val delX: Double, val delY: Double, val pos: Vec2d) : MouseState()
	data class ButtonJustDown(val id: MouseDevice.ButtonId, val pos: Vec2d) : MouseState()
	data class ButtonJustUp(val id: MouseDevice.ButtonId, val pos: Vec2d) : MouseState()
	data class ButtonKeepDown(val dur: Duration, val pos: Vec2d) : MouseState()
	data class WheelYMotion(val delta: Double, val pos: Vec2d) : MouseState()
	data class WheelXMotion(val delta: Double, val pos: Vec2d) : MouseState()

	class Listener(override val triggers: Set<Trigger>, private val action: (MouseState) -> Unit) :
		InputState.Listener<MouseState, Key> {
		override fun act(state: MouseState) = action(state)
	}

	class Trigger(override val key: Key, private val condition: (MouseState) -> Boolean) :
		InputState.Trigger<MouseState, Key> {
		override fun check(state: MouseState) = condition(state)
	}
}
