/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

import com.cout970.math.vec3.ImmVec3d
import com.cout970.math.vec3.Vec3d

data class Cuboid(val pt: Vec3d, val dims: Dimension3D) {
	fun max() = ImmVec3d(pt.x + dims.x, pt.y + dims.y, pt.z + dims.z)

	fun center() = ImmVec3d(pt.x + dims.x / 2, pt.y + dims.y / 2, pt.z + dims.z / 2)
}
