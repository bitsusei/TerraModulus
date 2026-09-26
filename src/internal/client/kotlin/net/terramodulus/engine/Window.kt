/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec2.ImmVec2f
import net.terramodulus.engine.ferricia.Mui.dropSdlHandle
import net.terramodulus.engine.ferricia.Mui.dropWindowHandle
import net.terramodulus.engine.ferricia.Mui.getMousePos
import net.terramodulus.engine.ferricia.Mui.initSdlHandle
import net.terramodulus.engine.ferricia.Mui.initWindowHandle
import net.terramodulus.engine.ferricia.Mui.resizeGLViewport
import net.terramodulus.engine.ferricia.Mui.sdlPoll
import net.terramodulus.engine.ferricia.Mui.showWindow
import net.terramodulus.engine.ferricia.Mui.swapWindow
import java.io.Closeable

/**
 * Manages the SDL window instance and the underlying GL context.
 */
class Window(
	width: UInt,
	height: UInt,
) : Closeable {
	var width = width
		private set
	var height = height
		private set
	private val sdlHandle = initSdlHandle()
	private val windowHandle = initWindowHandle(sdlHandle) // TODO pass dimensions
	val canvas = Canvas(windowHandle)

	private val listeners = HashSet<(UInt, UInt) -> Unit>()

	fun addListener(listener: (UInt, UInt) -> Unit) = listeners.add(listener)
	fun removeListener(listener: (UInt, UInt) -> Unit) = listeners.remove(listener)

	fun sizeChanged(width: UInt, height: UInt) {
		this.width = width
		this.height = height
		canvas.resizeGLViewport()
		listeners.forEach { it(width, height) }
	}

	fun show() = showWindow(windowHandle)

	fun swap() = swapWindow(windowHandle)

	fun pollEvents() = sdlPoll(sdlHandle)

	fun getMousePos() = getMousePos(sdlHandle).let { ImmVec2f(it[0], height.toFloat() - it[1]) }

	override fun close() {
		dropWindowHandle(windowHandle)
		dropSdlHandle(sdlHandle)
	}
}
