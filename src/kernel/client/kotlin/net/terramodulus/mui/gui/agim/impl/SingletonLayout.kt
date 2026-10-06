/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec2.ImmVec2d
import net.terramodulus.mui.gui.agim.AgimoPropertyMap
import net.terramodulus.mui.gui.agim.AnchorAlignmentHelper
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.agim.Container
import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.agim.LayoutComputationGroup
import net.terramodulus.mui.gui.agim.LayoutComputationUnit
import net.terramodulus.mui.gui.agim.LayoutHandle
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.Dimension2D
import net.terramodulus.mui.gui.gfx.Direction2S
import net.terramodulus.mui.gui.gfx.InsetsD
import net.terramodulus.mui.gui.gfx.RectangleD
import kotlin.math.max
import kotlin.math.min

class SingletonLayout(container: Container, component: Component, private var config: Config) : Layout(container) {
	override val components = componentsSequence(::component)
	var component = component
		private set

	sealed class Config private constructor() {
		abstract fun layOut(layout: SingletonLayout): Set<LayoutComputationUnit>

		sealed class Absolute private constructor() : Config() {
			override fun layOut(layout: SingletonLayout): Set<LayoutComputationUnit> =
				setOf(LayoutComputationUnit({
					put(layout.container.asdHandle, setOf(RectangleProperty.KEY, BoundsProperty.KEY))
				}, {
					put(layout.component.asdHandle, setOf(BoundsProperty.KEY))
				}, {
					mapOf(layout.component.asdHandle to AgimoPropertyMap().apply {
						val prop = getUnit(layout.container.asdHandle)
						putProperty(BoundsProperty.KEY, BoundsProperty(compute(
							prop.getProperty(RectangleProperty.KEY)?.value
								?: prop.getProperty(BoundsProperty.KEY)!!.value)
						))
					})
				}))

			abstract fun compute(container: RectangleD): RectangleD

			data object Full : Absolute() {
				override fun compute(container: RectangleD) = container
			}

			data class Insets(var insets: InsetsD) : Absolute() {
				override fun compute(container: RectangleD) = container - insets
			}
		}

		/**
		 * Automatically inducing full insets by only two distinct sides of insets and intrinsic dimensions.
		 */
		data class Auto(val x: Side, val y: Side) : Config() {
			data class Side(val dir: Direction2S, val offset: Double)

			override fun layOut(layout: SingletonLayout) = setOf(LayoutComputationUnit({
				put(layout.container.asdHandle, setOf(RectangleProperty.KEY, BoundsProperty.KEY))
				put(layout.component.asdHandle, setOf(IntrinsicDimensionsProperty.KEY, DimensionsProperty.KEY))
			}, {
				put(layout.component.asdHandle, setOf(BoundsProperty.KEY))
			}, {
				mapOf(layout.component.asdHandle to AgimoPropertyMap().apply {
					val prop = getUnit(layout.container.asdHandle)
					val rect = prop.getProperty(RectangleProperty.KEY)?.value
						?: prop.getProperty(BoundsProperty.KEY)!!.value
					val dims = DimensionsProperty.getOrComputeValue(getUnit(layout.component.asdHandle))
					val left: Double
					val top: Double
					val right: Double
					val bottom: Double
					when (x.dir) {
						Direction2S.Positive -> {
							right = x.offset
							left = rect.width - x.offset - dims.width
						}
						Direction2S.Negative -> {
							left = x.offset
							right = rect.width - x.offset - dims.width
						}
					}
					when (y.dir) {
						Direction2S.Positive -> {
							top = y.offset
							bottom = rect.height - y.offset - dims.height
						}
						Direction2S.Negative -> {
							bottom = y.offset
							top = rect.height - y.offset - dims.height
						}
					}
					putProperty(BoundsProperty.KEY, BoundsProperty(rect - InsetsD(left, top, right, bottom)))
				})
			}))
		}

		data class Aligned(val config: Relative, val alignment: AlignmentConfig) : Config() {
			override fun layOut(layout: SingletonLayout): Set<LayoutComputationUnit> =
				setOf(LayoutComputationUnit(config.dependencies(layout), {
					put(layout.component.asdHandle, setOf(BoundsProperty.KEY))
				}, {
					mapOf(layout.component.asdHandle to AgimoPropertyMap().apply {
						val prop = getUnit(layout.container.asdHandle)
						val rect = prop.getProperty(RectangleProperty.KEY)?.value
							?: prop.getProperty(BoundsProperty.KEY)!!.value
						val target = config.compute(layout, this@LayoutComputationUnit)
						putProperty(BoundsProperty.KEY, BoundsProperty(
							AnchorAlignmentHelper.simple(rect, target, ImmVec2d(alignment.x, alignment.y))
						))
					})
				}))
		}

		/**
		 * Configuration where both intrinsic properties and bounds are transmissive:
		 * - intrinsic properties are transmitted from inner to outer
		 * - bounds and rectangle are transmitted from outer to inner
		 */
		data class Sole(val config: Scaled) : Config() {
			override fun layOut(layout: SingletonLayout): Set<LayoutComputationUnit> = setOf(
				LayoutComputationUnit({
					put(layout.component.asdHandle, setOf(
						IntrinsicDimensionsProperty.KEY,
						IntrinsicRatioProperty.KEY,
						DimensionsProperty.KEY,
					))
				}, {
					put(layout.container.asdHandle, setOf(
						IntrinsicDimensionsProperty.KEY,
						IntrinsicRatioProperty.KEY,
						DimensionsProperty.KEY,
					))
				}, {
					mapOf(layout.container.asdHandle to AgimoPropertyMap().apply {
						val target = config.compute(layout, this@LayoutComputationUnit)
						val dims = IntrinsicDimensionsProperty(target.width.toUInt(), target.height.toUInt())
						putProperty(IntrinsicDimensionsProperty.KEY, dims)
						putProperty(IntrinsicRatioProperty.KEY, dims.computeRatio())
						putProperty(DimensionsProperty.KEY, DimensionsProperty(target))
					})
				}),
				LayoutComputationUnit({
					put(layout.container.asdHandle, setOf(BoundsProperty.KEY, RectangleProperty.KEY))
				}, {
					put(layout.component.asdHandle, setOf(BoundsProperty.KEY))
				}, {
					mapOf(layout.component.asdHandle to AgimoPropertyMap().apply {
						val prop = getUnit(layout.container.asdHandle)
						val rect = prop.getProperty(RectangleProperty.KEY)?.value
							?: prop.getProperty(BoundsProperty.KEY)!!.value
						putProperty(BoundsProperty.KEY, BoundsProperty(rect))
					})
				}),
			)
		}

		sealed class Relative {
			// Must include Container Bounds & Rectangle
			abstract fun dependencies(layout: SingletonLayout):
				MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>.() -> Unit

			abstract fun compute(layout: SingletonLayout, handle: LayoutHandle): Dimension2D

			data class Simple(val scale: Double) : Relative() {
				override fun dependencies(layout: SingletonLayout):
					MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>.() -> Unit = {
					put(layout.container.asdHandle, setOf(RectangleProperty.KEY, BoundsProperty.KEY))
				}

				override fun compute(layout: SingletonLayout, handle: LayoutHandle): Dimension2D {
					val prop = handle.getUnit(layout.container.asdHandle)
					val rect = prop.getProperty(RectangleProperty.KEY)?.value
						?: prop.getProperty(BoundsProperty.KEY)!!.value
					return Dimension2D(rect.width * scale, rect.height * scale)
				}
			}

			data class Both(val scaleX: Double, val scaleY: Double) : Relative() {
				override fun dependencies(layout: SingletonLayout):
					MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>.() -> Unit = {
					put(layout.container.asdHandle, setOf(RectangleProperty.KEY, BoundsProperty.KEY))
				}

				override fun compute(layout: SingletonLayout, handle: LayoutHandle): Dimension2D {
					val prop = handle.getUnit(layout.container.asdHandle)
					val rect = prop.getProperty(RectangleProperty.KEY)?.value
						?: prop.getProperty(BoundsProperty.KEY)!!.value
					return Dimension2D(rect.width * scaleX, rect.height * scaleY)
				}
			}
		}

		sealed class ObjectFit private constructor() : Relative() {
			override fun dependencies(layout: SingletonLayout):
				MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>.() -> Unit = {
				put(layout.container.asdHandle, setOf(RectangleProperty.KEY, BoundsProperty.KEY))
				put(layout.component.asdHandle, setOf(IntrinsicRatioProperty.KEY, DimensionsProperty.KEY))
			}

			override fun compute(layout: SingletonLayout, handle: LayoutHandle): Dimension2D {
				val prop = handle.getUnit(layout.container.asdHandle)
				return compute(
					prop.getProperty(RectangleProperty.KEY)?.value
						?: prop.getProperty(BoundsProperty.KEY)!!.value,
					DimensionsProperty.getOrComputeRatio(handle.getUnit(layout.component.asdHandle))
				)
			}

			abstract fun compute(container: RectangleD, component: Dimension2D): Dimension2D

			data object Contain : ObjectFit() {
				override fun compute(container: RectangleD, component: Dimension2D): Dimension2D {
					val w = container.width / component.width
					val h = container.height / component.height
					val scale = min(w, h)
					return Dimension2D(
						component.width * scale,
						component.height * scale,
					)
				}
			}

			data object Cover : ObjectFit() {
				override fun compute(container: RectangleD, component: Dimension2D): Dimension2D {
					val w = container.width / component.width
					val h = container.height / component.height
					val scale = max(w, h)
					return Dimension2D(
						component.width * scale,
						component.height * scale,
					)
				}
			}
		}

		sealed class Scaled private constructor() : Relative() {
			override fun dependencies(layout: SingletonLayout):
				MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>.() -> Unit = {
				put(layout.container.asdHandle, setOf(RectangleProperty.KEY, BoundsProperty.KEY))
				put(layout.component.asdHandle, setOf(IntrinsicDimensionsProperty.KEY, DimensionsProperty.KEY))
			}

			override fun compute(layout: SingletonLayout, handle: LayoutHandle) = compute(
				DimensionsProperty.getOrComputeValue(handle.getUnit(layout.component.asdHandle))
			)

			abstract fun compute(dim: Dimension2D): Dimension2D

			/**
			 * Scale both dimensions by the same scaling
			 * @param scale `> 0`
			 */
			data class Scale(val scale: Double) : Scaled() {
				override fun compute(dim: Dimension2D) =
					Dimension2D(dim.width * scale, dim.height * scale)
			}

			class Compute private constructor(private val x: Value, private val y: Value) : Scaled() {
				private object MathEnvImpl : MathEnv

				constructor(x: MathEnv.() -> Value, y: MathEnv.() -> Value) : this(x(MathEnvImpl), y(MathEnvImpl))

				sealed interface Value {
					fun compute(dim: Dimension2D): Double

					operator fun plus(that: Value) = Operator.Plus(this, that)
					operator fun minus(that: Value) = Operator.Minus(this, that)
					operator fun times(that: Value) = Operator.Times(this, that)
					operator fun div(that: Value) = Operator.Div(this, that)
				}

				sealed class Operator private constructor() : Value {
					data class Plus(val a: Value, val b: Value) : Operator() {
						override fun compute(dim: Dimension2D) = a.compute(dim) + b.compute(dim)
					}
					data class Minus(val a: Value, val b: Value) : Operator() {
						override fun compute(dim: Dimension2D) = a.compute(dim) - b.compute(dim)
					}
					data class Times(val a: Value, val b: Value) : Operator() {
						override fun compute(dim: Dimension2D) = a.compute(dim) * b.compute(dim)
					}
					data class Div(val a: Value, val b: Value) : Operator() {
						override fun compute(dim: Dimension2D) = a.compute(dim) / b.compute(dim)
					}
				}

				sealed class Param private constructor() : Value {
					data class Num(val value: Double) : Param() {
						override fun compute(dim: Dimension2D) = value
					}
					data object DimX : Param() {
						override fun compute(dim: Dimension2D) = dim.width
					}
					data object DimY : Param() {
						override fun compute(dim: Dimension2D) = dim.height
					}
				}

				sealed interface MathEnv {
					val dimX: Param get() = Param.DimX
					val dimY: Param get() = Param.DimY
					fun num(value: Double) = Param.Num(value)
				}

				override fun compute(dim: Dimension2D) = Dimension2D(x.compute(dim), y.compute(dim))
			}
		}

		/**
		 * Range within `[0,1]`
		 */
		data class AlignmentConfig(val x: Double, val y: Double) {
			companion object {
				val DEFAULT = AlignmentConfig(0.5, 0.5)
				fun withX(x: Double) = AlignmentConfig(x, 0.5)
				fun withY(y: Double) = AlignmentConfig(y, 0.5)
			}
		}
	}

	fun update(component: Component) {
		operate {
			this@SingletonLayout.component = component
		}
	}

	interface ConfigEnv {
		var config: Config
	}

	fun update(operation: ConfigEnv.() -> Unit) {
		operate {
			operation(object : ConfigEnv {
				override var config: Config by this@SingletonLayout::config
			})
		}
	}

	override fun layOut(handle: LayoutHandle) =
		sequenceOf(LayoutComputationGroup({}, { config.layOut(this@SingletonLayout) }))
}
