/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec2.ImmVec2d
import com.cout970.math.vec2.MutVec2d
import com.cout970.math.vec2.Vec2d
import net.terramodulus.engine.ferricia.Mui.modelGeneralTransform
import net.terramodulus.engine.ferricia.Mui.updateGeneralTransform
import java.io.Closeable
import kotlin.properties.Delegates

@OptIn(ExperimentalUnsignedTypes::class)
sealed class ModelTransform(handles: ULongArray) : Closeable {
	internal val handle: ULong = handles[0]
	internal val wideHandle: ULong = handles[1]

	override fun close() {
		TODO("Not yet implemented")
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
class GeneralTransform(sx: Double, sy: Double, angle: Double, px: Double, py: Double) :
	ModelTransform(modelGeneralTransform(doubleArrayOf(sx, sy, angle, px, py))) {
	constructor() : this(1.0, 1.0, 0.0, 0.0, 0.0)

	var scale: Vec2d = ImmVec2d(sx, sy)
		private set
	var angle: Double = angle
		private set
	var pos: Vec2d = ImmVec2d(px, py)
		private set

	interface Op {
		var scale: Vec2d
		var angle: Double
		var pos: Vec2d
	}

	fun update(operation: Op.() -> Unit) {
		operation(object : Op {
			override var scale: Vec2d by this@GeneralTransform::scale
			override var angle: Double by this@GeneralTransform::angle
			override var pos: Vec2d by this@GeneralTransform::pos
		})
		updateGeneralTransform(handle, doubleArrayOf(scale.x, scale.y, angle, pos.x, pos.y))
	}
}
