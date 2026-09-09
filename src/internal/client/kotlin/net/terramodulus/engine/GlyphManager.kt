/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import net.terramodulus.engine.ferricia.Mui.newGlyphManager

class GlyphManager internal constructor(managerHandle: ULong, windowHandle: ULong) {
	internal val handle = newGlyphManager(managerHandle, windowHandle)
}
