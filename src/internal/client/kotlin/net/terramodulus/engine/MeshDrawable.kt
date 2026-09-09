/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import net.terramodulus.engine.ferricia.Mui.newSpriteMesh

sealed class MeshDrawable(handle: ULong) : Drawable(handle) {
}

class SpriteMesh(canvas: Canvas, x0: Int, y0: Int, x1: Int, y1: Int) :
	MeshDrawable(canvas.newSpriteMesh(x0, y0, x1, y1)) {

}
