/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec2.MutVec2d
import net.terramodulus.mui.gui.agim.AgimoPropertyMap
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.agim.Container
import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.agim.LayoutComputationGroup
import net.terramodulus.mui.gui.agim.LayoutComputationUnit
import net.terramodulus.mui.gui.agim.LayoutHandle
import net.terramodulus.mui.gui.agim.getProperty
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.Anchor5
import net.terramodulus.mui.gui.gfx.Direction2
import net.terramodulus.mui.gui.gfx.RectangleD
import kotlin.math.max

/**
 * Common implementation that is either [ColumnLayout] or [RowLayout].
 *
 * This is an optimized special version of [FlexibleBoxLayout] without any expected
 * multiple *sequences* of components in a single layout.
 */
sealed class SequenceLayout(
	container: Container,
	override val elements: ElementList<Element>,
	protected var config: Config,
) : Layout.ElementGroup<SequenceLayout.Element>(container, elements) {
	class Element {
		companion object {
			fun default() = Element()
		}
	}

	/**
	 * [padding] is the paddings from the four edges.
	 * [gap] is the gaps only in between elements.
	 */
	class Config(val direction: Direction2, val gap: Double = 0.0, val padding: Double = 0.0)

	interface ConfigEnv {
		var config: Config
	}

	fun update(operation: ConfigEnv.() -> Unit) {
		operate { operation(object : ConfigEnv {
			override var config: Config by this@SequenceLayout::config
		}) }
	}

	override fun add(component: Component) = elements.add(component, Element.default())

	override fun addBefore(target: Component, component: Component) =
		elements.addBefore(target, component, Element.default())

	override fun addAfter(target: Component, component: Component) =
		elements.addAfter(target, component, Element.default())

	override fun replace(target: Component, component: Component) =
		elements.replace(target, component, Element.default())
}

/**
 * **Column** case of [SequenceLayout].
 */
class ColumnLayout private constructor(container: Container, elements: ElementList<Element>, config: Config) :
	SequenceLayout(container, elements, config) {
	companion object {
		fun withComponents(vararg components: Component, config: Config) = { it: Container ->
			ColumnLayout(it, ElementList.withComponentsDefault(Element::default, *components), config)
		}

		fun withComponents(components: Collection<Component>, config: Config) = { it: Container ->
			ColumnLayout(it, ElementList.withComponentsDefault(Element::default, components), config)
		}

		fun withElements(vararg elements: Pair<Component, Element>, config: Config) = { it: Container ->
			ColumnLayout(it, ElementList.withElements(*elements), config)
		}

		fun withElements(elements: Map<Component, Element>, config: Config) = { it: Container ->
			ColumnLayout(it, ElementList.withElements(elements), config)
		}
	}

	override fun layOut(handle: LayoutHandle) = sequenceOf(LayoutComputationGroup({}, {
		// If all elements are separated into respective Units, race conditions may occur.
		setOf(LayoutComputationUnit({
			put(container.asdHandle, setOf(BoundsProperty.KEY))
			elements.forEach { put(it.first.asdHandle, setOf(IntrinsicDimensionsProperty.KEY)) }
		}, {
			elements.forEach { put(it.first.asdHandle, setOf(BoundsProperty.KEY)) }
			put(container.asdHandle, setOf(RectangleProperty.KEY))
		}, {
			val containerRect = getUnit(container.asdHandle).getProperty(BoundsProperty.KEY)!!.value
			val anchor = MutVec2d(containerRect.x + config.padding, when (config.direction) {
				Direction2.Positive -> containerRect.y + config.padding
				Direction2.Negative -> containerRect.y + containerRect.height - config.padding
			})
			var width = 0.0
			var height = 0.0
			val map = mutableMapOf<AsdHandle, AgimoPropertyMap>()
			elements.forEach {
				val dim = getUnit(it.first.asdHandle).getProperty(IntrinsicDimensionsProperty.KEY)!!
				width = max(width, dim.width.toDouble())
				height += dim.height.toDouble() + config.gap
			}
			height = max(height - config.gap, 0.0)
			elements.forEach {
				val dim = getUnit(it.first.asdHandle).getProperty(IntrinsicDimensionsProperty.KEY)!!
				if (config.direction == Direction2.Negative) anchor.y -= dim.height.toDouble()
				map[it.first.asdHandle] = AgimoPropertyMap().apply {
					putProperty(BoundsProperty.KEY, BoundsProperty(
						RectangleD(anchor.x, anchor.y, width, dim.height.toDouble())
					))
				}
				when (config.direction) {
					Direction2.Positive -> anchor.y += dim.height.toDouble() + config.gap
					Direction2.Negative -> anchor.y -= config.gap
				}
			}
			map[container.asdHandle] = AgimoPropertyMap().apply {
				putProperty(RectangleProperty.KEY, RectangleProperty(RectangleD(
					containerRect.x, containerRect.y, width + config.padding * 2, height + config.padding * 2,
				)))
			}
			map
		}))
	}))
}

/**
 * **Row** case of [SequenceLayout].
 */
class RowLayout private constructor(container: Container, elements: ElementList<Element>, config: Config) :
	SequenceLayout(container, elements, config) {
	companion object {
		fun withComponents(vararg components: Component, config: Config) = { it: Container ->
			RowLayout(it, ElementList.withComponentsDefault(Element::default, *components), config)
		}

		fun withComponents(components: Collection<Component>, config: Config) = { it: Container ->
			RowLayout(it, ElementList.withComponentsDefault(Element::default, components), config)
		}

		fun withElements(vararg elements: Pair<Component, Element>, config: Config) = { it: Container ->
			RowLayout(it, ElementList.withElements(*elements), config)
		}

		fun withElements(elements: Map<Component, Element>, config: Config) = { it: Container ->
			RowLayout(it, ElementList.withElements(elements), config)
		}
	}

	override fun layOut(handle: LayoutHandle) = sequenceOf(LayoutComputationGroup({}, {
		// If all elements are separated into respective Units, race conditions may occur.
		setOf(LayoutComputationUnit({
			put(container.asdHandle, setOf(BoundsProperty.KEY))
			elements.forEach { put(it.first.asdHandle, setOf(IntrinsicDimensionsProperty.KEY)) }
		}, {
			elements.forEach { put(it.first.asdHandle, setOf(BoundsProperty.KEY)) }
			put(container.asdHandle, setOf(RectangleProperty.KEY))
		}, {
			val containerRect = getUnit(container.asdHandle).getProperty(BoundsProperty.KEY)!!.value
			val anchor = MutVec2d(when (config.direction) {
				Direction2.Positive -> containerRect.x + config.padding
				Direction2.Negative -> containerRect.x + containerRect.width - config.padding
			}, containerRect.y + config.gap)
			var width = 0.0
			var height = 0.0
			val map = mutableMapOf<AsdHandle, AgimoPropertyMap>()
			elements.forEach {
				val dim = getUnit(it.first.asdHandle).getProperty(IntrinsicDimensionsProperty.KEY)!!
				width += dim.width.toDouble() + config.gap
				height = max(height, dim.height.toDouble())
			}
			width = max(width - config.gap, 0.0)
			elements.forEach {
				val dim = getUnit(it.first.asdHandle).getProperty(IntrinsicDimensionsProperty.KEY)!!
				if (config.direction == Direction2.Negative) anchor.x -= dim.width.toDouble()
				map[it.first.asdHandle] = AgimoPropertyMap().apply {
					putProperty(BoundsProperty.KEY, BoundsProperty(
						RectangleD(anchor.x, anchor.y, dim.width.toDouble(), height)
					))
				}
				when (config.direction) {
					Direction2.Positive -> anchor.x += dim.width.toDouble() + config.gap
					Direction2.Negative -> anchor.x -= config.gap
				}
			}
			map[container.asdHandle] = AgimoPropertyMap().apply {
				putProperty(RectangleProperty.KEY, RectangleProperty(RectangleD(
					containerRect.x, containerRect.y, width + config.padding * 2, height + config.padding * 2,
				)))
			}
			map
		}))
	}))
}
