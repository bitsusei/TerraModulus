/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.GeneralTransform
import net.terramodulus.mui.gui.gfx.GuiGeometry
import net.terramodulus.mui.gui.gfx.RectStParams
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleI
import net.terramodulus.mui.gui.gfx.RenderSystem

class GeomComponent(
	val geom: GuiGeometry,
	private val bounds: RectangleD,
	asdHandle: AsdHandle,
) : Component(asdHandle) {
	private val transform = GeneralTransform().apply { geom.add(this) }

	init {
		val dim = IntrinsicDimensionsProperty(bounds.width.toUInt(), bounds.height.toUInt())
		asdHandle.properties.putProperty(IntrinsicDimensionsProperty.KEY, dim)
		asdHandle.properties.putProperty(IntrinsicRatioProperty.KEY, dim.computeRatio())
		asdHandle.observeRect {
			RectStParams.fromRects(bounds, asdHandle.rect).applyToGeneralTransform(transform)
		}
	}

	override fun render(renderSystem: RenderSystem) {
		geom.render(renderSystem)
	}
}
