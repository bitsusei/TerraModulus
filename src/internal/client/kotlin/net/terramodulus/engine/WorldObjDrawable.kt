/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.quaternion.Quatd
import com.cout970.math.vec3.ImmVec3d
import com.cout970.math.vec3.Vec3d
import com.cout970.math.vec3.times
import com.cout970.math.vec4.Vec4i
import net.terramodulus.engine.ferricia.Gwr.newDrawableWorldObj
import net.terramodulus.engine.ferricia.Gwr.updateWorldObjModel

class WorldObjDrawable(
	private val geom: WorldObjGeom,
	rgba: Vec4i,
	private var pos: Vec3d,
	private var scale: Vec3d,
	private var rot: Quatd,
) {
	internal val handle = newDrawableWorldObj(geom.wideHandle, rgba.toArray())

	/**
	 * Point of center and total dimensions
	 */
	lateinit var aabb: Pair<Vec3d, Vec3d>

	private val observers = mutableSetOf<() -> Unit>()

	fun observeAabb(observer: () -> Unit) {
		observers.add(observer)
	}

	fun unobserveAabb(observer: () -> Unit) {
		assert(observers.remove(observer))
	}

	fun updateModel(pos: Vec3d, scale: Vec3d, rot: Quatd) {
		aabb = pos to (geom.geomDims * scale)
		observers.forEach { it() }
		updateWorldObjModel(handle, doubleArrayOf(
			pos.x,
			pos.y,
			pos.z,
			rot.w,
			rot.x,
			rot.y,
			rot.z,
			scale.x,
			scale.y,
			scale.z,
		))
	}

	init {
		updateModel(pos, scale, rot)
	}

	fun setPos(value: Vec3d) {
		pos = value
		updateModel(pos, scale, rot)
	}

	fun setScale(value: Vec3d) {
		scale = value
		updateModel(pos, scale, rot)
	}

	fun setRot(value: Quatd) {
		rot = value
		updateModel(pos, scale, rot)
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
sealed class WorldObjGeom(handles: ULongArray) {
	protected val handle = handles[0]
	internal val wideHandle = handles[1]

	internal abstract val geomDims: Vec3d
}

@OptIn(ExperimentalUnsignedTypes::class)
class SimpleMesh3dGeomCube(canvas: Canvas, width: Float) : WorldObjGeom(canvas.newMeshGeomCube(width)) {
	override val geomDims = ImmVec3d(width.toDouble())
}

@OptIn(ExperimentalUnsignedTypes::class)
class SimpleMesh3dGeomSphere(canvas: Canvas, radius: Float) : WorldObjGeom(canvas.newMeshGeomSphere(radius)) {
	override val geomDims = ImmVec3d(radius.toDouble() * 2)
}
