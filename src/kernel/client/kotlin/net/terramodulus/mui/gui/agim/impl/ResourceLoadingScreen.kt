/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.engine.common.ZeroImmVec3f
import net.terramodulus.mui.gui.agim.Screen
import net.terramodulus.mui.gui.agim.ScreenManager
import net.terramodulus.mui.gui.agim.event.ScreenEvent
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.AlphaFilter
import net.terramodulus.mui.gui.gfx.Dimension2I
import net.terramodulus.mui.gui.gfx.GuiRect
import net.terramodulus.mui.gui.gfx.GuiSprite
import net.terramodulus.mui.gui.gfx.Rectangle
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleI
import net.terramodulus.mui.gui.gfx.RenderSystem
import kotlin.math.min
import kotlin.math.pow
import kotlin.properties.Delegates

private val BG_COLOR = floatArrayOf(.145F, .776F, 0.768F)

private const val ANI_DURATION = 1F // in second
private const val PAUSE_DURATION = 2F // in second

class ResourceLoadingScreen(
	managerHandle: ScreenManager.Handle,
	asdHandle: AsdHandle.Container,
	renderSystemHandle: RenderSystem.Handle,
) : Screen(managerHandle, asdHandle) {
	private var stage = 0
	private var last = System.currentTimeMillis() // timestamp in milliseconds
	private var alphaFilter = AlphaFilter(0F)
	private val progressBar = ProgressBar(renderSystemHandle)
	override val layout = CompositeLayout(this)

	init {
		layout.update {
			add(SingletonLayout(this@ResourceLoadingScreen, GeomComponent(GuiRect(
				renderSystemHandle.canvasHandle, 0, 0, 1, 1, 0, 255, 213, 255
			), RectangleD(0.0, 0.0, 1.0, 1.0), ComponentAsdHandleImpl()).apply {
				geom.add(alphaFilter)
			}, SingletonLayout.Config.Absolute.Full))
			add(SingletonLayout(this@ResourceLoadingScreen, SimplePane(ComponentAsdHandleImpl()) {
				SingletonLayout(this, DrawablesComponent(sequenceOf(
					DrawablesComponent.Drawable(
						GuiSprite(
							renderSystemHandle.canvasHandle,
							RectangleI(0, 100, 400, 100),
							renderSystemHandle.loadTexture("/game_logo.png")
						)
					),
					DrawablesComponent.Drawable(
						GuiRect(renderSystemHandle.canvasHandle, 0, 0, 400, 40, 240, 240, 240, 255)
					),
					DrawablesComponent.Drawable(
						GuiRect(renderSystemHandle.canvasHandle, 5, 5, 395, 35, 0, 255, 213, 255)
					),
					DrawablesComponent.Drawable(progressBar.rect),
				), RectangleD(0.0, 0.0, 400.0, 200.0), ComponentAsdHandleImpl()).apply {
					addFilter(alphaFilter)
				}, SingletonLayout.Config.Aligned(
					SingletonLayout.Config.ObjectFit.Contain,
					SingletonLayout.Config.AlignmentConfig.DEFAULT,
				))
			}, SingletonLayout.Config.Aligned(
				SingletonLayout.Config.Relative.Simple(0.5),
				SingletonLayout.Config.AlignmentConfig.DEFAULT,
			)))
		}

		addListener(ScreenEvent.Update::class.java) {
			val current = System.currentTimeMillis()
			val elapsed = (current - last) / 1000F // elapsed time in second at this stage
			when (stage) {
				0 -> if (elapsed >= ANI_DURATION) {
					stage = 1
					last = current
					alphaFilter.alpha = 1F
				} else {
					alphaFilter.alpha = elapsed / ANI_DURATION
				}

				1 -> {
					// TODO when there is something to load, stay at this stage until ready
					val x = elapsed / PAUSE_DURATION
					// S-curve animation, but this will not look good if speed is not constant
					progressBar.progress = min(1 - (1 - x.pow(3.5F)).pow(12), 1F)
					if (progressBar.progress >= 1F) {
						stage = 2
						last = current
					}
				}

				2 -> if (elapsed >= ANI_DURATION) {
					stage = 3
					last = current
					alphaFilter.alpha = 0F
				} else {
					alphaFilter.alpha = 1 - elapsed / ANI_DURATION
				}

// 			3 -> screenManager.handle.openBefore(::TitleScreen, this)
				3 -> it.muiIoI.screenManager.handle.reset(it.muiIoI.renderSystem.newGameplayScreen(ZeroImmVec3f))
			}
		}
	}

	private class ProgressBar(renderSystemHandle: RenderSystem.Handle) {
		val rectDim = Rectangle.withPoints(7, 7, 393, 33)
		val length = rectDim.width
		var progress: Float by Delegates.observable(0f) { _, _, _ ->
			rect.setPos(7, 7, rectDim.x + (progress * length).toInt(), 33)
		}
		val rect = GuiRect(renderSystemHandle.canvasHandle, 7, 7, 7, 33, 240, 240, 240, 255)
	}
}
