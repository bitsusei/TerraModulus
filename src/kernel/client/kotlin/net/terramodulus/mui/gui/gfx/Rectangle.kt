/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

import com.cout970.math.vec2.ImmVec2d
import com.cout970.math.vec2.ImmVec2f
import com.cout970.math.vec2.ImmVec2i
import com.cout970.math.vec2.Vec2
import com.cout970.math.vec2.Vec2d
import com.cout970.math.vec2.Vec2f
import com.cout970.math.vec2.Vec2i

/**
 * Rectangle in a coordinate system with (0, 0) on the bottom left.
 * The anchor of the rectangle is the bottom-left corner.
 */
sealed class Rectangle<T: Rectangle<T, N, V, D>, N: Number, V: Vec2, D>(
	open val x: N,
	open val y: N,
	open val width: N,
	open val height: N,
) {
	companion object {
		fun withPoints(x0: Int, y0: Int, x1: Int, y1: Int): RectangleI {
			val minX: Int;
			val maxX: Int;
			if (x0 < x1) {
				minX = x0;
				maxX = x1;
			} else {
				maxX = x0;
				minX = x1;
			}
			val minY: Int;
			val maxY: Int;
			if (y0 < y1) {
				minY = y0;
				maxY = y1;
			} else {
				maxY = y0;
				minY = y1;
			}
			return RectangleI(minX, minY, maxX - minX, maxY - minY)
		}

		fun withPoints(x0: Float, y0: Float, x1: Float, y1: Float): RectangleF {
			val minX: Float;
			val maxX: Float;
			if (x0 < x1) {
				minX = x0;
				maxX = x1;
			} else {
				maxX = x0;
				minX = x1;
			}
			val minY: Float;
			val maxY: Float;
			if (y0 < y1) {
				minY = y0;
				maxY = y1;
			} else {
				maxY = y0;
				minY = y1;
			}
			return RectangleF(minX, maxX, minY, maxY)
		}

		fun withDirection(x: Int, y: Int, width: Int, height: Int, dir: Direction4AD) = when (dir) {
			Direction4AD.QuadOne -> RectangleI(x, y, width, height)
			Direction4AD.QuadTwo -> RectangleI(x - width, y, width, height)
			Direction4AD.QuadThree -> RectangleI(x - width, y - height, width, height)
			Direction4AD.QuadFour -> RectangleI(x, y - height, width, height)
		}

		fun withDirection(x: Float, y: Float, width: Float, height: Float, dir: Direction4AD) = when (dir) {
			Direction4AD.QuadOne -> RectangleF(x, y, width, height)
			Direction4AD.QuadTwo -> RectangleF(x - width, y, width, height)
			Direction4AD.QuadThree -> RectangleF(x - width, y - height, width, height)
			Direction4AD.QuadFour -> RectangleF(x, y - height, width, height)
		}
	}

	protected abstract fun constructor(x: N, y: N, width: N, height: N): T
	protected abstract fun vec2(x: N, y: N): V
	protected abstract operator fun N.plus(other: N): N
	protected abstract operator fun N.minus(other: N): N
	protected abstract operator fun N.div(other: Int): N
	protected abstract val V.x: N
	protected abstract val V.y: N

	abstract val size: D

	fun anchor(pos: Anchor5) = when (pos) {
		Anchor5.TopLeft -> vec2(x, y + width)
		Anchor5.TopRight -> vec2(x + width, y + height)
		Anchor5.BottomLeft -> vec2(x, y)
		Anchor5.BottomRight -> vec2(x + width, y)
		Anchor5.Center -> vec2(x + width / 2, y + height / 2)
	}

	fun translateBy(pos: V) = constructor(x + pos.x, y + pos.y, width, height)

	fun translateBy(x: N, y: N) = constructor(this.x + x, this.y + y, width, height)

	fun translateByY(y: N) = constructor(x, this.y + y, width, height)

	fun translateByX(x: N) = constructor(this.x + x, y, width, height)

	fun translateToY(y: N) = constructor(x, y, width, height)

	fun translateToX(x: N) = constructor(x, y, width, height)

	fun translateTo(pos: V) = constructor(pos.x, pos.y, width, height)

	fun translateTo(x: N, y: N) = constructor(x, y, width, height)

	/** Inflates the [Rectangle] with the [Insets] */
	operator fun plus(other: Insets<N>) = constructor(
		x - other.left,
		y - other.bottom,
		width + other.left + other.right,
		height + other.bottom + other.top,
	)

	/** Deflates the [Rectangle] with the [Insets] */
	operator fun minus(other: Insets<N>) = constructor(
		x + other.left,
		y + other.bottom,
		width - other.left - other.right,
		height - other.bottom - other.top,
	)

	abstract fun toInt(): RectangleI
	abstract fun toFloat(): RectangleF
	abstract fun toDouble(): RectangleD
}

data class RectangleI(
	override val x: Int,
	override val y: Int,
	override val width: Int,
	override val height: Int
) : Rectangle<RectangleI, Int, Vec2i, Dimension2I>(x, y, width, height) {
	override fun constructor(x: Int, y: Int, width: Int, height: Int) = RectangleI(x, y, width, height)

	override fun vec2(x: Int, y: Int) = ImmVec2i(x, y)

	override fun Int.plus(other: Int) = this + other
	override fun Int.minus(other: Int) = this - other
	override fun Int.div(other: Int) = this / other

	override val Vec2i.x: Int by ::x
	override val Vec2i.y: Int by ::y
	override val size = Dimension2I(width, height)

	override fun toInt() = this
	override fun toFloat() = RectangleF(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
	override fun toDouble() = RectangleD(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

data class RectangleF(
	override val x: Float,
	override val y: Float,
	override val width: Float,
	override val height: Float
) : Rectangle<RectangleF, Float, Vec2f, Dimension2F>(x, y, width, height) {
	override fun constructor(
		x: Float,
		y: Float,
		width: Float,
		height: Float
	) = RectangleF(x, y, width, height)

	override fun vec2(x: Float, y: Float) = ImmVec2f(x, y)

	override fun Float.plus(other: Float) = this + other
	override fun Float.minus(other: Float) = this - other
	override fun Float.div(other: Int) = this / other

	override val Vec2f.x: Float by ::x
	override val Vec2f.y: Float by ::y
	override val size = Dimension2F(width, height)

	override fun toInt() = RectangleI(x.toInt(), y.toInt(), width.toInt(), height.toInt())
	override fun toFloat() = this
	override fun toDouble() = RectangleD(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

data class RectangleD(
	override val x: Double,
	override val y: Double,
	override val width: Double,
	override val height: Double
) : Rectangle<RectangleD, Double, Vec2d, Dimension2D>(x, y, width, height) {
	override fun constructor(
		x: Double,
		y: Double,
		width: Double,
		height: Double
	) = RectangleD(x, y, width, height)

	override fun vec2(x: Double, y: Double) = ImmVec2d(x, y)

	override fun Double.plus(other: Double) = this + other
	override fun Double.minus(other: Double) = this - other
	override fun Double.div(other: Int) = this / other

	override val Vec2d.x: Double by ::x
	override val Vec2d.y: Double by ::y
	override val size = Dimension2D(width, height)

	override fun toInt() = RectangleI(x.toInt(), y.toInt(), width.toInt(), height.toInt())
	override fun toFloat() = RectangleF(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
	override fun toDouble() = this
}

@ExposedCopyVisibility
data class RectRange private constructor(val rect: RectangleD, internal val type: Type) {
	internal enum class Type { Inclusive, Range, Exclusive }
	companion object {
		fun inclusive(rect: RectangleD) = RectRange(rect, Type.Inclusive)
		fun exclusive(rect: RectangleD) = RectRange(rect, Type.Exclusive)

		/**
		 * For each axis, inclusive for the lower bound and exclusive for the upper bound.
		 */
		fun range(rect: RectangleD) = RectRange(rect, Type.Range)
	}

	fun contains(pt: Vec2d): Boolean {
		val lower = ImmVec2d(rect.x, rect.y)
		val upper = ImmVec2d(rect.x + rect.width, rect.y + rect.height)
		return when (type) {
			Type.Inclusive -> pt.x in lower.x..upper.x && pt.y in lower.y..upper.y
			Type.Exclusive -> pt.x > lower.x && pt.x < upper.x && pt.y > lower.y && pt.y < upper.y
			Type.Range -> pt.x in lower.x..<upper.x && pt.y in lower.y..<upper.y
		}
	}
}
