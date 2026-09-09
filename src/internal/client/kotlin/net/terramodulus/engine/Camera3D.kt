/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import net.terramodulus.engine.ferricia.Gwr.newCamera
import net.terramodulus.engine.ferricia.Gwr.refreshCameraPos
import net.terramodulus.engine.ferricia.Gwr.setCameraZoomLevel
import java.io.Closeable
import kotlin.properties.Delegates

class Camera3D internal constructor(private val canvas: Canvas, pos: FloatArray) : Closeable {
	internal val handle = newCamera(canvas.handle, pos)

	fun loadGeoShaders(vsh: String, fsh: String) = canvas.load3DGeoShaders(vsh, fsh)

	fun refreshPos(pos: FloatArray) = refreshCameraPos(handle, pos)

	var zoomLevel: Float by Delegates.observable(1F) { _, _, new ->
		setCameraZoomLevel(handle, new)
	}

	fun renderGwrGeo(drawable: WorldObjDrawable, programHandle: ULong) =
		canvas.drawGwrObj(this, drawable, programHandle)

	override fun close() {
		canvas.camera3D = null
	}
}
