/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

data class Dimension2I(val width: Int, val height: Int)

data class Dimension2F(val width: Float, val height: Float)

data class Dimension2D(val width: Double, val height: Double)

data class Dimension3I(val width: Int, val height: Int, val depth: Int) {
	val x get() = width
	val y get() = height
	val z get() = depth
}

data class Dimension3F(val width: Float, val height: Float, val depth: Float) {
	val x get() = width
	val y get() = height
	val z get() = depth
}

data class Dimension3D(val width: Double, val height: Double, val depth: Double) {
	val x get() = width
	val y get() = height
	val z get() = depth
}
