/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import net.terramodulus.engine.ferricia.Gwr
import net.terramodulus.engine.ferricia.Gwr.drawGwrObj
import net.terramodulus.engine.ferricia.Gwr.newMeshGeomCube
import net.terramodulus.engine.ferricia.Gwr.newMeshGeomSphere
import net.terramodulus.engine.ferricia.Mui
import net.terramodulus.engine.ferricia.Mui.clearCanvas
import net.terramodulus.engine.ferricia.Mui.drawGuiGeo
import net.terramodulus.engine.ferricia.Mui.drawGuiTex
import net.terramodulus.engine.ferricia.Mui.dropCanvasHandle
import net.terramodulus.engine.ferricia.Mui.geoShaders
import net.terramodulus.engine.ferricia.Mui.getGLVersion
import net.terramodulus.engine.ferricia.Mui.initCanvasHandle
import net.terramodulus.engine.ferricia.Mui.loadImageToCanvas
import net.terramodulus.engine.ferricia.Mui.newSimpleLineGeom
import net.terramodulus.engine.ferricia.Mui.newSimpleRectGeom
import net.terramodulus.engine.ferricia.Mui.newSpriteMesh
import net.terramodulus.engine.ferricia.Mui.newTxtProgram
import net.terramodulus.engine.ferricia.Mui.setCanvasClearColor
import net.terramodulus.engine.ferricia.Mui.texShaders
import java.io.Closeable

/**
 * Manages the OpenGL viewport rendering as a "**canvas**"; managed by the GL context.
 *
 * This manages GL viewport in the SDL window and rendering in the viewport.
 */
@OptIn(ExperimentalUnsignedTypes::class)
class Canvas internal constructor(private val windowHandle: ULong) : Closeable {
	internal val handle = initCanvasHandle(windowHandle)
	val glVersion = getGLVersion(windowHandle)
	internal var camera3D: Camera3D? = null;

	fun clear() = clearCanvas(windowHandle)

	fun setClearColor(r: Float, g: Float, b: Float, a: Float) = setCanvasClearColor(windowHandle, r, g, b, a)

	internal fun resizeGLViewport() = if (camera3D == null) {
		Mui.resizeGLViewport(windowHandle, handle)
	} else {
		Mui.resizeGLViewportCamera(windowHandle, handle, camera3D!!.handle)
	}

	fun createCamera(pos: FloatArray): Camera3D {
		camera3D = Camera3D(this, pos)
		return camera3D!!
	}

	fun newGlyphManager(manager: FontManager) = manager.newGlyphManager(windowHandle)

	fun loadImage(data: ByteArray) = loadImageToCanvas(handle, data)

	fun loadGeoShaders(vsh: String, fsh: String) = geoShaders(windowHandle, vsh, fsh)

	fun load3DGeoShaders(vsh: String, fsh: String) = Gwr.geoShaders(windowHandle, vsh, fsh)

	fun loadTexShaders(vsh: String, fsh: String) = texShaders(windowHandle, vsh, fsh)

	fun loadTxtShaders(vsh: String, fsh: String) = newTxtProgram(windowHandle, vsh, fsh)

	fun newTextRenderer(geoProgramHandle: ULong, txtProgramHandle: ULong) =
		TextRenderer(windowHandle, geoProgramHandle, txtProgramHandle)

	internal fun newSimpleLineGeom(x0: Int, y0: Int, x1: Int, y1: Int, r: Int, g: Int, b: Int, a: Int) =
		newSimpleLineGeom(windowHandle, intArrayOf(x0, y0, x1, y1, r, g, b, a))

	internal fun newSimpleRectGeom(x0: Int, y0: Int, x1: Int, y1: Int, r: Int, g: Int, b: Int, a: Int) =
		newSimpleRectGeom(windowHandle, intArrayOf(x0, y0, x1, y1, r, g, b, a))

	internal fun newSpriteMesh(x0: Int, y0: Int, x1: Int, y1: Int) =
		newSpriteMesh(windowHandle, intArrayOf(x0, y0, x1, y1))

	internal fun newMeshGeomCube(width: Float) = newMeshGeomCube(windowHandle, width)

	internal fun newMeshGeomSphere(radius: Float) = newMeshGeomSphere(windowHandle, radius)

	fun renderGuiGeo(drawable: GeomDrawable, programHandle: ULong) =
		drawGuiGeo(handle, drawable.handle, programHandle)

	fun renderGuiTex(drawable: MeshDrawable, programHandle: ULong, textureHandle: UInt) =
		drawGuiTex(handle, drawable.handle, programHandle, textureHandle)

	internal fun drawGwrObj(camera3D: Camera3D, drawable: WorldObjDrawable, programHandle: ULong) =
		drawGwrObj(windowHandle, handle, camera3D.handle, drawable.handle, programHandle)

	override fun close() {
		dropCanvasHandle(handle)
	}
}
