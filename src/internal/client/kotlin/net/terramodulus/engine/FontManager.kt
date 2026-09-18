/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec4.Vec4i
import net.terramodulus.engine.ferricia.Mui.newFontManager

class FontManager {
	internal val handle = newFontManager()

	internal fun newGlyphManager(windowHandle: ULong) = GlyphManager(handle, windowHandle)

	fun newTextRenderingManager(fontSize: Float, lineHeight: Float, color: Vec4i) =
		TextRenderingContext(handle, fontSize, lineHeight, color)
}
