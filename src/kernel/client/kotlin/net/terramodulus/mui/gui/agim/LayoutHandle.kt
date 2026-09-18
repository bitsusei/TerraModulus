/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.mui.gui.asd.AsdHandle

abstract class LayoutHandle internal constructor() {
	abstract fun getUnit(handle: AsdHandle): Unit

	abstract class Unit internal constructor() {
		abstract fun <T: AgimoProperty> getProperty(key: AgimoPropertyMap.Key<T>): T?
		abstract fun <T: AgimoProperty> containsProperty(key: AgimoPropertyMap.Key<T>): Boolean
	}
}
