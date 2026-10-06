/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.AgimoProperty
import net.terramodulus.mui.gui.agim.AgimoPropertyMap
import net.terramodulus.mui.gui.gfx.RectangleD

data class RectangleProperty(val value: RectangleD) : AgimoProperty() {
	companion object {
		val KEY = AgimoPropertyMap.Key(RectangleProperty::class.java)
	}
}
