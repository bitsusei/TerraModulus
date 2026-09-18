/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.vec2.ImmVec2d
import net.terramodulus.mui.gui.agim.AbstractPane
import net.terramodulus.mui.gui.agim.AgimoPropertyMap
import net.terramodulus.mui.gui.agim.AnchorAlignmentHelper
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.agim.LayoutComputationGroup
import net.terramodulus.mui.gui.agim.LayoutComputationUnit
import net.terramodulus.mui.gui.agim.LayoutHandle
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.Dimension2D
import net.terramodulus.mui.gui.gfx.Direction2S
import net.terramodulus.mui.gui.gfx.Direction4A
import net.terramodulus.mui.gui.gfx.GeneralTransform
import net.terramodulus.mui.gui.gfx.GuiLine
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RenderSystem
import kotlin.math.PI
import kotlin.math.max
import kotlin.properties.Delegates

class CollapsablePane(canvasHandle: RenderSystem.CanvasHandle, asdHandle: AsdHandle, config: ConstructEnv.() -> ConstructEnv.Config) : AbstractPane(asdHandle) {
	companion object {
		private const val ROT_RIGHT = PI / 2
		private const val ROT_LEFT = -PI / 2
		private const val INDICATOR_SIZE = 12.0
		private val INDICATOR_DIMS = Dimension2D(INDICATOR_SIZE, INDICATOR_SIZE)
	}

	object ConstructEnv {
		class Config(
			val headerPos: Direction4A,
			val indicatorPos: Direction2S,
			val header: Component,
			val contents: Component,
		)
	}

	private val _layout = CollapsableLayout(canvasHandle, config(ConstructEnv))
	override val layout: Layout = _layout

	var open: Boolean by _layout::open

	private inner class CollapsableLayout(canvasHandle: RenderSystem.CanvasHandle, config: ConstructEnv.Config) :
		Layout(this@CollapsablePane) {
		private val headerPos = config.headerPos
		private val indicatorPos = config.indicatorPos
		private val standardRot: GeneralTransform
		private val transform = GeneralTransform()
		// standard orientation: right/pos towards x
		private val indicatorFace = DrawablesComponent(sequenceOf(
			DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 1, 1, 3, 2, 255, 255, 255, 255)),
			DrawablesComponent.Drawable.Geom(GuiLine(canvasHandle, 1, 3, 3, 2, 255, 255, 255, 255)),
		), RectangleD(0.0, 0.0, 4.0, 4.0), ComponentAsdHandleImpl())
		private var header = config.header
		private var contents = config.contents

		var open: Boolean by Delegates.observable(false) { _, _, newValue ->
			operate {
				transform.update {
					angle = if (newValue) when (headerPos) {
						Direction4A.XPos -> when (indicatorPos) {
							Direction2S.Positive -> ROT_RIGHT
							Direction2S.Negative -> ROT_LEFT
						}
						Direction4A.XNeg -> when (indicatorPos) {
							Direction2S.Positive -> ROT_LEFT
							Direction2S.Negative -> ROT_RIGHT
						}
						Direction4A.YPos -> when (indicatorPos) {
							Direction2S.Positive -> ROT_LEFT
							Direction2S.Negative -> ROT_RIGHT
						}
						Direction4A.YNeg -> when (indicatorPos) {
							Direction2S.Positive -> ROT_RIGHT
							Direction2S.Negative -> ROT_LEFT
						}
					} else 0.0
				}
			}
		}

		init {
			val angle = when (headerPos) {
				Direction4A.XPos, Direction4A.XNeg -> when (indicatorPos) { // y
					Direction2S.Positive -> -PI / 2 // -90 degrees to upwards
					Direction2S.Negative -> PI / 2 // +90 degrees to downwards
				}
				Direction4A.YPos, Direction4A.YNeg -> when (indicatorPos) { // x
					Direction2S.Positive -> 0.0 // 0 to rightwards
					Direction2S.Negative -> PI // 180 degrees to leftwards
				}
			}
			standardRot = GeneralTransform(1.0, 1.0, angle, 0.0, 0.0)
			indicatorFace.addTransform(standardRot)
			indicatorFace.addTransform(transform)
		}

		override val components = componentsSequence(::indicatorFace, ::header, ::contents)

		fun updateHeader(header: Component) = operate { this@CollapsableLayout.header = header }

		fun updateContents(contents: Component) = operate { this@CollapsableLayout.contents = contents }

		override fun layOut(handle: LayoutHandle) = sequenceOf(LayoutComputationGroup({}, {
			setOf(LayoutComputationUnit({
				// indicatorFace can be ignored here
				put(header.asdHandle, setOf(IntrinsicDimensionsProperty.KEY, DimensionsProperty.KEY))
				put(contents.asdHandle, setOf(IntrinsicDimensionsProperty.KEY, DimensionsProperty.KEY))
			}, {
				put(this@CollapsablePane.asdHandle, setOf(DimensionsProperty.KEY))
			}, {
				val headerDims = DimensionsProperty.getOrComputeValue(getUnit(header.asdHandle))
				val contentsDims = DimensionsProperty.getOrComputeValue(getUnit(contents.asdHandle))
				mapOf(this@CollapsablePane.asdHandle to AgimoPropertyMap().apply {
					putProperty(DimensionsProperty.KEY, DimensionsProperty(when (headerPos) {
						Direction4A.XPos, Direction4A.XNeg -> {
							val height = max(headerDims.height + INDICATOR_SIZE, contentsDims.height)
							val headerWidth = max(INDICATOR_SIZE, headerDims.width)
							Dimension2D(headerWidth + contentsDims.width, height)
						}
						Direction4A.YPos, Direction4A.YNeg -> {
							val width = max(headerDims.width + INDICATOR_SIZE, contentsDims.width)
							val headerHeight = max(INDICATOR_SIZE, headerDims.height)
							Dimension2D(width, headerHeight + contentsDims.height)
						}
					}))
				})
			}), LayoutComputationUnit({
				put(header.asdHandle, setOf(IntrinsicDimensionsProperty.KEY, DimensionsProperty.KEY))
				put(contents.asdHandle, setOf(IntrinsicDimensionsProperty.KEY, DimensionsProperty.KEY))
				put(this@CollapsablePane.asdHandle, setOf(
					BoundsProperty.KEY,
					RectangleProperty.KEY,
					DimensionsProperty.KEY,
				))
			}, {
				put(indicatorFace.asdHandle, setOf(BoundsProperty.KEY))
				put(header.asdHandle, setOf(BoundsProperty.KEY))
				put(contents.asdHandle, setOf(BoundsProperty.KEY))
			}, {
				val headerDims = DimensionsProperty.getOrComputeValue(getUnit(header.asdHandle))
				val contentsDims = DimensionsProperty.getOrComputeValue(getUnit(contents.asdHandle))
				val prop = getUnit(this@CollapsablePane.asdHandle)
				val containerRect = prop.getProperty(RectangleProperty.KEY)?.value
					?: prop.getProperty(BoundsProperty.KEY)!!.value
				assert(prop.getProperty(DimensionsProperty.KEY)!!.value.let {
					containerRect.width == it.width && containerRect.height == it.height
				})
				val indicatorRect: RectangleD
				val headerRect: RectangleD
				val contentsRect: RectangleD
				when (headerPos) {
					Direction4A.XPos, Direction4A.XNeg -> {
						val headerWidth = max(INDICATOR_SIZE, headerDims.width)
						val headerSpace: RectangleD
						when (headerPos) {
							Direction4A.XPos -> {
								headerSpace = RectangleD(
									containerRect.x + containerRect.width - headerWidth,
									containerRect.y,
									headerWidth,
									containerRect.height,
								)
								contentsRect = AnchorAlignmentHelper.simple(RectangleD(
									containerRect.x,
									containerRect.y,
									contentsDims.width,
									containerRect.height,
								), contentsDims, ImmVec2d(0.5))
							}
							Direction4A.XNeg -> {
								headerSpace = RectangleD(
									containerRect.x,
									containerRect.y,
									headerWidth,
									containerRect.height,
								)
								contentsRect = AnchorAlignmentHelper.simple(RectangleD(
									containerRect.x + headerWidth,
									containerRect.y,
									contentsDims.width,
									containerRect.height,
								), contentsDims, ImmVec2d(0.5))
							}
						}
						when (indicatorPos) {
							Direction2S.Positive -> {
								indicatorRect =
									AnchorAlignmentHelper.simple(headerSpace, INDICATOR_DIMS, ImmVec2d(0.5, 1.0))
								headerRect = AnchorAlignmentHelper.simple(headerSpace, headerDims, ImmVec2d(0.5, 0.0))
							}
							Direction2S.Negative -> {
								indicatorRect =
									AnchorAlignmentHelper.simple(headerSpace, INDICATOR_DIMS, ImmVec2d(0.5, 0.0))
								headerRect = AnchorAlignmentHelper.simple(headerSpace, headerDims, ImmVec2d(0.5, 1.0))
							}
						}
					}
					Direction4A.YPos, Direction4A.YNeg -> {
						val headerHeight = max(INDICATOR_SIZE, headerDims.height)
						val headerSpace: RectangleD
						when (headerPos) {
							Direction4A.YPos -> {
								headerSpace = RectangleD(
									containerRect.x,
									containerRect.y + containerRect.height - headerHeight,
									containerRect.width,
									headerHeight,
								)
								contentsRect = AnchorAlignmentHelper.simple(RectangleD(
									containerRect.x,
									containerRect.y,
									containerRect.width,
									contentsDims.height,
								), contentsDims, ImmVec2d(0.5))
							}
							Direction4A.YNeg -> {
								headerSpace = RectangleD(
									containerRect.x,
									containerRect.y,
									containerRect.width,
									headerHeight,
								)
								contentsRect = AnchorAlignmentHelper.simple(RectangleD(
									containerRect.x,
									containerRect.y + headerHeight,
									containerRect.width,
									contentsDims.height,
								), contentsDims, ImmVec2d(0.5))
							}
						}
						when (indicatorPos) {
							Direction2S.Positive -> {
								indicatorRect =
									AnchorAlignmentHelper.simple(headerSpace, INDICATOR_DIMS, ImmVec2d(1.0, 0.5))
								headerRect = AnchorAlignmentHelper.simple(headerSpace, headerDims, ImmVec2d(0.0, 0.5))
							}
							Direction2S.Negative -> {
								indicatorRect =
									AnchorAlignmentHelper.simple(headerSpace, INDICATOR_DIMS, ImmVec2d(0.0, 0.5))
								headerRect = AnchorAlignmentHelper.simple(headerSpace, headerDims, ImmVec2d(1.0, 0.5))
							}
						}
					}
				}
				mapOf(
					indicatorFace.asdHandle to AgimoPropertyMap().apply { putProperty(BoundsProperty(indicatorRect)) },
					header.asdHandle to AgimoPropertyMap().apply { putProperty(BoundsProperty(headerRect)) },
					contents.asdHandle to AgimoPropertyMap().apply { putProperty(BoundsProperty(contentsRect)) },
				)
			}))
		}))
	}

	fun updateHeader(header: Component) = _layout.updateHeader(header)

	fun updateContents(contents: Component) = _layout.updateContents(contents)

	override fun render(renderSystem: RenderSystem) = _layout.render(renderSystem)
}
