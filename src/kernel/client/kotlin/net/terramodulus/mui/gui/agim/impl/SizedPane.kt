/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.AbstractPane
import net.terramodulus.mui.gui.agim.AgimoPropertyMap
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.agim.LayoutComputationGroup
import net.terramodulus.mui.gui.agim.LayoutComputationUnit
import net.terramodulus.mui.gui.agim.LayoutHandle
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.RenderSystem

/**
 * Provide intrinsic information for this container and its [component].
 */
class SizedPane(asdHandle: AsdHandle, component: Component, private var config: Config) : AbstractPane(asdHandle) {
	private val _layout = SizedLayout(component)
	override val layout: Layout get() = _layout

	private inner class SizedLayout(var component: Component) : Layout(this@SizedPane) {
		override val components = componentsSequence(::component)

		override fun layOut(handle: LayoutHandle) = sequenceOf(LayoutComputationGroup({}, {
			setOf(LayoutComputationUnit({}, {
				put(component.asdHandle, setOf(IntrinsicRatioProperty.KEY, IntrinsicDimensionsProperty.KEY))
				put(container.asdHandle, setOf(IntrinsicRatioProperty.KEY, IntrinsicDimensionsProperty.KEY))
			}, {
				val dim = IntrinsicDimensionsProperty(config.width, config.height)
				val ratio = dim.computeRatio()
				mapOf(
					component.asdHandle to AgimoPropertyMap().apply {
						putProperty(IntrinsicRatioProperty.KEY, ratio)
						putProperty(IntrinsicDimensionsProperty.KEY, dim)
					},
					container.asdHandle to AgimoPropertyMap().apply {
						putProperty(IntrinsicRatioProperty.KEY, ratio)
						putProperty(IntrinsicDimensionsProperty.KEY, dim)
					}
				)
			}), LayoutComputationUnit({ // Forwarding Rect to component
				put(container.asdHandle, setOf(RectangleProperty.KEY, BoundsProperty.KEY))
			}, {
				put(component.asdHandle, setOf(BoundsProperty.KEY))
			}, {
				mapOf(component.asdHandle to AgimoPropertyMap().apply {
					val prop = getUnit(container.asdHandle)
					putProperty(BoundsProperty.KEY, BoundsProperty(
						prop.getProperty(RectangleProperty.KEY)?.value
							?: prop.getProperty(BoundsProperty.KEY)!!.value)
					)
				})
			}))
		}))
	}

	data class Config(val width: UInt, val height: UInt)

	fun update(component: Component) {
		_layout.operate {
			_layout.component = component
		}
	}

	interface ConfigEnv {
		var config: Config
	}

	fun update(operation: ConfigEnv.() -> Unit) {
		_layout.operate {
			operation(object : ConfigEnv {
				override var config: Config by this@SizedPane::config
			})
		}
	}

	override fun render(renderSystem: RenderSystem) {
		layout.render(renderSystem)
	}
}
