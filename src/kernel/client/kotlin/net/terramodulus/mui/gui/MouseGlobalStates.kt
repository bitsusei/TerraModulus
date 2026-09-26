/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui

import com.cout970.math.vec2.ImmVec2d
import com.cout970.math.vec2.Vec2f
import net.terramodulus.mui.kui.MouseInputHandler

class MouseGlobalStates : InputGlobalStates<MouseState, MouseState.Key>() {
	internal fun update(events: Sequence<MouseInputHandler.Event>, mousePos: Vec2f) {
		val mousePos = ImmVec2d(mousePos.xd, mousePos.yd)
		events.forEach {
			when (it) {
				is MouseInputHandler.Event.Button -> {
					when (it) {
						is MouseInputHandler.Event.Button.Down -> triggerListeners(
							MouseState.Key.ButtonJustDown(it.key),
							MouseState.ButtonJustDown(it.key, mousePos),
						)
						is MouseInputHandler.Event.Button.Up -> triggerListeners(
							MouseState.Key.ButtonJustUp(it.key),
							MouseState.ButtonJustUp(it.key, mousePos),
						)
					}
				}
				is MouseInputHandler.Event.Movement -> {
					triggerListeners(
						MouseState.Key.Movement,
						MouseState.Movement(it.delX.toDouble(), it.delY.toDouble(), mousePos),
					)
				}
			}
		}
	}
}
