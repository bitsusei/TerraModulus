/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui

sealed class InputGlobalStates<S : InputState, K : Any> {
	protected val triggers = mutableMapOf<K, MutableSet<InputState.Trigger<S, K>>>()
	protected val listeners = mutableMapOf<InputState.Trigger<S, K>, InputState.Listener<S, K>>()

	internal fun addListener(listener: InputState.Listener<S, K>) {
		listener.triggers.forEach {
			listeners[it] = listener
			triggers.computeIfAbsent(it.key) { mutableSetOf() }.add(it)
		}
	}

	internal fun removeListener(listener: InputState.Listener<S, K>) {
		listener.triggers.forEach {
			listeners.remove(it)
			triggers[it.key]!!.remove(it)
		}
	}

	internal fun triggerListeners(key: K, state: S) {
		triggers[key]?.forEach { listeners[it]!!.act(state) }
	}
}
