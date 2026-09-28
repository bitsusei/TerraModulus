/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec2.ImmVec2d
import com.cout970.math.vec3.ImmVec3d
import com.cout970.math.vec3.Vec3d
import com.cout970.math.vec3.Vec3i
import net.terramodulus.engine.common.toArray
import net.terramodulus.engine.ferricia.Gwr.dropLightSpace
import net.terramodulus.engine.ferricia.Gwr.getCameraSpace
import net.terramodulus.engine.ferricia.Gwr.getLightSpaceAabb
import net.terramodulus.engine.ferricia.Gwr.newCamera
import net.terramodulus.engine.ferricia.Gwr.newLightSpace
import net.terramodulus.engine.ferricia.Gwr.refreshCameraPos
import net.terramodulus.engine.ferricia.Gwr.setCameraSpace
import net.terramodulus.engine.ferricia.Gwr.setCameraZoomLevel
import java.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.Delegates

class Camera3D internal constructor(private val canvas: Canvas, pos: FloatArray) : Closeable {
	internal val handle = newCamera(canvas.handle, pos)

	fun loadGeoShaders(vsh: String, fsh: String) = canvas.load3DGeoShaders(vsh, fsh)

	fun loadSdwShaders(vsh: String, fsh: String) = canvas.load3DSdwShaders(vsh, fsh)

	fun getSpace() = getCameraSpace(handle).let { ImmVec2d(it[0], it[1]) }

	fun refreshPos(pos: FloatArray) = refreshCameraPos(handle, pos)

	var zoomLevel: Float by Delegates.observable(1F) { _, _, new ->
		setCameraZoomLevel(handle, new)
	}

	var ceilLevel by Delegates.notNull<Double>()
		private set
	var floorLevel by Delegates.notNull<Double>()
		private set

	fun setCameraSpace(
		ceilLevel: Double,
		floorLevel: Double,
		nearThreshold: Float,
		farThreshold: Float,
		fogColor: Vec3i,
	) {
		this.ceilLevel = ceilLevel
		this.floorLevel = floorLevel
		setCameraSpace(
			handle,
			doubleArrayOf(ceilLevel, floorLevel),
			floatArrayOf(nearThreshold, farThreshold),
			fogColor.toArray(),
		)
	}

	fun renderGwrGeo(drawable: WorldObjDrawable, space: LightSpace, programHandle: ULong) =
		canvas.drawGwrObj(this, drawable, space, programHandle)

	fun renderGwrShadow(drawable: WorldObjDrawable, space: LightSpace, programHandle: ULong) =
		canvas.drawGwrShadow(this, drawable, space, programHandle)

	class LightSpace(min: Vec3d, max: Vec3d) : AutoCloseable {
		internal val handle = newLightSpace(doubleArrayOf(min.x, min.y, min.z, max.x, max.y, max.z))

		val aabb get() = getLightSpaceAabb(handle).let {
			ImmVec3d(it[0], it[1], it[2]) to ImmVec3d(it[3], it[4], it[5])
		}

		override fun close() {
			dropLightSpace(handle)
		}
	}

	@OptIn(ExperimentalContracts::class)
	fun withShadowRendering(block: () -> Unit) {
		contract {
			callsInPlace(block, InvocationKind.EXACTLY_ONCE)
		}

		canvas.startShadowRendering(this)
		block()
		canvas.endShadowRendering(this)
	}

	override fun close() {
		canvas.camera3D = null
	}
}
