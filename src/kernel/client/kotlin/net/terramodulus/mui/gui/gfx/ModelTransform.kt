/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

import com.cout970.math.vec2.ImmVec2d
import net.terramodulus.engine.GeneralTransform
import net.terramodulus.engine.ModelTransform

typealias ModelTransform = ModelTransform

typealias GeneralTransform = GeneralTransform

/**
 * Scaling and Translation transform parameters (for SRT Transform) for a rectangle.
 */
data class RectStParams(val scaleX: Double, val scaleY: Double, val translateX: Double, val translateY: Double) {
	companion object {
		/**
		 * @param a source rectangle
		 * @param b target rectangle
		 */
		fun fromRects(a: RectangleD, b: RectangleD): RectStParams {
			val sx = b.width / a.width
			val sy = b.height / a.height
			val tx = b.x - a.x * sx
			val ty = b.y - a.y * sy
			return RectStParams(sx, sy, tx, ty)
		}
	}

	fun applyToGeneralTransform(generalTransform: GeneralTransform) {
		generalTransform.update {
			scale = ImmVec2d(scaleX, scaleY)
			angle = 0.0
			pos = ImmVec2d(translateX, translateY)
		}
	}
}
