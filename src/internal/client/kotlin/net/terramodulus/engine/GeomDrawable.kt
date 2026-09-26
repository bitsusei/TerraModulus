/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import net.terramodulus.engine.ferricia.Mui.newSimpleLineGeom
import net.terramodulus.engine.ferricia.Mui.newSimpleRectGeom

sealed class GeomDrawable(handle: ULong) : Drawable(handle) {
}

class SimpleLineGeom(canvas: Canvas, x0: Int, y0: Int, x1: Int, y1: Int, r: Int, g: Int, b: Int, a: Int) :
	GeomDrawable(canvas.newSimpleLineGeom(x0, y0, x1, y1, r, g, b, a))

class SimpleRectGeom(canvas: Canvas, x0: Int, y0: Int, x1: Int, y1: Int, r: Int, g: Int, b: Int, a: Int) :
	GeomDrawable(canvas.newSimpleRectGeom(x0, y0, x1, y1, r, g, b, a))
