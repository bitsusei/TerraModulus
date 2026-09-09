/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

import com.cout970.math.vec2.Vec2f
import com.cout970.math.vec3.Vec3f
import com.cout970.math.vec4.Vec4i
import net.terramodulus.core.TerraModulus
import net.terramodulus.core.getResourceAsBytes
import net.terramodulus.core.getResourceAsString
import net.terramodulus.engine.Canvas
import net.terramodulus.engine.FontManager
import net.terramodulus.engine.GeomDrawable
import net.terramodulus.engine.MeshDrawable
import net.terramodulus.engine.TextRenderingContext
import net.terramodulus.mui.gui.InputStatesHandle
import net.terramodulus.mui.gui.agim.ScreenManager
import net.terramodulus.mui.gui.agim.impl.GameplayScreen
import net.terramodulus.mui.gui.asd.AsdHandle

class RenderSystem internal constructor(private val core: TerraModulus, private val canvas: Canvas) {
	val handle: Handle = HandleImpl()
	// In production, initialization of fonts shall be deferred to session of resource loading.
	private val fontManager = FontManager()
	private val glyphManager = canvas.newGlyphManager(fontManager)
	private val texShaders = canvas.loadTexShaders(
		getResourceAsString("/gms_tex.vsh"),
		getResourceAsString("/gms_tex.fsh")
	)
	private val geoShaders = canvas.loadGeoShaders(
		getResourceAsString("/gms_geo.vsh"),
		getResourceAsString("/gms_geo.fsh")
	)
	private val txtShaders = canvas.loadTxtShaders(
		getResourceAsString("/gms_tex.vsh"),
		getResourceAsString("/gms_txt.fsh")
	)
	private val textRenderer = canvas.newTextRenderer(geoShaders, txtShaders)
	val targetFps = 1000;

	sealed interface Handle {
		val canvasHandle: CanvasHandle

		fun loadTexture(path: String): UInt

		fun setBackgroundColor(red: Float, green: Float, blue: Float, alpha: Float)

		fun renderText(ctx: TextRenderingContext, pos: Vec2f)

		fun newTextRenderingContext(fontSize: Float, lineHeight: Float, color: Vec4i): TextRenderingContext
	}

	inner class CanvasHandle internal constructor() {
		internal val canvas = this@RenderSystem.canvas
	}

	private inner class HandleImpl : Handle {
		override val canvasHandle = CanvasHandle()

		override fun loadTexture(path: String) = canvas.loadImage(getResourceAsBytes(path))

		override fun setBackgroundColor(red: Float, green: Float, blue: Float, alpha: Float) {
			canvas.setClearColor(red, green, blue, alpha)
		}

		override fun renderText(ctx: TextRenderingContext, pos: Vec2f) {
			textRenderer.renderText(ctx, canvas, glyphManager, fontManager, pos)
		}

		override fun newTextRenderingContext(fontSize: Float, lineHeight: Float, color: Vec4i) =
			fontManager.newTextRenderingManager(fontSize, lineHeight, color)
	}

	internal fun newGameplayScreen(pos: Vec3f) =
		{ mh: ScreenManager.Handle, ah: AsdHandle.Container, it: Handle, ish: InputStatesHandle ->
			GameplayScreen(core, canvas.createCamera(floatArrayOf(pos.x, pos.y, pos.z)), it, mh, ah, ish)
		}

	internal fun renderGuiTex(drawable: MeshDrawable, texture: UInt) = canvas.renderGuiTex(drawable, texShaders, texture)

	internal fun renderGuiGeo(drawable: GeomDrawable) = canvas.renderGuiGeo(drawable, geoShaders)

	internal fun render() {

	}
}
