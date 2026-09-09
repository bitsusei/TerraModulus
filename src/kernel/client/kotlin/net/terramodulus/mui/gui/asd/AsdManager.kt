/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.asd

import net.terramodulus.mui.gui.agim.AgimoTreeVisitor

// TODO Should ASD affect choices of Layout?
//   There are two proposals:
//   - Each Container manages a Layout, then the Layout is put into LayoutManager/LayoutViewport
//   - Each Container is totally managed by AsdManager except for Facets, but this rather complicated for ASD
internal class AsdManager internal constructor() {
	companion object {
		// TODO temporary demonstrative testing default
		internal fun default(): AsdManager = AsdManager()
	}

	private val processors = HashSet<AsdProcessor<*>>()

	internal fun registerProcessor(processor: AsdProcessor<*>) {
		processors.add(processor)
	}

	internal inner class AgimHandle {
		internal fun registerProcessor(processor: AsdProcessor<*>) = this@AsdManager.registerProcessor(processor)
	}

	internal fun process() {
		fun <T> process(processor: AsdProcessor<T>) = processor.processDefined(processor.produceDefined())
		processors.forEach { process(it) }
	}

	// TODO What should be the use of this
	private inner class TreeVisitor(visitorScreens: ScreenTreeVisitor, visitorMenus: MenuTreeVisitor) : AgimoTreeVisitor() {
		init {
			val root = RootNode(visitorScreens.visit(), visitorMenus.visit())
		}
	}
}
