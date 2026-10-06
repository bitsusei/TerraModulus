/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.event

// Must not be used in kernel
abstract class GenericEvent : BubblingEvent() {
	override fun equals(other: Any?): Boolean {
		return this === other
	}

	override fun hashCode(): Int {
		return System.identityHashCode(this)
	}
}
