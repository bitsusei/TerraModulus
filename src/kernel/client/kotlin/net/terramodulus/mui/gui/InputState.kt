/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui

/**
 * It is unlikely that local triggers being unaffected by other factors,
 * so this should match any range of inputs while this may also filter for specific triggers.
 *
 * Likely be implemented as simple data classes.
 */
sealed class InputState {
	sealed interface Listener<S : InputState, K : Any> {
		val triggers: Set<Trigger<S, K>>

		fun act(state: S)
	}

	sealed interface Trigger<S : InputState, K : Any> {
		/**
		 * Shall be implemented as simple data classes supporting equality.
		 */
		val key: K

		fun check(state: S): Boolean
	}
}
