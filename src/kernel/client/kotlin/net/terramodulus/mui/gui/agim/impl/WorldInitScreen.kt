/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec4.ImmVec4i
import net.terramodulus.mui.gui.agim.Screen
import net.terramodulus.mui.gui.agim.ScreenManager
import net.terramodulus.mui.gui.agim.event.ScreenEvent
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.AlphaFilter
import net.terramodulus.mui.gui.gfx.Direction4A
import net.terramodulus.mui.gui.gfx.GuiRect
import net.terramodulus.mui.gui.gfx.GuiSprite
import net.terramodulus.mui.gui.gfx.InsetsD
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RectangleI
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.gfx.TextContext
import net.terramodulus.void.World
import kotlin.math.roundToInt

private const val ANI_DURATION = 1F // in second

class WorldInitScreen internal constructor(
	managerHandle: ScreenManager.Handle,
	asdHandle: AsdHandle.Container,
	renderSystemHandle: RenderSystem.Handle,
) : Screen(managerHandle, asdHandle) {
	private var stage = 0
	private var last = System.currentTimeMillis() // timestamp in milliseconds
	private var alphaFilter = AlphaFilter(0F)
// 	private val progressBar = ProgressBar(renderSystemHandle)
	private val progressBarComponent = SliderComponent(
		Direction4A.XPos,
		renderSystemHandle.canvasHandle,
		ComponentAsdHandleImpl(),
		// Not sure why background has to be non-transparent for this to render correctly
		SliderComponent.Config(ImmVec4i(56, 255, 252, 255), ImmVec4i(59, 12, 120, 255)),
	).apply {
		addFilter(alphaFilter)
	}
	override val layout = CompositeLayout(this)
	private val progressBarImpl = ProgressBarImpl()
	internal val progressBar: World.ProgressBar = progressBarImpl
	private var onAlphaChange: () -> Unit = {}

	init {
		layout.update {
			add(SingletonLayout(this@WorldInitScreen, GeomComponent(GuiRect(
				renderSystemHandle.canvasHandle, 0, 0, 1, 1, 56, 255, 252, 255
			), RectangleD(0.0, 0.0, 1.0, 1.0), ComponentAsdHandleImpl()).apply {
				geom.add(alphaFilter)
			}, SingletonLayout.Config.Absolute.Full))
			add(SingletonLayout(this@WorldInitScreen, SimplePane(ComponentAsdHandleImpl()) {
				SingletonLayout(this, DrawablesComponent(sequenceOf(
					DrawablesComponent.Drawable(
						GuiSprite(
							renderSystemHandle.canvasHandle,
							RectangleI(0, 100, 400, 100),
							renderSystemHandle.loadTexture("/game_logo.png")
						)
					),
					DrawablesComponent.Drawable(
						GuiRect(renderSystemHandle.canvasHandle, 0, 0, 400, 40, 59, 12, 120, 255)
					),
					DrawablesComponent.Drawable(
						GuiRect(renderSystemHandle.canvasHandle, 5, 5, 395, 35, 56, 255, 252, 255)
					),
// 					DrawablesComponent.Drawable(progressBar.rect),
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
			add(SingletonLayout(this@WorldInitScreen, SimplePane(ComponentAsdHandleImpl()) {
				SingletonLayout(this, SizedPane(ComponentAsdHandleImpl(), SimplePane(ComponentAsdHandleImpl()) {
					SingletonLayout(this, progressBarComponent,
						SingletonLayout.Config.Absolute.Insets(InsetsD(7.0, 167.0, 7.0, 7.0))
					)
				},
					SizedPane.Config(400u, 200u)), SingletonLayout.Config.Aligned(
						SingletonLayout.Config.ObjectFit.Contain,
						SingletonLayout.Config.AlignmentConfig.DEFAULT,
					)
				)
			}, SingletonLayout.Config.Aligned(
				SingletonLayout.Config.Relative.Simple(0.5),
				SingletonLayout.Config.AlignmentConfig.DEFAULT,
			)))
			add(SingletonLayout(this@WorldInitScreen, SimplePane(ComponentAsdHandleImpl()) {
				SingletonLayout(this, SizedPane(ComponentAsdHandleImpl(), SimplePane(ComponentAsdHandleImpl()) {
					SingletonLayout(this, TextDisplayComponent(
						ComponentAsdHandleImpl(),
						renderSystemHandle,
						TextContext.Config(16.0F, 16.0F, ImmVec4i(255, 255, 255, 0)),
					).apply {
						text = "Initializing Demo World..."
						onAlphaChange = {
							update {
								color = ImmVec4i(255, 255, 255, (alphaFilter.alpha * 255).roundToInt())
							}
						}
					}, SingletonLayout.Config.Absolute.Insets(InsetsD(7.0, 149.0, 7.0, 35.0)))
				},
					SizedPane.Config(400u, 200u)), SingletonLayout.Config.Aligned(
						SingletonLayout.Config.ObjectFit.Contain,
						SingletonLayout.Config.AlignmentConfig.DEFAULT,
					)
				)
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
					onAlphaChange()
					progressBarImpl.ready = true
				} else {
					alphaFilter.alpha = elapsed / ANI_DURATION
					onAlphaChange()
				}

				1 -> {
					if (progressBarComponent.fraction >= 1) {
						progressBarComponent.fraction = 1.0
						stage = 2
						last = current
					}
				}

				2 -> if (elapsed >= ANI_DURATION) {
					stage = 3
					last = current
					alphaFilter.alpha = 0F
					onAlphaChange()
				} else {
					alphaFilter.alpha = 1 - elapsed / ANI_DURATION
					onAlphaChange()
				}

// 			3 -> screenManager.handle.openBefore(::TitleScreen, this)
				3 -> it.muiIoI.screenManager.handle.exit(1)
			}
		}
	}

	private inner class ProgressBarImpl : World.ProgressBar {
		var ready = false
			set(value) {
				field = value
				readyListener?.invoke()
			}
		var readyListener: (() -> Unit)? = null

		override fun addReadyListener(listener: () -> Unit) {
			readyListener = listener
			if (ready) listener()
		}

		override fun setProgress(progress: Double) {
			progressBarComponent.fraction = progress
		}
	}

// 	internal class ProgressBar(renderSystemHandle: RenderSystem.Handle) {
// 		val rectDim = Rectangle.withPoints(7, 7, 393, 33)
// 		val length = rectDim.width
// 		var progress: Float by Delegates.observable(0f) { _, _, _ ->
// 			rect.setPos(7, 7, rectDim.x + (progress * length).toInt(), 33)
// 		}
// 		val rect = GuiRect(renderSystemHandle.canvasHandle, 7, 7, 7, 33, 240, 240, 240, 255)
// 	}
}
