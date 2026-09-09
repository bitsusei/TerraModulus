/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.Container
import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.agim.LayoutComputationGroup
import net.terramodulus.mui.gui.agim.LayoutHandle
import net.terramodulus.mui.gui.asd.AsdHandle

class CompositeLayout(container: Container) : Layout(container) {
	private val layouts = ArrayDeque<Layout>()

	override val components = layouts.asSequence().flatMap { it.components }

	fun update(operation: ArrayDeque<Layout>.() -> Unit) {
		operate {
			operation(layouts)
			layouts.forEach { it.update() }
		}
	}

	override fun layOut(handle: LayoutHandle) = layouts.flatMap { it.layOutInternal(handle) }.asSequence()
}
