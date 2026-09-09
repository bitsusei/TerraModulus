/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec2.Vec2f
import com.cout970.math.vec4.Vec4i

fun Vec4i.toArray() = intArrayOf(x, y, z, w)

fun Vec2f.toArray() = floatArrayOf(x, y)
