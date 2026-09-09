/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.engine.Window
import net.terramodulus.mui.MuiManager
import net.terramodulus.mui.gui.InputStatesHandle
import net.terramodulus.mui.gui.agim.impl.BoundsProperty
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.agim.impl.LaunchingScreen
import net.terramodulus.mui.gui.agim.impl.RectangleProperty
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.asd.AsdManager
import net.terramodulus.mui.gui.asd.AsdProcessor
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.kui.InputSystem

class ScreenManager internal constructor(
	window: Window,
	private val renderSystemHandle: RenderSystem.Handle,
	private val asdManagerHandle: AsdManager.AgimHandle,
	private val inputStatesHandle: InputStatesHandle,
) {
	/**
	 * FILO screen stack; the top-most screen instance is in the last.
	 */
	private val screens = ArrayDeque<Screen>()
	private val screenOpQueue = ArrayDeque<ScreenOperation>()
	private val menuManager = MenuManager(asdManagerHandle::registerProcessor)

	private val asdHandles = HashSet<AsdHandle.Screen>()
	private var viewportRect = RectangleD(0.0, 0.0, window.width.toDouble(), window.height.toDouble())

	init {
		window.addListener { w, h ->
			viewportRect = RectangleD(0.0, 0.0, w.toDouble(), h.toDouble())
			asdHandles.forEach { it.triggerRectObservers() } // already referring viewportRect
		}
	}

	// TODO Basically need to hide those rectangles in LayoutManager instead
// 	inner class LayoutHandle.Screen : ManagedRect(), Closeable {
// 		override var value: RectangleF by Delegates.observable(viewportRect.value) { _, _, newValue ->
// 			observers.forEach { it(newValue) }
// 		}
// 			private set
//
// 	}

	val handle: Handle = HandleImpl()

	init {
		screens.add(LaunchingScreen(handle, ScreenAsdHandleImpl(), renderSystemHandle))
	}

	private sealed interface ScreenOperation {
		fun apply(handle: RenderSystem.Handle, screens: ArrayDeque<Screen>, asdHandles: HashSet<AsdHandle.Screen>)

		/**
		 * Exits `n` times
		 *
		 * @throws IllegalArgumentException when `n` < 1
		 * @throws IllegalStateException when `n` >= [screens] size during operation
		 */
		class Exit(val n: Int) : ScreenOperation {
			init {
				require(n > 0) { "`n` < 1" }
			}

			override fun apply(
				handle: RenderSystem.Handle,
				screens: ArrayDeque<Screen>,
				asdHandles: HashSet<AsdHandle.Screen>
			) {
				if (n >= screens.size) {
					throw IllegalStateException("`n` >= screens.size")
				}

				for (i in 1..n) {
					screens.removeLast().apply {
						close()
						asdHandles.remove(asdHandle)
					}
				}
			}
		}

		/**
		 * Opens the `screen`
		 */
		class Open(val screen: (RenderSystem.Handle) -> Screen) : ScreenOperation {
			override fun apply(
				handle: RenderSystem.Handle,
				screens: ArrayDeque<Screen>,
				asdHandles: HashSet<AsdHandle.Screen>
			) {
				screens.addLast(screen(handle))
			}
		}

		/**
		 * Opens the `screen` before the `target` screen
		 */
		class OpenBefore(val target: Screen, val screen: (RenderSystem.Handle) -> Screen) : ScreenOperation {
			override fun apply(
				handle: RenderSystem.Handle,
				screens: ArrayDeque<Screen>,
				asdHandles: HashSet<AsdHandle.Screen>
			) {
				screens.add(screens.lastIndexOf(target), screen(handle))
			}
		}

		/**
		 * Exits until reaching the `screen` then remains on the `screen`
		 */
		class ExitTo(val screen: Screen) : ScreenOperation {
			override fun apply(
				handle: RenderSystem.Handle,
				screens: ArrayDeque<Screen>,
				asdHandles: HashSet<AsdHandle.Screen>
			) {
				val it = screens.asReversed().listIterator()
				while (it.hasNext()) {
					val e = it.next()
					if (e == screen) {
						break
					} else {
						it.remove()
						e.close()
						asdHandles.remove(e.asdHandle)
					}
				}
			}
		}

		/**
		 * Clears [screens] then opens the `screen`
		 */
		class Reset(val screen: (RenderSystem.Handle) -> Screen) : ScreenOperation {
			override fun apply(
				handle: RenderSystem.Handle,
				screens: ArrayDeque<Screen>,
				asdHandles: HashSet<AsdHandle.Screen>
			) {
				screens.asReversed().forEach { it.close() }
				screens.clear()
				asdHandles.clear()
				screens.add(screen(handle))
			}
		}
	}

	sealed interface Handle {
		/**
		 * @see ScreenOperation.Exit
		 */
		fun exit(n: Int)

		/**
		 * @see ScreenOperation.Open
		 */
		fun open(screen: (Handle, AsdHandle.Screen, RenderSystem.Handle) -> Screen)

		/**
		 * @see ScreenOperation.Open
		 */
		fun open(screen: (Handle, AsdHandle.Screen, RenderSystem.Handle, InputStatesHandle) -> Screen)

		/**
		 * It is not recommended to use this in general scenarios.
		 * @see ScreenOperation.OpenBefore
		 */
		fun openBefore(target: Screen, screen: (Handle, AsdHandle.Screen, RenderSystem.Handle) -> Screen)

		/**
		 * It is not recommended to use this in general scenarios.
		 * @see ScreenOperation.OpenBefore
		 */
		fun openBefore(
			target: Screen,
			screen: (Handle, AsdHandle.Screen, RenderSystem.Handle, InputStatesHandle) -> Screen,
		)

		/**
		 * @see ScreenOperation.ExitTo
		 */
		fun exitTo(screen: Screen)

		/**
		 * @see ScreenOperation.Reset
		 */
		fun reset(screen: (Handle, AsdHandle.Screen, RenderSystem.Handle) -> Screen)

		/**
		 * @see ScreenOperation.Reset
		 */
		fun reset(screen: (Handle, AsdHandle.Screen, RenderSystem.Handle, InputStatesHandle) -> Screen)

		fun addMenu(menu: (MenuManager.Handle, AsdHandle) -> Menu)

		fun removeMenu(menu: Menu)
	}

	private inner class HandleImpl : Handle {
		override fun exit(n: Int) {
			screenOpQueue.add(ScreenOperation.Exit(n))
		}

		override fun open(screen: (Handle, AsdHandle.Screen, RenderSystem.Handle) -> Screen) {
			screenOpQueue.add(ScreenOperation.Open { screen(handle, ScreenAsdHandleImpl(), it) })
		}

		override fun open(screen: (Handle, AsdHandle.Screen, RenderSystem.Handle, InputStatesHandle) -> Screen) {
			screenOpQueue.add(ScreenOperation.Open { screen(handle, ScreenAsdHandleImpl(), it, inputStatesHandle) })
		}

		override fun openBefore(target: Screen, screen: (Handle, AsdHandle.Screen, RenderSystem.Handle) -> Screen) {
			screenOpQueue.add(ScreenOperation.OpenBefore(target) { screen(handle, ScreenAsdHandleImpl(), it) })
		}

		override fun openBefore(
			target: Screen,
			screen: (Handle, AsdHandle.Screen, RenderSystem.Handle, InputStatesHandle) -> Screen
		) {
			screenOpQueue.add(ScreenOperation.OpenBefore(target) {
				screen(handle, ScreenAsdHandleImpl(), it, inputStatesHandle)
			})
		}

		override fun exitTo(screen: Screen) {
			screenOpQueue.add(ScreenOperation.ExitTo(screen))
		}

		override fun reset(screen: (Handle, AsdHandle.Screen, RenderSystem.Handle) -> Screen) {
			screenOpQueue.add(ScreenOperation.Reset { screen(handle, ScreenAsdHandleImpl(), it) })
		}

		override fun reset(screen: (Handle, AsdHandle.Screen, RenderSystem.Handle, InputStatesHandle) -> Screen) {
			screenOpQueue.add(ScreenOperation.Reset { screen(handle, ScreenAsdHandleImpl(), it, inputStatesHandle) })
		}

		override fun addMenu(menu: (MenuManager.Handle, AsdHandle) -> Menu) = menuManager.handle.addMenu(menu)

		override fun removeMenu(menu: Menu) = menuManager.handle.removeMenu(menu)
	}

	private inner class ScreenAsdHandleImpl : AsdHandle.Screen() {
		init {
			asdHandles.add(this)
			properties.putProperty(BoundsProperty.KEY, BoundsProperty(viewportRect))
			properties.putProperty(RectangleProperty.KEY, RectangleProperty(viewportRect))
			observeRect {
				properties.putProperty(BoundsProperty.KEY, BoundsProperty(viewportRect))
				properties.putProperty(RectangleProperty.KEY, RectangleProperty(viewportRect))
			}
		}

		override var rect get() = viewportRect
			set(value) = throw UnsupportedOperationException() // should never be invoked

		override fun registerAsdProcessor(processor: AsdProcessor<*>) = asdManagerHandle.registerProcessor(processor)
	}

	/**
	 * MUI Interoperability Interface
	 */
	class MuiIoI internal constructor(
		val renderSystem: RenderSystem,
		val screenManager: ScreenManager,
		val inputSystem: InputSystem,
	)

	internal fun update(muiManager: MuiManager) {
		repeat(screenOpQueue.size) {
			screenOpQueue.removeFirst().apply(renderSystemHandle, screens, asdHandles)
		}
		val ioi = MuiIoI(muiManager.guiManager.renderSystem, this, muiManager.kuiManager.inputSystem)
		menuManager.update(ioi)
		screens.forEach { it.update(ioi) }
	}

	internal fun render(renderSystem: RenderSystem) {
		menuManager.render(renderSystem, this)
		screens.forEach { it.render(renderSystem, this) }
	}

	internal fun visitScreens() = AgimoTreeVisitor.ScreenTreeVisitor {
		screens.asSequence()
	}

	internal fun visitMenus() = menuManager.visit()
}
