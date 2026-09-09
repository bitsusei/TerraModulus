/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.AgimoProperty
import net.terramodulus.mui.gui.agim.AgimoPropertyMap
import net.terramodulus.util.gcd

/**
 * @constructor Simple constructor without any preprocessing
 */
data class IntrinsicRatioProperty(val width: UInt, val height: UInt) : AgimoProperty() {
	companion object {
		val KEY = AgimoPropertyMap.Key(IntrinsicRatioProperty::class.java)
		fun compute(width: UInt, height: UInt): IntrinsicRatioProperty {
			val gcd = gcd(width, height)
			return IntrinsicRatioProperty(width / gcd, height / gcd)
		}
	}
}

class IntrinsicDimensionsProperty(val width: UInt, val height: UInt) : AgimoProperty() {
	companion object {
		val KEY = AgimoPropertyMap.Key(IntrinsicDimensionsProperty::class.java)
	}
	fun computeRatio() = IntrinsicRatioProperty.compute(width, height)
}
