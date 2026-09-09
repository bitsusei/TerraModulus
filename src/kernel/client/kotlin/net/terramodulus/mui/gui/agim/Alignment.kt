/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import com.cout970.math.vec2.Vec2d
import com.cout970.math.vec2.minus
import com.cout970.math.vec2.plus
import net.terramodulus.mui.gui.gfx.Anchor5
import net.terramodulus.mui.gui.gfx.Dimension2D
import net.terramodulus.mui.gui.gfx.RectangleD

const val ALIGN_START = 0.0
const val ALIGN_CENTER = 0.5
const val ALIGN_END = 1.0

/**
 * `anchor` is relative position of positioning anchor from the anchor of the rectangle/dimension.;
 * should be within the bounds of the rectangle/dimension.
 */
class AnchorAlignmentHelper {
	data class Subject(val rect: RectangleD, val anchor: Vec2d) {
		fun alignTarget(target: Target): RectangleD {
			// This should be the anchor of the rectangle of target
			val anchor = rect.anchor(Anchor5.BottomLeft) + anchor - target.anchor
			return RectangleD(anchor.x, anchor.y, target.dim.width, target.dim.height)
		}
	}

	data class Target(val dim: Dimension2D, val anchor: Vec2d)
}
