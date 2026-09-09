/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine.ferricia

@OptIn(ExperimentalUnsignedTypes::class)
internal object Gwr {
	/**
	 * @param windowHandle window handle pointer
	 * @param vsh source code of vector shader
	 * @param fsh source code of fragment shader
	 * @return GWR Geo Shader Program handle pointer
	 */
	@JvmName("geoShaders")
	external fun geoShaders(windowHandle: ULong, vsh: String, fsh: String): ULong

	/**
	 * @param canvasHandle Canvas handle pointer
	 * @param data `[x, y, z]`
	 * @return Camera3D handle pointer
	 */
	@JvmName("newCamera")
	external fun newCamera(canvasHandle: ULong, data: FloatArray): ULong

	/**
	 * @param cameraHandle Camera3D handle pointer
	 * @param data `[x, y, z]`
	 */
	@JvmName("refreshCameraPos")
	external fun refreshCameraPos(cameraHandle: ULong, data: FloatArray)

	/**
	 * @param cameraHandle Camera3D handle pointer
	 * @param data zoom level
	 */
	@JvmName("setCameraZoomLevel")
	external fun setCameraZoomLevel(cameraHandle: ULong, data: Float)

	/**
	 * @param windowHandle window handle pointer
	 * @param width cube's width, in `(0,2]`
	 * @return SimpleMesh3dGeom handle pointer and Render3dPrimitive (wide) handle pointer
	 */
	@JvmName("newMeshGeomCube")
	external fun newMeshGeomCube(windowHandle: ULong, width: Float): ULongArray

	/**
	 * @param windowHandle window handle pointer
	 * @param width cube's radius, in `(0,1]`
	 * @return SimpleMesh3dGeom handle pointer and Render3dPrimitive (wide) handle pointer
	 */
	@JvmName("newMeshGeomSphere")
	external fun newMeshGeomSphere(windowHandle: ULong, width: Float): ULongArray

	/**
	 * @param handle Render3dPrimitive (wide) handle pointer
	 * @param data `[r, g, b, a]`
	 * @return DrawableWorldObj handle pointer
	 */
	@JvmName("newDrawableWorldObj")
	external fun newDrawableWorldObj(handle: ULong, data: IntArray): ULong

	/**
	 * @param objHandle DrawableWorldObj handle pointer
	 * @param data `[px, py, pz, w, i, j, k, sx, sy, sz]`; position, quaternion and scaling
	 */
	@JvmName("updateWorldObjModel")
	external fun updateWorldObjModel(objHandle: ULong, data: DoubleArray)

	/**
	 * @param windowHandle window handle pointer
	 * @param canvasHandle Canvas handle pointer
	 * @param cameraHandle Camera3D handle pointer
	 * @param objHandle DrawableWorldObj handle pointer
	 * @param programHandle GWR Shader Program handle pointer
	 */
	@JvmName("drawGwrObj")
	external fun drawGwrObj(windowHandle: ULong, canvasHandle: ULong, cameraHandle: ULong, objHandle: ULong, programHandle: ULong)
}
