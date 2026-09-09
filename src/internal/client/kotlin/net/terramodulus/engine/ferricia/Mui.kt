/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine.ferricia

import net.terramodulus.engine.MuiEvent

@OptIn(ExperimentalUnsignedTypes::class)
internal object Mui {
	/**
	 * @return SDL handle pointer
	 */
	@JvmName("initSdlHandle")
	external fun initSdlHandle(): ULong

	/**
	 * @param sdlHandle SDL handle pointer
	 */
	@JvmName("dropSdlHandle")
	external fun dropSdlHandle(sdlHandle: ULong)

	/**
	 * @param sdlHandle SDL handle pointer
	 * @return window handle pointer
	 */
	@JvmName("initWindowHandle")
	external fun initWindowHandle(sdlHandle: ULong): ULong

	/**
	 * @param windowHandle window handle pointer
	 */
	@JvmName("dropWindowHandle")
	external fun dropWindowHandle(windowHandle: ULong)

	/**
	 * @param windowHandle window handle pointer
	 */
	@JvmName("getGLVersion")
	external fun getGLVersion(windowHandle: ULong): String

	/**
	 * @param sdlHandle SDL handle pointer
	 * @return `[x, y]` in window coordinates
	 */
	@JvmName("getMousePos")
	external fun getMousePos(sdlHandle: ULong): FloatArray

	/**
	 * @param sdlHandle SDL handle pointer
	 * @return the list of all MUI events in this frame
	 */
	@JvmName("sdlPoll")
	external fun sdlPoll(sdlHandle: ULong): Array<MuiEvent>

	/**
	 * @param windowHandle window handle pointer
	 */
	@JvmName("resizeGLViewport")
	external fun resizeGLViewport(windowHandle: ULong, canvasHandle: ULong)

	/**
	 * @param windowHandle window handle pointer
	 */
	@JvmName("resizeGLViewportCamera")
	external fun resizeGLViewportCamera(windowHandle: ULong, canvasHandle: ULong, cameraHandle: ULong)

	/**
	 * @param windowHandle window handle pointer
	 */
	@JvmName("showWindow")
	external fun showWindow(windowHandle: ULong)

	/**
	 * @param windowHandle window handle pointer
	 */
	@JvmName("swapWindow")
	external fun swapWindow(windowHandle: ULong)

	/**
	 * @param windowHandle window handle pointer
	 * @return Canvas handle pointer
	 */
	@JvmName("initCanvasHandle")
	external fun initCanvasHandle(windowHandle: ULong): ULong

	/**
	 * @param canvasHandle Canvas handle pointer
	 */
	@JvmName("dropCanvasHandle")
	external fun dropCanvasHandle(canvasHandle: ULong)

	/**
	 * @param canvasHandle Canvas handle pointer
	 * @param data bytes of RGB image
	 * @return Texture ID
	 */
	@JvmName("loadImageToCanvas")
	external fun loadImageToCanvas(canvasHandle: ULong, data: ByteArray): UInt

	/**
	 * @param windowHandle window handle pointer
	 */
	@JvmName("clearCanvas")
	external fun clearCanvas(windowHandle: ULong)

	/**
	 * @param windowHandle window handle pointer
	 */
	@JvmName("setCanvasClearColor")
	external fun setCanvasClearColor(windowHandle: ULong, r: Float, g: Float, b: Float, a: Float)

	/**
	 * @param windowHandle window handle pointer
	 * @param vsh source code of vector shader
	 * @param fsh source code of fragment shader
	 * @return Geo Shader Program handle pointer
	 */
	@JvmName("geoShaders")
	external fun geoShaders(windowHandle: ULong, vsh: String, fsh: String): ULong

	/**
	 * @param windowHandle window handle pointer
	 * @param vsh source code of vector shader
	 * @param fsh source code of fragment shader
	 * @return Tex Shader Program handle pointer
	 */
	@JvmName("texShaders")
	external fun texShaders(windowHandle: ULong, vsh: String, fsh: String): ULong

	/**
	 * @param windowHandle window handle pointer
	 * @param data `[x0, y0, x1, y1, r, g, b, a]`
	 * @return SimpleLineGeom as DrawableSet handle pointer
	 */
	@JvmName("newSimpleLineGeom")
	external fun newSimpleLineGeom(windowHandle: ULong, data: IntArray): ULong

	/**
	 * @param windowHandle window handle pointer
	 * @param data `[x0, y0, x1, y1, r, g, b, a]`
	 * @return SimpleRectGeom as DrawableSet handle pointer
	 */
	@JvmName("newSimpleRectGeom")
	external fun newSimpleRectGeom(windowHandle: ULong, data: IntArray): ULong

	/**
	 * @param windowHandle window handle pointer
	 * @param data `[x0, y0, x1, y1]`
	 * @return SpriteMesh as DrawableSet handle pointer
	 */
	@JvmName("newSpriteMesh")
	external fun newSpriteMesh(windowHandle: ULong, data: IntArray): ULong

	/**
	 * @param handle DrawableSet handle pointer
	 * @param data must check Ferricia code for the exact implementation
	 */
	@JvmName("setGeomPos")
	external fun setGeomPos(handle: ULong, data: FloatArray)

	/**
	 * @param data `[sx, sy, angle, px, py]`; scaling, rotation, position
	 * @return GeneralTransform handle pointer and PrimModelTransform (wide) handle pointer
	 */
	@JvmName("modelGeneralTransform")
	external fun modelGeneralTransform(data: DoubleArray): ULongArray

	/**
	 * @param handle GeneralTransform thin pointer
	 * @param data `[sx, sy, angle, px, py]`; scaling, rotation, position
	 */
	@JvmName("updateGeneralTransform")
	external fun updateGeneralTransform(handle: ULong, data: DoubleArray)

	/**
	 * @param data alpha
	 * @return AlphaFilter handle pointer and PrimColorFilter (wide) handle pointer
	 */
	@JvmName("filterAlphaFilter")
	external fun filterAlphaFilter(data: Float): ULongArray

	/**
	 * @param filter AlphaFilter handle pointer
	 * @param data alpha
	 */
	@JvmName("editAlphaFilter")
	external fun editAlphaFilter(filter: ULong, data: Float)

	/**
	 * @param drawableHandle DrawableSet handle pointer
	 * @param modelHandle Model Transform wide handle pointer
	 */
	@JvmName("addModelTransform")
	external fun addModelTransform(drawableHandle: ULong, modelHandle: ULong)

	/**
	 * @param drawableHandle DrawableSet handle pointer
	 * @param modelHandle Model Transform wide handle pointer
	 */
	@JvmName("removeModelTransform")
	external fun removeModelTransform(drawableHandle: ULong, modelHandle: ULong)

	/**
	 * @param drawableHandle DrawableSet handle pointer
	 * @param filterHandle Color Filter wide handle pointer
	 */
	@JvmName("addColorFilter")
	external fun addColorFilter(drawableHandle: ULong, filterHandle: ULong)

	/**
	 * @param drawableHandle DrawableSet handle pointer
	 * @param filterHandle Color Filter wide handle pointer
	 */
	@JvmName("removeColorFilter")
	external fun removeColorFilter(drawableHandle: ULong, filterHandle: ULong)

	/**
	 * @param canvasHandle Canvas handle pointer
	 * @param drawableHandle DrawableSet handle pointer
	 * @param programHandle Geo Shader Program handle pointer
	 */
	@JvmName("drawGuiGeo")
	external fun drawGuiGeo(canvasHandle: ULong, drawableHandle: ULong, programHandle: ULong)

	/**
	 * @param canvasHandle Canvas handle pointer
	 * @param drawableHandle DrawableSet handle pointer
	 * @param programHandle Tex Shader Program handle pointer
	 */
	@JvmName("drawGuiTex")
	external fun drawGuiTex(canvasHandle: ULong, drawableHandle: ULong, programHandle: ULong, textureHandle: UInt)

	/**
	 * @return FontManager handle pointer
	 */
	@JvmName("newFontManager")
	external fun newFontManager(): ULong

	/**
	 * @param managerHandle FontManager handle pointer
	 * @param windowHandle Window handle pointer
	 * @return GlyphManager handle pointer
	 */
	@JvmName("newGlyphManager")
	external fun newGlyphManager(managerHandle: ULong, windowHandle: ULong): ULong

	/**
	 * @param windowHandle Window handle pointer
	 * @param vsh Window handle pointer
	 * @param fsh Window handle pointer
	 * @return TxtProgram handle pointer
	 */
	@JvmName("newTxtProgram")
	external fun newTxtProgram(windowHandle: ULong, vsh: String, fsh: String): ULong

	/**
	 * @param windowHandle Window handle pointer
	 * @param geoProgramHandle GeoProgram handle pointer
	 * @param txtProgramHandle TxtProgram handle pointer
	 * @return TextRenderer handle pointer
	 */
	@JvmName("newTextRenderer")
	external fun newTextRenderer(windowHandle: ULong, geoProgramHandle: ULong, txtProgramHandle: ULong): ULong

	/**
	 * @param managerHandle FontManager handle pointer
	 * @param data1 Font size and line height in pixels
	 * @param data2 `[r, g, b, a]` in [0,255]
	 * @return TextRenderingContext handle pointer
	 */
	@JvmName("newTextRenderingContext")
	external fun newTextRenderingContext(managerHandle: ULong, data1: FloatArray, data2: IntArray): ULong

	/**
	 * @param ctxHandle TextRenderingContext handle pointer
	 * @param data `[r, g, b, a]` in [0,255]
	 */
	@JvmName("setTextRenderingContextColor")
	external fun setTextRenderingContextColor(ctxHandle: ULong, data: IntArray)

	/**
	 * @param ctxHandle TextRenderingContext handle pointer
	 * @param data Font size and line height in pixels
	 */
	@JvmName("setTextRenderingContextMetrics")
	external fun setTextRenderingContextMetrics(ctxHandle: ULong, data: FloatArray)

	/**
	 * @param ctxHandle TextRenderingContext handle pointer
	 * @param data Width and height in pixels
	 */
	@JvmName("setTextRenderingContextSize")
	external fun setTextRenderingContextSize(ctxHandle: ULong, data: FloatArray)

	/**
	 * @param ctxHandle TextRenderingContext handle pointer
	 * @param text Contents of entire text widget
	 */
	@JvmName("setTextRenderingContextText")
	external fun setTextRenderingContextText(ctxHandle: ULong, text: String)

	/**
	 * @param canvasHandle Canvas handle pointer
	 * @param glyphMgrHandle GlyphManager handle pointer
	 * @param rendererHandle TextRenderer handle pointer
	 * @param fontMgrHandle FontManager handle pointer
	 * @param ctxHandle TextRenderingContext handle pointer
	 * @param data `[x, y]` Position
	 */
	@JvmName("renderText")
	external fun renderText(
		canvasHandle: ULong,
		glyphMgrHandle: ULong,
		rendererHandle: ULong,
		fontMgrHandle: ULong,
		ctxHandle: ULong,
		data: FloatArray,
	)
}
