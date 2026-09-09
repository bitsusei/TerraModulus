/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.AbstractPane
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.ColorFilter
import net.terramodulus.mui.gui.gfx.GeneralTransform
import net.terramodulus.mui.gui.gfx.GuiGeometry
import net.terramodulus.mui.gui.gfx.GuiSprite
import net.terramodulus.mui.gui.gfx.ModelTransform
import net.terramodulus.mui.gui.gfx.RectStParams
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RenderSystem

sealed interface GraphicsComponent

class SpriteComponent(val sprite: GuiSprite, asdHandle: AsdHandle) : Component(asdHandle), GraphicsComponent {
	private val transform = GeneralTransform().apply { sprite.add(this) }

	init {
		val dim = IntrinsicDimensionsProperty(sprite.rect.width.toUInt(), sprite.rect.height.toUInt())
		asdHandle.properties.putProperty(IntrinsicDimensionsProperty.KEY, dim)
		asdHandle.properties.putProperty(IntrinsicRatioProperty.KEY, dim.computeRatio())
		asdHandle.observeRect {
			RectStParams.fromRects(sprite.rect.toDouble(), asdHandle.rect).applyToGeneralTransform(transform)
		}
	}

	override fun render(renderSystem: RenderSystem) {
		sprite.render(renderSystem)
	}
}

class DrawablesComponent(
	val drawables: Sequence<Drawable>,
	private val bounds: RectangleD,
	asdHandle: AsdHandle,
) : Component(asdHandle) {
	private val transform = GeneralTransform().apply { addTransform(this) }

	fun addTransform(transform: ModelTransform) = drawables.forEach { drawable ->
		when (drawable) {
			is Drawable.Geom -> drawable.geom.add(transform)
			is Drawable.Sprite -> drawable.sprite.add(transform)
		}
	}

	fun addFilter(filter: ColorFilter) = drawables.forEach { drawable ->
		when (drawable) {
			is Drawable.Geom -> drawable.geom.add(filter)
			is Drawable.Sprite -> drawable.sprite.add(filter)
		}
	}

	sealed interface Drawable {
		companion object {
			operator fun invoke(geom: GuiGeometry) = Geom(geom)
			operator fun invoke(sprite: GuiSprite) = Sprite(sprite)
		}

		class Geom(val geom: GuiGeometry) : Drawable
		class Sprite(val sprite: GuiSprite) : Drawable
	}

	init {
		val dim = IntrinsicDimensionsProperty(bounds.width.toUInt(), bounds.height.toUInt())
		asdHandle.properties.putProperty(IntrinsicDimensionsProperty.KEY, dim)
		asdHandle.properties.putProperty(IntrinsicRatioProperty.KEY, dim.computeRatio())
		asdHandle.observeRect {
			RectStParams.fromRects(bounds, asdHandle.rect).applyToGeneralTransform(transform)
		}
	}

	override fun render(renderSystem: RenderSystem) {
		drawables.forEach { drawable ->
			when (drawable) {
				is Drawable.Geom -> drawable.geom.render(renderSystem)
				is Drawable.Sprite -> drawable.sprite.render(renderSystem)
			}
		}
	}
}

@Suppress("CanSealedSubClassBeObject")
class CanvasComponent(override val layout: Layout, asdHandle: AsdHandle) : AbstractPane(asdHandle), GraphicsComponent {
	override fun render(renderSystem: RenderSystem) {
		TODO("Not yet implemented")
	}
}
