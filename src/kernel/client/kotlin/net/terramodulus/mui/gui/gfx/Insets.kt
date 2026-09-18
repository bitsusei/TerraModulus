/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

abstract class Insets<N: Number>(open val left: N, open val top: N, open val right: N, open val bottom: N) {

}

data class InsetsI(
	override val left: Int,
	override val top: Int,
	override val right: Int,
	override val bottom: Int,
) : Insets<Int>(left, top, right, bottom)

data class InsetsF(
	override val left: Float,
	override val top: Float,
	override val right: Float,
	override val bottom: Float,
) : Insets<Float>(left, top, right, bottom) {
	operator fun plus(other: InsetsF) = InsetsF(left + other.left, top + other.top, right + other.right, bottom + other.bottom)
}

data class InsetsD(
	override val left: Double,
	override val top: Double,
	override val right: Double,
	override val bottom: Double,
) : Insets<Double>(left, top, right, bottom) {
	operator fun plus(other: InsetsD) = InsetsD(left + other.left, top + other.top, right + other.right, bottom + other.bottom)
}
