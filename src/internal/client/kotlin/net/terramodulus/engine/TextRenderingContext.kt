/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec2.Vec2f
import com.cout970.math.vec4.Vec4i
import net.terramodulus.engine.ferricia.Mui.newTextRenderingContext
import net.terramodulus.engine.ferricia.Mui.renderText
import net.terramodulus.engine.ferricia.Mui.setTextRenderingContextColor
import net.terramodulus.engine.ferricia.Mui.setTextRenderingContextMetrics
import net.terramodulus.engine.ferricia.Mui.setTextRenderingContextSize
import net.terramodulus.engine.ferricia.Mui.setTextRenderingContextText

class TextRenderingContext internal constructor(
	managerHandle: ULong,
	fontSize: Float,
	lineHeight: Float,
	color: Vec4i,
) {
	private val handle = newTextRenderingContext(managerHandle, floatArrayOf(fontSize, lineHeight), color.toArray())

	fun setColor(color: Vec4i) = setTextRenderingContextColor(handle, color.toArray())

	fun setMetrics(fontSize: Float, lineHeight: Float,) =
		setTextRenderingContextMetrics(handle, floatArrayOf(fontSize, lineHeight))

	fun setSize(width: Float, height: Float) = setTextRenderingContextSize(handle, floatArrayOf(width, height))

	fun setText(text: String) = setTextRenderingContextText(handle, text)

	internal fun render(
		canvasHandle: ULong,
		glyphMgrHandle: ULong,
		rendererHandle: ULong,
		fontMgrHandle: ULong,
		pos: Vec2f,
	) = renderText(canvasHandle, glyphMgrHandle, rendererHandle, fontMgrHandle, handle, pos.toArray())
}
