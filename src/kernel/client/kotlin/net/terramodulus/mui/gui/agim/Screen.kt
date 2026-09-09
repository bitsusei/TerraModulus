/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.mui.gui.agim.event.ScreenEvent
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.asd.AsdProcessor
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleF
import net.terramodulus.mui.gui.gfx.RenderSystem
import java.io.Closeable

abstract class Screen(
	managerHandle: ScreenManager.Handle,
	final override val asdHandle: AsdHandle.Container
) : Container, Closeable {
	private val listeners = HashMap<Class<out ScreenEvent>, LinkedHashSet<(ScreenEvent) -> Unit>>()
	private val menuManager = MenuManager(asdHandle::registerAsdProcessor)
	val handle: Handle = HandleImpl(managerHandle)

// 	init {
// 		// TODO there should be an entry for background, maybe it sets background for entire render background?
// 		asdHandle.registerAsdProcessor()
// 	}

	fun <T: ScreenEvent> addListener(e: Class<T>, l: (T) -> Unit) {
		@Suppress("UNCHECKED_CAST")
		listeners.computeIfAbsent(e) { LinkedHashSet() }.add(l as (ScreenEvent) -> Unit)
	}

	fun <T: ScreenEvent> removeListener(e: Class<T>, l: (T) -> Unit) {
		listeners[e]?.remove(l)
	}

	internal fun dispatchEvent(event: ScreenEvent) {
		listeners[event.javaClass]?.forEach { it(event) }
	}

	sealed interface Handle {
		fun addMenu(menu: (MenuManager.Handle, AsdHandle) -> Menu)

		fun removeMenu(menu: Menu)

		fun addTopMenu(menu: (MenuManager.Handle, AsdHandle) -> Menu)

		fun removeTopMenu(menu: Menu)
	}

	private inner class HandleImpl(private val managerHandle: ScreenManager.Handle) : Handle {
		override fun addMenu(menu: (MenuManager.Handle, AsdHandle) -> Menu) = menuManager.handle.addMenu(menu)

		override fun removeMenu(menu: Menu) = menuManager.handle.removeMenu(menu)

		override fun addTopMenu(menu: (MenuManager.Handle, AsdHandle) -> Menu) = managerHandle.addMenu(menu)

		override fun removeTopMenu(menu: Menu) = managerHandle.removeMenu(menu)
	}

	protected inner class ComponentAsdHandleImpl : AsdHandle.Container() {
		override lateinit var rect: RectangleD
		override fun registerAsdProcessor(processor: AsdProcessor<*>) = asdHandle.registerAsdProcessor(processor)
	}

	internal fun update(muiIoI: ScreenManager.MuiIoI) {
		dispatchEvent(ScreenEvent.Update(muiIoI))
		layout.update()
		layout.components.forEach { it.update(muiIoI) }
	}

	internal fun render(renderSystem: RenderSystem, screenManager: ScreenManager) {
		layout.render(renderSystem)
		menuManager.render(renderSystem, screenManager)
	}

	/**
	 * Cleans up and closes any used resources in this session.
	 */
	final override fun close() {
		dispatchEvent(ScreenEvent.Close)
	}

	internal fun visit() = menuManager.visit()
}
