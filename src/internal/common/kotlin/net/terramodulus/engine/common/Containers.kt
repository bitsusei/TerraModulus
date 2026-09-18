/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine.common

import com.cout970.math.vec3.ImmVec3d
import com.cout970.math.vec3.ImmVec3f
import com.cout970.math.vec3.Vec3d

/**
 * @throws ArrayIndexOutOfBoundsException if [array]'s size < 3
 */
fun ImmVec3dFromArray(array: DoubleArray) = ImmVec3d(array[0], array[1], array[2])

fun Vec3d.toArray() = doubleArrayOf(x, y, z)

val ZeroImmVec3d = ImmVec3d(0.0)
val ZeroImmVec3f = ImmVec3f(0F)
