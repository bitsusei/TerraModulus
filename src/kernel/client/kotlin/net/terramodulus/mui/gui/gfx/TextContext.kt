/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

import com.cout970.math.vec2.ImmVec2d
import com.cout970.math.vec2.Vec2d
import com.cout970.math.vec2.toImmVec2f
import com.cout970.math.vec4.Vec4i
import kotlin.properties.Delegates

class TextContext(private val renderSystemHandle: RenderSystem.Handle, private var config: Config) {
	private val context = renderSystemHandle.newTextRenderingContext(config.fontSize, config.lineHeight, config.color)
	private lateinit var size: Dimension2D
	private lateinit var pos: Vec2d

	data class Config(val fontSize: Float, val lineHeight: Float, val color: Vec4i)

	interface ConfigEnv {
		var fontSize: Float
		var lineHeight: Float
		var color: Vec4i
	}

	fun update(operation: ConfigEnv.() -> Unit) {
		object : ConfigEnv {
			var fontSizeChanged = false
			override var fontSize: Float by Delegates.observable(config.fontSize) { _, _, _ ->
				fontSizeChanged = true
			}
			var lineHeightChanged = false
			override var lineHeight: Float by Delegates.observable(config.lineHeight) { _, _, _ ->
				lineHeightChanged = true
			}
			var colorChanged = false
			override var color: Vec4i by Delegates.observable(config.color) { _, _, _ ->
				colorChanged = true
			}
		}.apply(operation).apply {
			if (fontSizeChanged || lineHeightChanged) context.setMetrics(fontSize, lineHeight)
			if (colorChanged) context.setColor(color)
			config = Config(fontSize, lineHeight, color)
		}
	}

	fun update(rect: RectangleD) {
		val prevSize = try { size } catch (_: UninitializedPropertyAccessException) { null }
		val prevPos = try { pos } catch (_: UninitializedPropertyAccessException) { null }
		if (prevSize == null || prevSize.width != rect.width || prevSize.height != rect.height)
			size = Dimension2D(rect.width, rect.height)
		if (prevPos == null || prevPos.x != rect.x || prevPos.y != rect.y)
			pos = ImmVec2d(rect.x, rect.y)
	}

	fun setText(text: String) = context.setText(text)

	fun render() = renderSystemHandle.renderText(context, pos.toImmVec2f())
}
