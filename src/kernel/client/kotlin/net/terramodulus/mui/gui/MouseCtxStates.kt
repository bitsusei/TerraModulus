/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui

import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.uid.MouseDevice

class MouseCtxStates(globalStates: MouseGlobalStates, asdHandle: AsdHandle) :
	InputCtxStates<MouseState, MouseState.Key>(globalStates, asdHandle) {
	inline fun listenRectFullClick(buttonId: MouseDevice.ButtonId, crossinline action: () -> Unit): MouseState.Listener {
		var clicked = false
		return MouseState.Listener(setOf(
			MouseState.Trigger(MouseState.Key.ButtonJustDown(buttonId)) { true },
			MouseState.Trigger(MouseState.Key.ButtonJustUp(buttonId)) { true },
		)) {
			when (it) {
				is MouseState.ButtonJustDown -> {
					assert(it.id == buttonId)
					clicked = ctxRange.rect.contains(it.pos)
				}
				is MouseState.ButtonJustUp -> {
					assert(it.id == buttonId)
					if (clicked && ctxRange.rect.contains(it.pos)) action()
					clicked = false
				}
				else -> throw UnsupportedOperationException()
			}
		}
	}
}
