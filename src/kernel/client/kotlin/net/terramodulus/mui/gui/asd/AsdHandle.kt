/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.asd

import net.terramodulus.mui.gui.agim.AgimoProperty
import net.terramodulus.mui.gui.agim.AgimoPropertyMap
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleF
import net.terramodulus.util.TypedAnchorMap
import java.util.function.BiFunction

/**
 * Since rectangles are modified only during layout processing,
 * further follow-ups must not be deferred to next tick.
 */
abstract class AsdHandle internal constructor() {
	/**
	 * Caveat: Must only be modified by [Layout][net.terramodulus.mui.gui.agim.Layout].
	 * When modified, [triggerRectObservers] must be invoked.
	 */
	abstract var rect: RectangleD
		internal set

	protected val rectObservers = LinkedHashSet<() -> Unit>()

	internal fun observeRect(observer: () -> Unit) {
		rectObservers.add(observer)
	}

	internal fun unobserveRect(observer: () -> Unit) {
		rectObservers.remove(observer)
	}

	internal fun triggerRectObservers() {
		rectObservers.forEach { it() }
	}

	/**
	 * Permanent AGIMO Properties
	 */
	val properties = AgimoPropertyMap()

	/**
	 * Registers ASD Processors from AGIMOs to [AsdManager].
	 */
	abstract fun registerAsdProcessor(processor: AsdProcessor<*>)

	// TODO likely those below are useless
	abstract class Container : AsdHandle() {}

	abstract class Menu : Container() {}

	abstract class Screen : Container() {}

// 	interface Component : LayoutHandle {} // Do we need this?
}
