/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.kui

import net.terramodulus.mui.uid.UidManager

class KuiManager internal constructor(uidManager: UidManager) {
	val keyboardInputHandler = KeyboardInputHandler(uidManager.keyboardDevice)
	val mouseInputHandler = MouseInputHandler(uidManager.mouseDevice)
	val inputSystem = InputSystem(this)

// 	internal fun update(events: List<KeyEvent>) {
// 		inputSystem.update(events)
// 	}
}
