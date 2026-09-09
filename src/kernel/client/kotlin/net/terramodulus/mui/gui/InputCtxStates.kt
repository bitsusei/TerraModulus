/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui

import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.RectRange
import java.io.Closeable

sealed class InputCtxStates<S : InputState, K : Any>(
	protected val globalStates: InputGlobalStates<S, K>,
	protected val asdHandle: AsdHandle,
) : Closeable {
	protected val listeners = mutableSetOf<InputState.Listener<S, K>>()
	val ctxRange: CtxRange by lazy { CtxRange() }

	inner class CtxRange : Closeable {
		lateinit var rect: RectRange
			private set
		private val listener = {
			rect = RectRange.range(asdHandle.rect)
		}.also(asdHandle::observeRect)

		init {
			try {
				rect = RectRange.range(asdHandle.rect)
			} catch (_: UninitializedPropertyAccessException) {}
		}

		override fun close() {
			asdHandle.unobserveRect(listener)
		}
	}

	fun addListener(listener: InputState.Listener<S, K>) {
		listeners.add(listener)
		globalStates.addListener(listener)
	}

	fun removeListener(listener: InputState.Listener<S, K>) {
		listeners.remove(listener)
		globalStates.removeListener(listener)
	}

	override fun close() {
		listeners.forEach { globalStates.removeListener(it) }
	}
}
