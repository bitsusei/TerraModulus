/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec2.Vec2f
import net.terramodulus.engine.ferricia.Mui.newTextRenderer

class TextRenderer internal constructor(windowHandle: ULong, geoProgramHandle: ULong, txtProgramHandle: ULong) {
	private val handle = newTextRenderer(windowHandle, geoProgramHandle, txtProgramHandle)

	fun renderText(
		ctx: TextRenderingContext,
		canvas: Canvas,
		glyphManager: GlyphManager,
		fontManager: FontManager,
		pos: Vec2f,
	) = ctx.render(canvas.handle, glyphManager.handle, handle, fontManager.handle, pos)
}
