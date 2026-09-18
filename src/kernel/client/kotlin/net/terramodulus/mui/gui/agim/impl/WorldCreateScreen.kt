/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec4.ImmVec4i
import net.terramodulus.engine.common.ZeroImmVec3f
import net.terramodulus.mui.gui.InputStatesHandle
import net.terramodulus.mui.gui.agim.Screen
import net.terramodulus.mui.gui.agim.ScreenManager
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.Direction2S
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.gfx.TextContext
import net.terramodulus.util.nextEntry

internal class WorldCreateScreen(
	renderSystem: RenderSystem,
	managerHandle: ScreenManager.Handle,
	asdHandle: AsdHandle.Container,
	renderSystemHandle: RenderSystem.Handle,
	inputStatesHandle: InputStatesHandle,
) : Screen(managerHandle, asdHandle) {
	override val layout = CompositeLayout(this) {
		add(SingletonLayout(this@WorldCreateScreen, TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
			TextContext.Config(26F, 26F, ImmVec4i(255)),
		).apply {
			text = "World Options"
		}, SingletonLayout.Config.Auto(
			SingletonLayout.Config.Auto.Side(Direction2S.Negative, 0.0),
			SingletonLayout.Config.Auto.Side(Direction2S.Positive, 0.0),
		)))
		val options = WorldOptions.Builder()
		add(SingletonLayout(this@WorldCreateScreen, SimplePane(ComponentAsdHandleImpl()) {
			ColumnLayout.withComponents(listOf(
				TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
					TextContext.Config(22F, 22F, ImmVec4i(255))
				).apply { text = "World Type" },
				run {
					lateinit var listener: () -> Unit
					ButtonComponent(ComponentAsdHandleImpl(), inputStatesHandle, {
						lateinit var layout: SingletonLayout
						SingletonLayout(
							this, TextDisplayComponent(
							ComponentAsdHandleImpl(), renderSystemHandle,
							TextContext.Config(20F, 20F, ImmVec4i(240))
						).apply {
							val listener1 = {
								text = when (options.worldType) {
									WorldOptions.WorldType.CubeSets -> "Cube Sets"
									WorldOptions.WorldType.Flat -> "Flat"
								}
							}.apply { this() }
							listener = {
								layout.operate { listener1() }
							}
						}, SingletonLayout.Config.Sole(SingletonLayout.Config.Scaled.Scale(1.0))).apply {
							layout = this
						}
					}) {
						options.worldType = WorldOptions.WorldType.entries.nextEntry(options.worldType)
						listener()
					}
				},
				SizedPane(
					ComponentAsdHandleImpl(),
					BlankComponent(ComponentAsdHandleImpl()),
					SizedPane.Config(1u, 20u),
				),
				TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
					TextContext.Config(22F, 22F, ImmVec4i(255))
				).apply { text = "Character Type" },
				run {
					lateinit var listener: () -> Unit
					ButtonComponent(ComponentAsdHandleImpl(), inputStatesHandle, {
						lateinit var layout: SingletonLayout
						SingletonLayout(
							this, TextDisplayComponent(
								ComponentAsdHandleImpl(), renderSystemHandle,
								TextContext.Config(20F, 20F, ImmVec4i(240))
							).apply {
								val listener1 = {
									text = when (options.charType) {
										WorldOptions.CharacterType.Sphere -> "Sphere"
										WorldOptions.CharacterType.Complex -> "Complex"
									}
								}.apply { this() }
								listener = {
									layout.operate { listener1() }
								}
							}, SingletonLayout.Config.Sole(SingletonLayout.Config.Scaled.Scale(1.0))).apply {
							layout = this
						}
					}) {
						options.charType = WorldOptions.CharacterType.entries.nextEntry(options.charType)
						listener()
					}
				},
			), SequenceLayout.Config(Direction2S.Negative, 3.0, intrinsic = true))(this)
		}, SingletonLayout.Config.Aligned(
			SingletonLayout.Config.Scaled.Scale(1.0),
			SingletonLayout.Config.AlignmentConfig.DEFAULT,
		)))
		add(SingletonLayout(this@WorldCreateScreen, ButtonComponent(ComponentAsdHandleImpl(), inputStatesHandle, {
			SingletonLayout(this, TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
				TextContext.Config(24F, 24F, ImmVec4i(255)),
			).apply {
				text = "Create World"
			}, SingletonLayout.Config.Sole(SingletonLayout.Config.Scaled.Scale(1.0)))
		}) {
			managerHandle.reset(renderSystem.newGameplayScreen(options.build(), ZeroImmVec3f))
		}, SingletonLayout.Config.Auto(
			SingletonLayout.Config.Auto.Side(Direction2S.Positive, 0.0),
			SingletonLayout.Config.Auto.Side(Direction2S.Negative, 0.0),
		)))
	}

	init {
		renderSystemHandle.setBackgroundColor(0F, 0F, 0F, 0F)
	}

	data class WorldOptions(val worldType: WorldType, val charType: CharacterType) {
		enum class WorldType { CubeSets, Flat }
		enum class CharacterType { Sphere, Complex }

		class Builder {
			var worldType = WorldType.CubeSets
			var charType = CharacterType.Sphere

			fun build() = WorldOptions(worldType, charType)
		}
	}
}
