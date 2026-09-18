/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.uid

class MouseDevice(override val id: Device.Id) : Device {
	class Button internal constructor() {
		var down = false
	}

	@JvmInline
	value class ButtonId(private val id: UInt)

	private val buttons = HashMap<ButtonId, Button>()

	init {
		// Refers to ferricia::mui::MouseKey
		for (i in 0u..4u) buttons[ButtonId(i)] = Button()
	}

	internal fun getKey(key: ButtonId) = buttons[key]
	internal fun iterButtons() = buttons.asSequence()
}
