/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec3.Vec3d
import net.terramodulus.engine.ferricia.Mui.dropCameraSpace
import net.terramodulus.engine.ferricia.Mui.intersectCameraSpace
import net.terramodulus.engine.ferricia.Mui.newCameraSpace
import java.io.Closeable

class CameraSpace(center: Vec3d, dims: Vec3d) : Closeable {
	private val handle = newCameraSpace(doubleArrayOf(center.x, center.y, center.z, dims.x, dims.y, dims.z))

	fun intersects(min: Vec3d, max: Vec3d) =
		intersectCameraSpace(handle, doubleArrayOf(min.x, min.y, min.z, max.x, max.y, max.z))

	override fun close() = dropCameraSpace(handle)
}
