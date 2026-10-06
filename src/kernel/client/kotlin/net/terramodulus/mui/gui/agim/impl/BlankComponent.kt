/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.Dimension2F

/**
 * This can act as a placeholder [Component] in a [Layout][net.terramodulus.mui.gui.agim.Layout].
 */
class BlankComponent(asdHandle: AsdHandle) : Component(asdHandle) {
	override fun render(renderSystem: RenderSystem) {}
}
