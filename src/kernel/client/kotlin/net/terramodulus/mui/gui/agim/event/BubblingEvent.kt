/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.event

sealed class BubblingEvent {
	var bubbles: Boolean = true
		private set

	fun stopPropagation() {
		bubbles = false
	}
}
