/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec2.ImmVec2d
import com.cout970.math.vec3.Vec3i
import net.terramodulus.engine.common.toArray
import net.terramodulus.engine.ferricia.Gwr.getCameraSpace
import net.terramodulus.engine.ferricia.Gwr.newCamera
import net.terramodulus.engine.ferricia.Gwr.refreshCameraPos
import net.terramodulus.engine.ferricia.Gwr.setCameraSpace
import net.terramodulus.engine.ferricia.Gwr.setCameraZoomLevel
import java.io.Closeable
import kotlin.properties.Delegates

class Camera3D internal constructor(private val canvas: Canvas, pos: FloatArray) : Closeable {
	internal val handle = newCamera(canvas.handle, pos)

	fun loadGeoShaders(vsh: String, fsh: String) = canvas.load3DGeoShaders(vsh, fsh)

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

	fun renderGwrGeo(drawable: WorldObjDrawable, programHandle: ULong) =
		canvas.drawGwrObj(this, drawable, programHandle)

	override fun close() {
		canvas.camera3D = null
	}
}
