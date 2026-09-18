/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

import net.terramodulus.engine.SpriteMesh

class GuiSprite(handle: RenderSystem.CanvasHandle, val rect: RectangleI, private val texture: UInt) {
	private val mesh = SpriteMesh(handle.canvas, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height)

	fun add(model: ModelTransform) = mesh.add(model)

	fun add(filter: ColorFilter) = mesh.add(filter)

	fun render(renderSystem: RenderSystem) = renderSystem.renderGuiTex(mesh, texture)
}
