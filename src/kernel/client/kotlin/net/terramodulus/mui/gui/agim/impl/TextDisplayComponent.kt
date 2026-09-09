/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.gfx.TextContext
import net.terramodulus.util.lateInitObservable

class TextDisplayComponent(
	asdHandle: AsdHandle,
	renderSystemHandle: RenderSystem.Handle,
	config: TextContext.Config,
) : Component(asdHandle) {
	private val context = TextContext(renderSystemHandle, config)
	var text: String by lateInitObservable { _, _, new ->
		context.setText(new)
	}

	init {
		asdHandle.observeRect {
			context.update(asdHandle.rect)
			println(asdHandle.rect)
		}
	}

	fun update(operation: TextContext.ConfigEnv.() -> Unit) = context.update(operation)

	override fun render(renderSystem: RenderSystem) {
		context.render()
	}
}
