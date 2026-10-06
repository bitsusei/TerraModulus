/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.mui.gui.agim.event.MenuEvent
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.asd.AsdProcessor
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleF
import net.terramodulus.mui.gui.gfx.RenderSystem
import java.io.Closeable

abstract class Menu(
	managerHandle: MenuManager.Handle,
	final override val asdHandle: AsdHandle.Container,
) : Container, Closeable {
	private val listeners = HashMap<Class<out MenuEvent>, LinkedHashSet<(MenuEvent) -> Unit>>()
	val handle: Handle = HandleImpl(managerHandle)

	fun <T: MenuEvent> addListener(e: Class<T>, l: (T) -> Unit) {
		@Suppress("UNCHECKED_CAST")
		listeners.computeIfAbsent(e) { LinkedHashSet() }.add(l as (MenuEvent) -> Unit)
	}

	fun <T: MenuEvent> removeListener(e: Class<T>, l: (T) -> Unit) {
		listeners[e]?.remove(l)
	}

	internal fun dispatchEvent(event: MenuEvent) {
		listeners[event.javaClass]?.forEach { it(event) }
	}

	sealed interface Handle {
		fun addMenu(menu: (MenuManager.Handle, AsdHandle) -> Menu)

		fun removeMenu(menu: Menu)
	}

	private inner class HandleImpl(private val managerHandle: MenuManager.Handle) : Handle {
		override fun addMenu(menu: (MenuManager.Handle, AsdHandle) -> Menu) = managerHandle.addMenu(menu)

		override fun removeMenu(menu: Menu) = managerHandle.removeMenu(menu)
	}

	protected inner class ComponentAsdHandleImpl : AsdHandle.Container() {
		override lateinit var rect: RectangleD
		override fun registerAsdProcessor(processor: AsdProcessor<*>) = asdHandle.registerAsdProcessor(processor)
	}

	internal fun render(renderSystem: RenderSystem) {
		layout.render(renderSystem)
	}

	internal fun update(muiIoI: ScreenManager.MuiIoI) {
		dispatchEvent(MenuEvent.Update(muiIoI))
		layout.update()
		layout.components.forEach { it.update(muiIoI) }
	}

	/**
	 * Cleans up and closes any used resources in this session.
	 */
	final override fun close() {
		dispatchEvent(MenuEvent.Close)
	}
}
