/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui

import com.cout970.math.vec2.Vec2f
import net.terramodulus.mui.kui.InputSystem.InputEvent

class InputStatesHandle {
    val mouseGlobalStates = MouseGlobalStates()

	internal fun update(events: Sequence<InputEvent>, mousePos: Vec2f) {
		mouseGlobalStates.update(events.filterIsInstance<InputEvent.Mouse>().map { it.inner }, mousePos)
	}
}
