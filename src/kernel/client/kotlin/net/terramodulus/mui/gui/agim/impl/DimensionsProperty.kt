/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.AgimoProperty
import net.terramodulus.mui.gui.agim.AgimoPropertyMap
import net.terramodulus.mui.gui.agim.LayoutHandle
import net.terramodulus.mui.gui.gfx.Dimension2D

data class DimensionsProperty(val value: Dimension2D) : AgimoProperty() {
	companion object {
		val KEY = AgimoPropertyMap.Key(DimensionsProperty::class.java)

		fun getOrComputeValue(map: LayoutHandle.Unit): Dimension2D {
			return map.getProperty(KEY)?.value ?: map.getProperty(IntrinsicDimensionsProperty.KEY)!!.let {
				Dimension2D(it.width.toDouble(), it.height.toDouble())
			}
		}

		fun getOrComputeRatio(map: LayoutHandle.Unit): Dimension2D {
			return map.getProperty(IntrinsicRatioProperty.KEY)?.let {
				Dimension2D(it.width.toDouble(), it.height.toDouble())
			} ?: map.getProperty(KEY)?.value ?: map.getProperty(IntrinsicDimensionsProperty.KEY)!!.let {
				Dimension2D(it.width.toDouble(), it.height.toDouble())
			}
		}
	}
}
