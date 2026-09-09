/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.mui.gui.agim.event.ComponentEvent
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.RenderSystem

/**
 * [Component] can only be contained by only one [Container][net.terramodulus.mui.gui.agim.Container] at once.
 *
 * It is an undefined behavior when the `Component` is contained repeatedly
 * or in different containers simultaneously.
 */
abstract class Component(open val asdHandle: AsdHandle) {
	private val listeners = HashMap<Class<out ComponentEvent>, LinkedHashSet<(ComponentEvent) -> Unit>>()

// 	/**
// 	 * Caveat: This should only be modified by [Layout][net.terramodulus.mui.gui.agim.Layout] managers.
// 	 */
// 	open lateinit var layoutHandle: LayoutHandle
// 		internal set

	abstract fun render(renderSystem: RenderSystem)

	fun <T: ComponentEvent> addListener(e: Class<T>, l: (T) -> Unit) {
		@Suppress("UNCHECKED_CAST")
		listeners.computeIfAbsent(e) { LinkedHashSet() }.add(l as (ComponentEvent) -> Unit)
	}

	fun <T: ComponentEvent> removeListener(e: Class<T>, l: (T) -> Unit) {
		listeners[e]?.remove(l)
	}

	internal fun dispatchEvent(event: ComponentEvent) {
		listeners[event.javaClass]?.forEach { it(event) }
	}

	internal open fun update(muiIoI: ScreenManager.MuiIoI) {
		dispatchEvent(ComponentEvent.Update(muiIoI))
	}
}
