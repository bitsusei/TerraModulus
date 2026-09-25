/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.quaternion.ImmQuatd
import com.cout970.math.vec2.ImmVec2d
import com.cout970.math.vec2.Vec2d
import com.cout970.math.vec2.minus
import com.cout970.math.vec2.normalized
import com.cout970.math.vec2.times
import com.cout970.math.vec3.ImmVec3d
import com.cout970.math.vec3.Vec3d
import com.cout970.math.vec3.Vec3f
import com.cout970.math.vec3.Vec3i
import com.cout970.math.vec3.div
import com.cout970.math.vec3.dot
import com.cout970.math.vec3.floor
import com.cout970.math.vec3.minus
import com.cout970.math.vec3.normalized
import com.cout970.math.vec3.plus
import com.cout970.math.vec3.times
import com.cout970.math.vec3.toImmVec3d
import com.cout970.math.vec3.toImmVec3f
import com.cout970.math.vec3.toMutVec3d
import com.cout970.math.vec4.ImmVec4i
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import net.terramodulus.core.TerraModulus
import net.terramodulus.core.getResourceAsString
import net.terramodulus.engine.Camera3D
import net.terramodulus.engine.PhyBody
import net.terramodulus.engine.PhyGeom
import net.terramodulus.engine.SimpleMesh3dGeomCube
import net.terramodulus.engine.SimpleMesh3dGeomSphere
import net.terramodulus.engine.WorldObjDrawable
import net.terramodulus.engine.common.ZeroImmVec3d
import net.terramodulus.mui.gui.InputStatesHandle
import net.terramodulus.mui.gui.MouseCtxStates
import net.terramodulus.mui.gui.MouseState
import net.terramodulus.mui.gui.agim.AbstractPane
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.agim.Menu
import net.terramodulus.mui.gui.agim.Screen
import net.terramodulus.mui.gui.agim.ScreenManager
import net.terramodulus.mui.gui.agim.event.ComponentEvent
import net.terramodulus.mui.gui.agim.event.MenuEvent
import net.terramodulus.mui.gui.agim.event.ScreenEvent
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.AlphaFilter
import net.terramodulus.mui.gui.gfx.Cuboid
import net.terramodulus.mui.gui.gfx.Dimension3D
import net.terramodulus.mui.gui.gfx.Direction2S
import net.terramodulus.mui.gui.gfx.Direction6C
import net.terramodulus.mui.gui.gfx.GeneralTransform
import net.terramodulus.mui.gui.gfx.GuiLine
import net.terramodulus.mui.gui.gfx.GuiRect
import net.terramodulus.mui.gui.gfx.Octree
import net.terramodulus.mui.gui.gfx.RectStParams
import net.terramodulus.mui.gui.gfx.RectangleD
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.gfx.TextContext
import net.terramodulus.mui.kui.KeyboardInputHandler
import net.terramodulus.mui.kui.MouseInputHandler
import net.terramodulus.util.logging.logger
import net.terramodulus.void.World
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.tan
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.reflect.KProperty0
import kotlin.sequences.filter
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val WHITE = ImmVec4i(255, 255, 255, 255)
private val RED = ImmVec4i(255, 0, 0, 255)
private val GREEN = ImmVec4i(0, 255, 0, 255)
private val BLUE = ImmVec4i(0, 0, 255, 255)
private val STD_SCALE = ImmVec3d(.5, .5, .5)
private val IDENT_ROT = ImmQuatd(1.0, .0, .0, .0)
private const val MASS = 1.0
private const val MAX_SPEED = PI // reachable by autonomous movement
private const val MAX_ACC = PI // without other forces, reaching MAX_SPEED in one second
private const val MAX_PROJ_SPEED = 64.0
private const val MIN_PROJ_SPEED = 1.0 / 4.0
private const val MIN_SPEED_FACTOR = 1.0 / 16.0
private const val MAX_SPEED_FACTOR = 64.0
private const val MIN_ACC_FACTOR = 1.0 / 16.0
private const val MAX_ACC_FACTOR = 64.0
private const val MOVE_EPSILON = .1 // smallest acc to apply
private const val MIN_GRAVITY = 1.0
private const val MAX_GRAVITY = 20.0
private const val MIN_FRICTION = 1.0 / 16.0
private const val MAX_FRICTION = 64.0
private const val MIN_ZOOM = 1.0 / 4.0
private const val MAX_ZOOM = 4
private const val CHUNK_SIZE = 512
private const val MIN_CELL_SIZE = 4

private val logger = logger {}

internal class GameplayScreen(
	worldOptions: WorldCreateScreen.WorldOptions,
	private val core: TerraModulus,
	private val camera: Camera3D,
	renderSystemHandle: RenderSystem.Handle,
	managerHandle: ScreenManager.Handle,
	asdHandle: AsdHandle.Container,
	inputStatesHandle: InputStatesHandle,
) : Screen(managerHandle, asdHandle) {
	private val geoShaders = camera.loadGeoShaders(
		getResourceAsString("/gwr_geo.vsh"),
		getResourceAsString("/gwr_geo.fsh"),
	)

	private val canvasHandle = renderSystemHandle.canvasHandle

	private val chunkManager = ChunkManager()

	private lateinit var player: PlayerVoidGeom
	override val layout = CompositeLayout(this)
	private val mouseDebugTrackingLayer = MouseDebugTrackingLayer(renderSystemHandle, inputStatesHandle)
	private val attributeTrackingPane = AttributeTrackingPane(renderSystemHandle)

	private var hotkeysEnabled = false
	private var speedFactor = 1.0
	private var accFactor = 1.0
	private var makeKinematicProjection = false
	private var projectionSpeed = 2.0

	private val worldCommands = mutableListOf<WorldCommand>()

	private sealed class WorldCommand {
		data class AddProjection(val pos: Vec3d, val dir: Vec3d) : WorldCommand()
	}

	init {
		renderSystemHandle.setBackgroundColor(0F, 0F, 0F, 0F)
		managerHandle.open { p1: ScreenManager.Handle, p2: AsdHandle.Container, p3: RenderSystem.Handle ->
			// In production, this screen should be placed separately.
			WorldInitScreen(p1, p2, p3).apply {
				core.world = World(object : World.Ymir.Builder {
					override fun build(agent: World.YmirAgent) = Ymir(worldOptions, agent)
				}, progressBar).apply {
					addUpdaterListener { e ->
						worldCommands.forEach {
							when (it) {
								is WorldCommand.AddProjection -> (e.commander as Ymir).addProjection(it.pos, it.dir)
							}
						}
						worldCommands.clear()
					}
				}
				addListener(ScreenEvent.Close::class.java) {
					this@GameplayScreen.layout.update {
						add(SingletonLayout(
							this@GameplayScreen,
							GameplayRenderer(renderSystemHandle, inputStatesHandle),
							SingletonLayout.Config.Absolute.Full,
						))
						add(SingletonLayout(
							this@GameplayScreen,
							SimplePane(ComponentAsdHandleImpl()) {
								ColumnLayout.withElements(
									ButtonComponent(ComponentAsdHandleImpl(), inputStatesHandle, {
										CompositeLayout(this) {
											add(SingletonLayout(this@ButtonComponent, GeomComponent(
												GuiRect(canvasHandle, 0, 0, 1, 1, 122, 122, 0, 255),
												RectangleD(0.0, 0.0, 1.0, 1.0),
												ComponentAsdHandleImpl(),
											), SingletonLayout.Config.Absolute.Full))
											add(SingletonLayout(
												this@ButtonComponent, TextDisplayComponent(
												ComponentAsdHandleImpl(),
												renderSystemHandle,
												TextContext.Config(20F, 20F, ImmVec4i(255)),
											).apply {
												text = "Query..."
											}, SingletonLayout.Config.Sole(SingletonLayout.Config.Scaled.Scale(1.0))))
										}
									}) {
										managerHandle.addMenu { handle, asdHandle ->
											object : Menu(handle, asdHandle) {
												override val layout = with(this) menu@ {
													fun command(label: String, action: () -> Unit) =
														ButtonComponent(ComponentAsdHandleImpl(), inputStatesHandle, {
															SingletonLayout(this@ButtonComponent, TextDisplayComponent(
																ComponentAsdHandleImpl(),
																renderSystemHandle,
																TextContext.Config(20F, 20F, ImmVec4i(255)),
															).apply { text = label }, SingletonLayout.Config.Sole(
																SingletonLayout.Config.Scaled.Scale(1.0)
															))
														}) {
															action()
															exit()
														}
													SingletonLayout(this, SimplePane(ComponentAsdHandleImpl()) parent@ {
														CompositeLayout(this) {
															add(SingletonLayout(this@parent, GeomComponent(
																GuiRect(canvasHandle, 0, 0, 1, 1, 122, 122, 128, 255),
																RectangleD(0.0, 0.0, 1.0, 1.0),
																ComponentAsdHandleImpl(),
															), SingletonLayout.Config.Absolute.Full))
															add(ColumnLayout.withComponents(listOf(
																command("Position of Sphere", ::queryPos),
																command("Velocity of Sphere", ::queryVec),
																command("Gravity of World", ::queryGravity),
																command("Friction of World", ::queryFriction),
															), config = SequenceLayout.Config(
																Direction2S.Negative,
																intrinsic = true,
															))(this@parent))
														}
													}, SingletonLayout.Config.Auto(
														SingletonLayout.Config.Auto.Side(Direction2S.Positive, 0.0),
														SingletonLayout.Config.Auto.Side(Direction2S.Positive, 20.0),
													))
												}

												init {
													// just a quick hack but this certainly needs to be changed
													asdHandle.properties.putProperty(
														BoundsProperty.KEY,
														BoundsProperty(this@GameplayScreen.asdHandle.rect)
													)
													this@GameplayScreen.asdHandle.observeRect {
														asdHandle.properties.putProperty(
															BoundsProperty.KEY,
															BoundsProperty(this@GameplayScreen.asdHandle.rect)
														)
													}

													addListener(MenuEvent.Update::class.java) {
														if (it.muiIoI.inputSystem.condition { keyboard { Escape.justDown } })
															exit()
													}
												}

												fun exit() = handle.removeMenu(this)
											}
										}
									} to SequenceLayout.Element(1.0),
									ButtonComponent(ComponentAsdHandleImpl(), inputStatesHandle, button@ {
										CompositeLayout(this) {
											add(SingletonLayout(this@button, GeomComponent(GuiRect(canvasHandle,
												0, 0, 1, 1, 122, 122, 255, 255,
											), RectangleD(0.0, 0.0, 1.0, 1.0), ComponentAsdHandleImpl()),
												SingletonLayout.Config.Absolute.Full))
											add(SingletonLayout(this@button, TextDisplayComponent(
												ComponentAsdHandleImpl(),
												renderSystemHandle,
												TextContext.Config(20F, 20F, ImmVec4i(255)),
											).apply {
												text = "Reset Velocity to 0"
											}, SingletonLayout.Config.Sole(SingletonLayout.Config.Scaled.Scale(1.0))))
										}
									}, ::resetVel) to SequenceLayout.Element(1.0),
									config = SequenceLayout.Config(Direction2S.Negative, intrinsic = true)
								)(this)
							},
							SingletonLayout.Config.Aligned(
								SingletonLayout.Config.Scaled.Scale(1.0),
								SingletonLayout.Config.AlignmentConfig(1.0, 1.0),
							),
						))
						add(SingletonLayout(
							this@GameplayScreen,
							SimplePane(ComponentAsdHandleImpl()) {
								CompositeLayout(this).apply {
									add(SingletonLayout(this@SimplePane, GeomComponent(
										GuiRect(canvasHandle, 0, 0, 1, 1, 10, 10, 255, 255),
										RectangleD(0.0, 0.0, 1.0, 1.0),
										ComponentAsdHandleImpl(),
									), SingletonLayout.Config.Absolute.Full))
// 									lateinit var trackingCtrlPane1: CollapsablePane
// 									lateinit var trackingCtrlPane2: CollapsablePane
									add(RowLayout.withElements(
										SimplePane(ComponentAsdHandleImpl()) {
											ColumnLayout.withComponents(listOf(
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Legacy Hotkeys" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Mouse Debug Tracking" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Gravity Influence" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Gravity (-y)" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Friction Mode" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Friction (Limited mode)" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Speed Factor" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Acceleration Factor" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Projection Type" },
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Projection Speed" },
// 												CollapsablePane(canvasHandle, ComponentAsdHandleImpl()) {
// 													config(YPos, Neg, TextDisplayComponent(
// 														ComponentAsdHandleImpl(),
// 														renderSystemHandle,
// 														TextContext.Config(20F, 20F, ImmVec4i(255)),
// 													).apply { text = "Tracking..." },
// 														SimplePane(ComponentAsdHandleImpl()) {
// 															ColumnLayout.withComponents(listOf(
																TextDisplayComponent(
																	ComponentAsdHandleImpl(),
																	renderSystemHandle,
																	TextContext.Config(20F, 20F, ImmVec4i(255)),
																).apply { text = "TPS & TPT" },
																TextDisplayComponent(
																	ComponentAsdHandleImpl(),
																	renderSystemHandle,
																	TextContext.Config(20F, 20F, ImmVec4i(255)),
																).apply { text = "Position" },
																TextDisplayComponent(
																	ComponentAsdHandleImpl(),
																	renderSystemHandle,
																	TextContext.Config(20F, 20F, ImmVec4i(255)),
																).apply { text = "Velocity" },
																TextDisplayComponent(
																	ComponentAsdHandleImpl(),
																	renderSystemHandle,
																	TextContext.Config(20F, 20F, ImmVec4i(255)),
																).apply { text = "Force/Acceleration" },
// 															), SequenceLayout.Config(
// 																Direction2S.Neg,
// 																intrinsic = true,
// 															))(this)
// 														})
// 												}.apply {
// 													trackingCtrlPane1 = this
// 													withMouseInput(inputStatesHandle) {
// 														trackingCtrlPane2.open = !trackingCtrlPane2.open
// 													}
// 												},
												TextDisplayComponent(ComponentAsdHandleImpl(), renderSystemHandle,
													TextContext.Config(20F, 20F, ImmVec4i(255)),
												).apply { text = "Zoom Level" },
											), SequenceLayout.Config(Direction2S.Negative, intrinsic = true))(this)
										} to SequenceLayout.Element(1.0),
										SimplePane(ComponentAsdHandleImpl()) {
											ColumnLayout.withComponents(listOf(
												SizedPane(ComponentAsdHandleImpl(), CheckboxComponent(
													ComponentAsdHandleImpl(), inputStatesHandle, canvasHandle
												) { hotkeysEnabled = it }, SizedPane.Config(20u, 20u)),
												SizedPane(ComponentAsdHandleImpl(), CheckboxComponent(
													ComponentAsdHandleImpl(), inputStatesHandle, canvasHandle,
												) { mouseDebugTrackingLayer.enabled = it }, SizedPane.Config(20u, 20u)),
												SizedPane(ComponentAsdHandleImpl(), CheckboxComponent(
													ComponentAsdHandleImpl(),
													inputStatesHandle,
													canvasHandle,
													player.phyBody.gravityMode,
												) { player.phyBody.gravityMode = it }.apply {
													gravityModeListener = { checked = player.phyBody.gravityMode }
												}, SizedPane.Config(20u, 20u)),
												SizedPane(ComponentAsdHandleImpl(), SimplePane(ComponentAsdHandleImpl())
												parent@ {
													CompositeLayout(this).apply {
														lateinit var listener: (Double) -> Unit
														add(SingletonLayout(this@parent, SliderComponent(
															canvasHandle, inputStatesHandle, ComponentAsdHandleImpl()
														) {
															config(withRanged(
																MIN_GRAVITY..MAX_GRAVITY,
																-core.world!!.gravity.y,
															) {
																core.world!!.gravity = core.world!!.gravity
																	.toMutVec3d().apply { y = -it }
																listener(it)
															}, xPos, ImmVec4i(123, 234, 56, 255),
																ImmVec4i(50, 50, 250, 255),
															)
														}.apply {
															gravityListener = {
																val v = -core.world!!.gravity.y
																fraction = SliderComponent.SliderMode.Ranged.Transform
																	.Linear.project(v, MIN_GRAVITY..MAX_GRAVITY)
																listener(v)
															}
														}, SingletonLayout.Config.Absolute.Full))
														add(SingletonLayout(this@parent, TextDisplayComponent(
															ComponentAsdHandleImpl(), renderSystemHandle,
															TextContext.Config(20F, 20F, ImmVec4i(255))
														).apply {
															listener = { it: Double ->
																text = String.format("%.2f", it)
															}.apply { this(-core.world!!.gravity.y) }
														}, SingletonLayout.Config.Absolute.Full))
													}
												}, SizedPane.Config(100u, 20u)),
												ButtonComponent(
													ComponentAsdHandleImpl(),
													inputStatesHandle,
													{
														lateinit var layout: SingletonLayout
														SingletonLayout(this, TextDisplayComponent(
															ComponentAsdHandleImpl(),
															renderSystemHandle,
															TextContext.Config(20F, 20F, ImmVec4i(255)),
														).apply {
															val listener = {
																text = core.world!!.frictionMode.toString()
															}.apply { this() }
															frictionModeListener = {
																layout.operate { listener() }
															}
														}, SingletonLayout.Config.Sole(
															SingletonLayout.Config.Scaled.Scale(1.0)
														)).apply { layout = this }
													},
												) {
													core.world!!.frictionMode = World.FrictionMode.entries[
														(core.world!!.frictionMode.ordinal + 1) % World.FrictionMode.entries.size
													]
													frictionModeListener()
												},
												SizedPane(ComponentAsdHandleImpl(), SimplePane(ComponentAsdHandleImpl())
												parent@ {
													CompositeLayout(this).apply {
														lateinit var listener: (Double) -> Unit
														add(SingletonLayout(this@parent, SliderComponent(
															canvasHandle, inputStatesHandle, ComponentAsdHandleImpl()
														) {
															config(withRanged(
																MIN_FRICTION..MAX_FRICTION,
																core.world!!.friction,
																transformLinearExponential(2.0),
															) {
																core.world!!.friction = it
																listener(it)
															}, xPos, ImmVec4i(123, 234, 56, 255),
																ImmVec4i(50, 50, 250, 255),
															)
														}.apply {
															frictionListener = {
																val v = core.world!!.friction
																fraction = SliderComponent.SliderMode.Ranged.Transform
																	.LinearExponential(2.0)
																	.project(v, MIN_FRICTION..MAX_FRICTION)
																listener(v)
															}
														}, SingletonLayout.Config.Absolute.Full))
														add(SingletonLayout(this@parent, TextDisplayComponent(
															ComponentAsdHandleImpl(), renderSystemHandle,
															TextContext.Config(20F, 20F, ImmVec4i(255))
														).apply {
															listener = { it: Double ->
																text = String.format("%.2f", it)
															}.apply { this(core.world!!.friction) }
														}, SingletonLayout.Config.Absolute.Full))
													}
												}, SizedPane.Config(100u, 20u)),
												SizedPane(ComponentAsdHandleImpl(), SimplePane(ComponentAsdHandleImpl())
												parent@ {
													CompositeLayout(this).apply {
														lateinit var listener: () -> Unit
														add(SingletonLayout(this@parent, SliderComponent(
															canvasHandle, inputStatesHandle, ComponentAsdHandleImpl()
														) {
															config(withRanged(
																MIN_SPEED_FACTOR..MAX_SPEED_FACTOR,
																speedFactor,
																transformLinearExponential(2.0),
															) {
																speedFactor = it
																listener()
															}, xPos, ImmVec4i(123, 234, 56, 255),
																ImmVec4i(50, 50, 250, 255),
															)
														}, SingletonLayout.Config.Absolute.Full))
														add(SingletonLayout(this@parent, TextDisplayComponent(
															ComponentAsdHandleImpl(), renderSystemHandle,
															TextContext.Config(20F, 20F, ImmVec4i(255))
														).apply {
															listener = {
																text = String.format("%.4g", speedFactor)
															}.apply { this() }
														}, SingletonLayout.Config.Absolute.Full))
													}
												}, SizedPane.Config(100u, 20u)),
												SizedPane(ComponentAsdHandleImpl(), SimplePane(ComponentAsdHandleImpl())
												parent@ {
													CompositeLayout(this).apply {
														lateinit var listener: () -> Unit
														add(SingletonLayout(this@parent, SliderComponent(
															canvasHandle, inputStatesHandle, ComponentAsdHandleImpl()
														) {
															config(withRanged(
																MIN_ACC_FACTOR..MAX_ACC_FACTOR,
																accFactor,
																transformLinearExponential(2.0),
															) {
																accFactor = it
																listener()
															}, xPos, ImmVec4i(123, 234, 56, 255),
																ImmVec4i(50, 50, 250, 255),
															)
														}, SingletonLayout.Config.Absolute.Full))
														add(SingletonLayout(this@parent, TextDisplayComponent(
															ComponentAsdHandleImpl(), renderSystemHandle,
															TextContext.Config(20F, 20F, ImmVec4i(255))
														).apply {
															listener = {
																text = String.format("%.4g", accFactor)
															}.apply { this() }
														}, SingletonLayout.Config.Absolute.Full))
													}
												}, SizedPane.Config(100u, 20u)),
												run {
													lateinit var listener: () -> Unit
													ButtonComponent(
														ComponentAsdHandleImpl(),
														inputStatesHandle,
														{
															lateinit var layout: SingletonLayout
															SingletonLayout(this, TextDisplayComponent(
																ComponentAsdHandleImpl(),
																renderSystemHandle,
																TextContext.Config(20F, 20F, ImmVec4i(255)),
															).apply {
																val listener0 = {
																	text = when (makeKinematicProjection) {
																		true -> "Kinematic"
																		false -> "Dynamic"
																	}
																}.apply { this() }
																listener = { layout.operate { listener0() } }
															}, SingletonLayout.Config.Sole(
																SingletonLayout.Config.Scaled.Scale(1.0)
															)).apply { layout = this }
														},
													) {
														makeKinematicProjection = !makeKinematicProjection
														listener()
													}
												},
												SizedPane(ComponentAsdHandleImpl(), SimplePane(ComponentAsdHandleImpl())
												parent@ {
													CompositeLayout(this).apply {
														lateinit var listener: () -> Unit
														add(SingletonLayout(this@parent, SliderComponent(
															canvasHandle, inputStatesHandle, ComponentAsdHandleImpl()
														) {
															config(withRanged(
																MIN_PROJ_SPEED..MAX_PROJ_SPEED,
																projectionSpeed,
																transformLinearExponential(2.0),
															) {
																projectionSpeed = it
																listener()
															}, xPos, ImmVec4i(123, 234, 56, 255),
																ImmVec4i(50, 50, 250, 255),
															)
														}, SingletonLayout.Config.Absolute.Full))
														add(SingletonLayout(this@parent, TextDisplayComponent(
															ComponentAsdHandleImpl(), renderSystemHandle,
															TextContext.Config(20F, 20F, ImmVec4i(255))
														).apply {
															listener = {
																text = String.format("%.4g", projectionSpeed)
															}.apply { this() }
														}, SingletonLayout.Config.Absolute.Full))
													}
												}, SizedPane.Config(100u, 20u)),
// 												CollapsablePane(canvasHandle, ComponentAsdHandleImpl()) {
// 													config(YPos, SizedPane(
// 														ComponentAsdHandleImpl(),
// 														BlankComponent(ComponentAsdHandleImpl()),
// 														SizedPane.Config(20u, 20u),
// 													), SimplePane(ComponentAsdHandleImpl()) {
// 														ColumnLayout.withComponents(listOf(
															SizedPane(ComponentAsdHandleImpl(), CheckboxComponent(ComponentAsdHandleImpl(),
																inputStatesHandle, canvasHandle, false) {
																attributeTrackingPane.toggleRowTpsTpt()
															}, SizedPane.Config(20u, 20u)),
															SizedPane(ComponentAsdHandleImpl(), CheckboxComponent(ComponentAsdHandleImpl(),
																inputStatesHandle, canvasHandle, false) {
																attributeTrackingPane.toggleRowPos()
															}, SizedPane.Config(20u, 20u)),
															SizedPane(ComponentAsdHandleImpl(), CheckboxComponent(ComponentAsdHandleImpl(),
																inputStatesHandle, canvasHandle, false) {
																attributeTrackingPane.toggleRowVel()
															}, SizedPane.Config(20u, 20u)),
															SizedPane(ComponentAsdHandleImpl(), CheckboxComponent(ComponentAsdHandleImpl(),
																inputStatesHandle, canvasHandle, false) {
																attributeTrackingPane.toggleRowForceAcc()
															}, SizedPane.Config(20u, 20u)),
// 														), SequenceLayout.Config(
// 															Direction2S.Neg,
// 															intrinsic = true,
// 														))(this)
// 													})
// 												}.apply {
// 													trackingCtrlPane2 = this
// 													withMouseInput(inputStatesHandle) {
// 														trackingCtrlPane1.open = !trackingCtrlPane1.open
// 													}
// 												},
												SimplePane(ComponentAsdHandleImpl()) {
													lateinit var listener1: () -> Unit
													lateinit var listener2: () -> Unit
													lateinit var listenerTxt: () -> Unit
													zoomLvlListener = {
														listener1()
														listener2()
														listenerTxt()
													}
													val filter1 = AlphaFilter(1F)
													val filter2 = AlphaFilter(1F)
													lateinit var layout: RowLayout
													RowLayout.withComponents(listOf(
														ButtonComponent(
															ComponentAsdHandleImpl(), inputStatesHandle,
															{
																SingletonLayout(
																	this, DrawablesComponent(
																		sequenceOf(
																			DrawablesComponent.Drawable(
																				GuiLine(canvasHandle,
																					1, 2, 3, 2, 255, 255, 255, 255
																				)
																			),
																		), RectangleD(
																			0.0, 0.0, 4.0, 4.0
																		), ComponentAsdHandleImpl()
																	).apply {
																		addFilter(filter1)
																		listener1 = {
																			if (camera.zoomLevel > MIN_ZOOM)
																				filter1.alpha = 1F
																			else
																				filter1.alpha = .5F
																		}
																	},
																	SingletonLayout.Config.Sole(
																		SingletonLayout.Config.Scaled.Scale(20 / 4.0)
																	)
																)
															},
														) {
															if (camera.zoomLevel > MIN_ZOOM) {
																camera.zoomLevel /= 2
																zoomLvlListener()
															}
														},
														TextDisplayComponent(ComponentAsdHandleImpl(),
															renderSystemHandle,
															TextContext.Config(20F, 20F, ImmVec4i(255)),
														).apply {
															val listener = {
																text = "${camera.zoomLevel}"
															}.apply { this() }
															listenerTxt = {
																layout.operate { listener() }
															}
														},
														ButtonComponent(
															ComponentAsdHandleImpl(), inputStatesHandle,
															{
																SingletonLayout(
																	this, DrawablesComponent(
																		sequenceOf(
																			DrawablesComponent.Drawable(
																				GuiLine(
																					canvasHandle,
																					1, 2, 3, 2, 255, 255, 255, 255
																				)
																			),
																			DrawablesComponent.Drawable(
																				GuiLine(
																					canvasHandle,
																					2, 1, 2, 3, 255, 255, 255, 255
																				)
																			),
																		), RectangleD(
																			0.0, 0.0, 4.0, 4.0
																		), ComponentAsdHandleImpl()
																	).apply {
																		addFilter(filter2)
																		listener2 = {
																			if (camera.zoomLevel < MAX_ZOOM)
																				filter2.alpha = 1F
																			else
																				filter2.alpha = .5F
																		}
																	},
																	SingletonLayout.Config.Sole(
																		SingletonLayout.Config.Scaled.Scale(20 / 4.0)
																	)
																)
															},
														) {
															if (camera.zoomLevel < MAX_ZOOM) {
																camera.zoomLevel *= 2
																zoomLvlListener()
															}
														},
													), SequenceLayout.Config(Direction2S.Positive, intrinsic = true)
													)(this).apply { layout = this }
												},
											), SequenceLayout.Config(Direction2S.Negative, intrinsic = true))(this)
										} to SequenceLayout.Element(1.0),
										config = SequenceLayout.Config(Direction2S.Positive, 2.0, 2.0, true),
									)(this@SimplePane))
								}
							},
							SingletonLayout.Config.Aligned(
								SingletonLayout.Config.Scaled.Scale(1.0),
								SingletonLayout.Config.AlignmentConfig(0.0, 1.0),
							),
						))
						add(SingletonLayout(
							this@GameplayScreen,
							attributeTrackingPane,
							SingletonLayout.Config.Auto(
								SingletonLayout.Config.Auto.Side(Direction2S.Pos, 0.0),
								SingletonLayout.Config.Auto.Side(Direction2S.Neg, 50.0),
							),
						))
						add(SingletonLayout(
							this@GameplayScreen,
							mouseDebugTrackingLayer,
							SingletonLayout.Config.Absolute.Full,
						))
					}
					this@GameplayScreen.addListener(ScreenEvent.Update::class.java) {
						update0(it.muiIoI)
						chunkManager.update()
					}
				}
			}
		}
	}

	private inner class MouseDebugTrackingLayer(
		private val handle: RenderSystem.Handle,
		inputStatesHandle: InputStatesHandle,
	) : Component(ComponentAsdHandleImpl()) {
		private var prevPos: Vec2d? = null
		private var label: TextContext? = null
		private val lines = ArrayDeque<Element>()
		private val timeSource = TimeSource.Monotonic
		private val mouseCtxStates = MouseCtxStates(inputStatesHandle.mouseGlobalStates, asdHandle)
		private val threshold = 3.seconds
		var enabled: Boolean by Delegates.observable(false) { _, _, newValue -> if (!newValue) lines.clear() }

		private inner class Element(val timestamp: TimeSource.Monotonic.ValueTimeMark, val geom: GuiLine)

		init {
			asdHandle.observeRect {
				lines.clear()
				prevPos = null
				label = null
			}
			mouseCtxStates.addListener(MouseState.Listener(
				setOf(MouseState.Trigger(MouseState.Key.Movement) { true })
			) {
				if (enabled) when (it) {
					is MouseState.Movement -> {
						if (prevPos != null) {
							val handle = handle.canvasHandle
							// Standard tracking aligned with SDL
							lines.add(Element(
								timeSource.markNow(),
								GuiLine(handle, prevPos!!.xi, prevPos!!.yi, it.pos.xi, it.pos.yi, 255, 165, 0, 255),
							))
							// Secondary tracking by relative values from event polling
							val x = (prevPos!!.xd + it.delX).roundToInt()
							val y = (prevPos!!.yd + it.delY).roundToInt()
							lines.add(Element(
								timeSource.markNow(),
								GuiLine(handle, prevPos!!.xi, prevPos!!.yi, x, y, 0, 255, 0, 255),
							))
						}
						prevPos = it.pos
						(label ?: TextContext(handle, TextContext.Config(16F, 16F, ImmVec4i(255))).apply {
							label = this
						}).apply {
							setText("(${it.pos.x}, ${it.pos.y})")
							update(RectangleD(it.pos.x, it.pos.y, asdHandle.rect.width, asdHandle.rect.height))
						}
					}
					else -> throw AssertionError()
				}
			})
		}

		override fun render(renderSystem: RenderSystem) {
			if (enabled) {
				val now = timeSource.markNow()
				lines.iterator().apply {
					while (hasNext()) {
						val it = next()
						if (now - it.timestamp > threshold) remove()
						else it.geom.render(renderSystem)
					}
				}
				label?.render()
			}
		}
	}

	private inner class AttributeTrackingPane(private val renderSystemHandle: RenderSystem.Handle) :
		AbstractPane(ComponentAsdHandleImpl()) {
		// first row as most bottom
		private var rowTpsTpt: TextDisplayComponent? = null
		private var rowPos: TextDisplayComponent? = null
		private var rowVel: TextDisplayComponent? = null
		private var rowForceAcc: TextDisplayComponent? = null

		override val layout = ColumnLayout.withElements(
			config = SequenceLayout.Config(Direction2S.Pos, intrinsic = true),
		)(this)

		private val backgroundBounds = RectangleD(0.0, 0.0, 1.0, 1.0)
		private val background = GuiRect(renderSystemHandle.canvasHandle, 0, 0, 1, 1, 0, 0, 0, 50)
		private val backgroundTransform = GeneralTransform().apply { background.add(this) }

		init {
			asdHandle.observeRect {
				RectStParams.fromRects(backgroundBounds, asdHandle.rect).applyToGeneralTransform(backgroundTransform)
			}
			addListener(ComponentEvent.Update::class.java) { refresh() }
		}

		private val components = sequenceOf(::rowTpsTpt, ::rowPos, ::rowVel, ::rowForceAcc)

		private fun placeComponent(property: KProperty0<TextDisplayComponent?>, component: TextDisplayComponent) {
			val list = components.toList()
			val e = list.subList(0, list.indexOf(property)).mapNotNull { it() }.lastOrNull()
			if (e == null) layout.components.firstOrNull().let { // because there is no addFirst
				if (it == null) layout.add(component) else layout.addBefore(it, component)
			} else layout.addAfter(e, component)
			layout.replace(component, component, SequenceLayout.Element(1.0))
		}

		private fun newRowComponent() = TextDisplayComponent(
			ComponentAsdHandleImpl(),
			renderSystemHandle,
			TextContext.Config(20F, 20F, ImmVec4i(255)),
		)

		fun toggleRowTpsTpt() {
			rowTpsTpt.let {
				if (it == null) {
					placeComponent(::rowTpsTpt, newRowComponent().apply { rowTpsTpt = this })
					refreshTpsTpt()
				} else layout.remove(it)
			}
		}

		private fun refreshTpsTpt() {
			rowTpsTpt?.text = "TPS: ${core.world!!.tps} | ${String.format("%.2f",
				core.world!!.timePerTick.toDouble(DurationUnit.MILLISECONDS),
			)} ms/t"
		}

		fun toggleRowPos() {
			rowPos.let {
				if (it == null) {
					placeComponent(::rowPos, newRowComponent().apply { rowPos = this })
					refreshPos()
				} else layout.remove(it)
			}
		}

		private fun refreshPos() {
			rowPos?.text = "Position: ${player.pos.display()}"
		}

		fun toggleRowVel() {
			rowVel.let {
				if (it == null) {
					placeComponent(::rowVel, newRowComponent().apply { rowVel = this })
					refreshVel()
				} else layout.remove(it)
			}
		}

		private fun refreshVel() {
			rowVel?.text = "Velocity: ${player.phyBody.linearVel.display()}"
		}

		fun toggleRowForceAcc() {
			rowForceAcc.let {
				if (it == null) {
					placeComponent(::rowForceAcc, newRowComponent().apply { rowForceAcc = this })
					refreshForceAcc()
				} else layout.remove(it)
			}
		}

		private fun refreshForceAcc() {
			rowForceAcc?.text = "Force: TBD | Acc: TBD (Mass: $MASS)"
		}

		override fun render(renderSystem: RenderSystem) {
			if (anyRow()) {
				background.render(renderSystem)
				layout.render(renderSystem)
			}
		}

		private fun anyRow() = components.mapNotNull { it() }.any()

		fun refresh() {
			if (anyRow()) layout.operate {
				refreshTpsTpt()
				refreshPos()
				refreshVel()
				refreshForceAcc()
			}
		}
	}

	private val interactiveGeoms = hashSetOf<VoidGeom>()
	private val interactiveGeomsLock = Any()

	private inner class Ymir(
		private val options: WorldCreateScreen.WorldOptions,
		private val agent: World.YmirAgent,
	) : World.Ymir {
		private val cubeGeom = SimpleMesh3dGeomCube(canvasHandle.canvas, 2F)
		private val sphereGeom = SimpleMesh3dGeomSphere(canvasHandle.canvas, 1F)
		private val projectGeom = SimpleMesh3dGeomSphere(canvasHandle.canvas, .5F)

		override fun wrapCube(phyGeom: PhyGeom, pos: Vec3d) =
			EnvVoidGeom(phyGeom, WorldObjDrawable(cubeGeom, randomColor(), pos, STD_SCALE, IDENT_ROT), pos).apply {
				chunkManager.add(this)
			}

		private fun randomColor() = when (Random.nextInt(3)) {
			0 -> RED
			1 -> GREEN
			2 -> BLUE
			else -> throw AssertionError("Invalid color")
		}

		override fun wrapChar(phyBody: PhyBody, pos: Vec3d) =
			PlayerVoidGeom(phyBody, WorldObjDrawable(sphereGeom, WHITE, pos, STD_SCALE, IDENT_ROT)).apply {
				player = this
				synchronized(interactiveGeomsLock) { interactiveGeoms.add(this) }
				chunkManager.add(this)
			}

		override fun wrapProject(phyBody: PhyBody, pos: Vec3d) =
			ProjectionVoidGeom(phyBody, WorldObjDrawable(projectGeom, WHITE, pos, STD_SCALE, IDENT_ROT)).apply {
				synchronized(interactiveGeomsLock) { interactiveGeoms.add(this) }
				chunkManager.add(this)
			}

		override fun generateWorld(progressBar: World.ProgressBar) {
			when (options.worldType) {
				WorldCreateScreen.WorldOptions.WorldType.CubeSets -> {
					// Spawn point
					agent.genCube(this, ImmVec3d(.0))
					// Main Character
					agent.genChar(this, ImmVec3d(0.0, 1.0, 0.0))
					// Test Objects
					randomCubes(progressBar)
				}
				WorldCreateScreen.WorldOptions.WorldType.Flat -> {
					// Main Character
					agent.genChar(this, ImmVec3d(0.0, 1.0, 0.0))
					// Floor
					val radius = 100
					for (x in -radius..radius) {
						progressBar.setProgress(x / (radius * 2 + 1).toDouble() * .9)
						for (z in -radius..radius) {
							agent.genCube(this, ImmVec3d(x.toDouble(), .0, z.toDouble()))
						}
					}
					// Random Walls
					for (x in -radius..radius) {
						progressBar.setProgress(x / 5.toDouble() * .1 + .9)
						for (z in -radius..radius) {
							if (x != 0 || z != 0)
								if (Random.nextInt(10) < 1)
									agent.genCube(this, ImmVec3d(x.toDouble(), 1.0, z.toDouble()))
						}
					}
					progressBar.setProgress(1.0)
				}
			}
			// TODO char type
		}

		fun addProjection(pos: Vec3d, dir: Vec3d) {
			agent.genProject(this, pos, makeKinematicProjection).apply {
				phyBody.linearVel = dir * projectionSpeed
			}
		}

		// Reference: https://en.wikipedia.org/wiki/Maze_generation_algorithm
		private fun randomCubes(progressBar: World.ProgressBar) {
			var i = 0
// 		    val radius = 12
			val radius = 5
			val total = radius * radius * 2 * 2 * 7
			val intervalHor = 5.0
			val intervalVert = 8
			val max = 5 * 5 * 5 // 125 for each set
			val directions = arrayOf(
				ImmVec3d(1.0, 0.0, 0.0),
				ImmVec3d(-1.0, 0.0, 0.0),
				ImmVec3d(0.0, 1.0, 0.0),
				ImmVec3d(0.0, -1.0, 0.0),
				ImmVec3d(0.0, 0.0, 1.0),
				ImmVec3d(0.0, 0.0, -1.0),
			)
			for (x in 1..radius) {
				for (y in -3..3) {
					for (z in 1..radius) {
						for (xs in booleanArrayOf(false, true)) {
							for (zs in booleanArrayOf(false, true)) {
								progressBar.setProgress(++i / total.toDouble())
								val xx = (if (xs) x else -x).toDouble() * intervalHor
								val zz = (if (zs) z else -z).toDouble() * intervalHor
								val yy = (y * intervalVert).toDouble()
								val origin = ImmVec3d(xx, yy + Random.nextInt(-3..3).toDouble(), zz)
								val visited = mutableSetOf(ImmVec3d(0.0, 0.0, 0.0))
								val heads = ArrayDeque<Vec3d>()
								heads.addLast(ImmVec3d(0.0, 0.0, 0.0))
								while (!heads.isEmpty()) {
									val head = heads.removeFirst()
									for (d in directions) {
										val cur = head + d
										if (Random.nextInt(max) > visited.size && cur !in visited) {
											visited.add(cur)
											if (Random.nextInt(max) > visited.size) {
												heads.addLast(cur)
											}
										}
									}
								}
								for (p in visited) {
									val pt = origin + p
									agent.genCube(this, pt)
								}
							}
						}
					}
				}
			}
		}
	}

	private abstract inner class VoidGeom(val drawable: WorldObjDrawable) : World.VoidGeom {
		override fun render() {
			renderGwrGeo(drawable)
		}
	}

	private inner class EnvVoidGeom(override val phyGeom: PhyGeom, drawable: WorldObjDrawable, override val pos: Vec3d) :
		VoidGeom(drawable), World.EnvVoidGeom

	private inner class ProjectionVoidGeom(
		override val phyBody: PhyBody,
		drawable: WorldObjDrawable,
	) : VoidGeom(drawable), World.InteractiveVoidGeom {
		override val pos: Vec3d by phyBody::pos
	}

	private inner class PlayerVoidGeom(
		override val phyBody: PhyBody,
		drawable: WorldObjDrawable,
	) : VoidGeom(drawable), World.InteractiveVoidGeom {
		fun move(dir: Vec3d) {
			if (dir == ZeroImmVec3d) return // avoid math errors and computations
			val dir = ImmVec3d(dir.x, dir.y, dir.z).normalized()
			val curVel = phyBody.linearVel
			// Let d be the unit vector of autonomous movement target direction,
			//     v_c be the current velocity of body,
			//     v_p be the scalar projection of v_c on d.
			// v_p = v_c * d, may be negative
			// Autonomous acceleration is made only if v_p < MAX_SPEED.
			val projVel = curVel dot dir
			if (projVel < MAX_SPEED * speedFactor) {
				// Let v_d be the delta velocity in direction of d,
				//     a_d be the delta acceleration to be made.
				// v_t = MAX_SPEED - v_p, must be positive
				// a_d = dir * clamp(v_t / 1 s, EPSILON, MAX)
				val deltaVel = MAX_SPEED * speedFactor - projVel
				val deltaAcc = dir * deltaVel.coerceIn(MOVE_EPSILON, MAX_ACC * accFactor)
				phyBody.addForce(deltaAcc * MASS)
			}
		}

		override var pos: Vec3d by phyBody::pos
	}

	private fun Vec3f.toArray() = floatArrayOf(x, y, z)

	private fun Direction6C.toKey() = when (this) {
		Direction6C.North -> KeyboardInputHandler.Keys.W
		Direction6C.South -> KeyboardInputHandler.Keys.S
		Direction6C.West -> KeyboardInputHandler.Keys.A
		Direction6C.East -> KeyboardInputHandler.Keys.D
		Direction6C.Up -> KeyboardInputHandler.Keys.Space
		Direction6C.Down -> KeyboardInputHandler.Keys.LShift
	}

	private fun Direction6C.toVector() = when (this) {
		Direction6C.North -> ImmVec3d(.0, .0, -1.0)
		Direction6C.South -> ImmVec3d(.0, .0, 1.0)
		Direction6C.West -> ImmVec3d(-1.0, .0, .0)
		Direction6C.East -> ImmVec3d(1.0, .0, .0)
		Direction6C.Up -> ImmVec3d(.0, 1.0, .0)
		Direction6C.Down -> ImmVec3d(.0, -1.0, .0)
	}

	private fun Vec3d.display() = "[$x, $y, $z]"

	/**
	 * Query position of sphere
	 */
	private fun queryPos() {
		logger.info { "Position: ${player.pos.display()}" }
	}

	/**
	 * Query velocity of sphere
	 *
	 * Note: Acceleration is hard to be queried as force is zeroed after each world step
	 */
	private fun queryVec() {
		logger.info { "Velocity: ${player.phyBody.linearVel.display()}" }
	}

	/**
	 * Query gravity of world and gravity mode of (influence to) sphere
	 */
	private fun queryGravity() {
		logger.info { "Gravity: ${core.world!!.gravity.display()}; influence: ${player.phyBody.gravityMode}" }
	}

	/**
	 * Query friction states
	 */
	private fun queryFriction() {
		logger.info { "Friction: ${core.world!!.friction}; mode: ${core.world!!.frictionMode}" }
	}

	/**
	 * Reset velocity of sphere to zero
	 */
	private fun resetVel() {
		player.phyBody.linearVel = ZeroImmVec3d
		logger.info { "Reset velocity to zero" }
	}

	private lateinit var gravityModeListener: () -> Unit
	private lateinit var gravityListener: () -> Unit
	private lateinit var frictionModeListener: () -> Unit
	private lateinit var frictionListener: () -> Unit
	private lateinit var zoomLvlListener: () -> Unit

	private fun update0(muiIoI: ScreenManager.MuiIoI) {
		val inputSystem = muiIoI.inputSystem
		if (hotkeysEnabled) { // Those keys are not related to GUI, so they are fine to be here.
			if (inputSystem.condition { keyboard { Q.justDown } }) queryPos()
			if (inputSystem.condition { keyboard { R.justDown } }) queryVec()
			if (inputSystem.condition { keyboard { U.justDown } }) queryGravity()
			if (inputSystem.condition { keyboard { I.justDown } }) {
				// Toggle gravity mode of (influence to) sphere
				player.phyBody.gravityMode = !player.phyBody.gravityMode
				logger.info { "Gravity influence toggled: ${player.phyBody.gravityMode}" }
				gravityModeListener()
			}
			if (inputSystem.condition { keyboard { O.justDown } }) {
				// Increase world gravity
				if (-core.world!!.gravity.y < MAX_GRAVITY) {
					core.world!!.gravity *= 2.0
					logger.info {
						"Gravity increased: ${core.world!!.gravity.display()}".let {
							if (!player.phyBody.gravityMode) "$it (ineffective)" else it
						}
					}
					gravityListener()
				} else {
					logger.info {
						"Gravity maximized: ${core.world!!.gravity.display()}".let {
							if (!player.phyBody.gravityMode) "$it (ineffective)" else it
						}
					}
				}
			}
			if (inputSystem.condition { keyboard { P.justDown } }) {
				// Decrease world gravity
				if (-core.world!!.gravity.y > MIN_GRAVITY) {
					core.world!!.gravity /= 2.0
					logger.info {
						"Gravity decreased: ${core.world!!.gravity.display()}".let {
							if (!player.phyBody.gravityMode) "$it (ineffective)" else it
						}
					}
					gravityListener()
				} else {
					logger.info {
						"Gravity minimized: ${core.world!!.gravity.display()}".let {
							if (!player.phyBody.gravityMode) "$it (ineffective)" else it
						}
					}
				}
			}
			if (inputSystem.condition { keyboard { J.justDown } }) queryFriction()
			if (inputSystem.condition { keyboard { K.justDown } }) {
				// Toggle friction mode
				core.world!!.frictionMode = World.FrictionMode.entries[
					(core.world!!.frictionMode.ordinal + 1) % World.FrictionMode.entries.size
				]
				logger.info {
					"Friction mode toggled: ${core.world!!.frictionMode}".let {
						if (core.world!!.frictionMode == World.FrictionMode.Limited) "$it ; friction: ${core.world!!.friction}" else it
					}
				}
				frictionModeListener()
			}
			if (inputSystem.condition { keyboard { L.justDown } }) {
				// Increase friction (for Limited mode)
				if (core.world!!.friction < MAX_FRICTION) {
					core.world!!.friction *= 2
					logger.info {
						"Friction increased: ${core.world!!.friction}".let {
							if (core.world!!.frictionMode != World.FrictionMode.Limited) "$it (ineffective)" else it
						}
					}
					frictionListener()
				} else {
					logger.info {
						"Friction maximized: ${core.world!!.friction}".let {
							if (core.world!!.frictionMode != World.FrictionMode.Limited) "$it (ineffective)" else it
						}
					}
				}
			}
			if (inputSystem.condition { keyboard { M.justDown } }) {
				// Decrease friction (for Limited mode)
				if (core.world!!.friction > MIN_FRICTION) {
					core.world!!.friction /= 2
					logger.info {
						"Friction decreased: ${core.world!!.friction}".let {
							if (core.world!!.frictionMode != World.FrictionMode.Limited) "$it (ineffective)" else it
						}
					}
					frictionListener()
				} else {
					logger.info {
						"Friction minimized: ${core.world!!.friction}".let {
							if (core.world!!.frictionMode != World.FrictionMode.Limited) "$it (ineffective)" else it
						}
					}
				}
			}
			if (inputSystem.condition { keyboard { N.justDown } }) resetVel()
			// This is problematic and difficult to be resolved.
	// 		if (inputSystem.condition { Z.justDown() }) {
	// 			// Reset position of sphere to spawn point
	// 			player.pos = Vec3D(0.0, 1.0, 0.0)
	// 			logger.info { "Reset position to spawn point" }
	// 		}
			if (inputSystem.condition { keyboard { Equals.justDown } }) {
				// Zoom in camera
				if (camera.zoomLevel < MAX_ZOOM) {
					camera.zoomLevel *= 2
					logger.info { "Zoomed in: ${camera.zoomLevel}" }
					zoomLvlListener()
				} else {
					logger.info { "Zoom maximized: ${camera.zoomLevel}" }
				}
			}
			if (inputSystem.condition { keyboard { Minus.justDown } }) {
				// Zoom out camera
				if (camera.zoomLevel > MIN_ZOOM) {
					camera.zoomLevel /= 2
					logger.info { "Zoomed out: ${camera.zoomLevel}" }
					zoomLvlListener()
				} else {
					logger.info { "Zoom minimized: ${camera.zoomLevel}" }
				}
			}
		}

		val dirs = ArrayList<Vec3d>()
		Direction6C.entries.forEach { if (inputSystem.condition { keyboard { it.toKey().down } }) dirs.add(it.toVector()) }
		player.move(dirs.fold(ZeroImmVec3d, Vec3d::plus))

		synchronized(interactiveGeomsLock) { interactiveGeoms.forEach { it.drawable.setPos(it.pos) } }
		camera.refreshPos(player.pos.toImmVec3f().toArray())
	}

	private class ChunkManager {
		private val chunks = mutableMapOf<Vec3i, Octree<Object>>()

		private class Object(val geom: VoidGeom) : Octree.Data() {
			override val aabb get() = Cuboid(
				geom.drawable.aabb.first - geom.drawable.aabb.second / 2,
				geom.drawable.aabb.second.let { Dimension3D(it.x, it.y, it.z) },
			)

			override fun observeAabb(observer: () -> Unit) = geom.drawable.observeAabb(observer)

			override fun unobserveAabb(observer: () -> Unit) = geom.drawable.unobserveAabb(observer)
		}

		private val objects = mutableMapOf<VoidGeom, Object>()
		private val objectChunks = mutableMapOf<Object, Vec3i>()
		private val changedObjects = mutableSetOf<VoidGeom>()
		private val observers = mutableMapOf<VoidGeom, () -> Unit>()

		fun add(geom: VoidGeom) {
			addChunk(
				(geom.drawable.aabb.first / CHUNK_SIZE).floor(),
				Object(geom).apply { objects[geom] = this },
			)
			observers[geom] = {
				changedObjects.add(geom)
				Unit
			}.apply { geom.drawable.observeAabb(this) }
		}

		private fun addChunk(chunk: Vec3i, obj: Object) {
			objectChunks[obj] = chunk
			chunks.computeIfAbsent(chunk) {
				Octree((it * CHUNK_SIZE + CHUNK_SIZE / 2).toImmVec3d(), CHUNK_SIZE / 2.0, MIN_CELL_SIZE.toDouble())
			}.add(obj)
		}

		private fun removeFromChunk(obj: Object) {
			val chunk = objectChunks.remove(obj)!!
			chunks[chunk]!!.remove(obj)
			if (chunks[chunk]!!.isEmpty()) assert(chunks.remove(chunk) != null)
		}

		fun remove(geom: VoidGeom) {
			removeFromChunk(objects.remove(geom)!!)
			changedObjects.remove(geom)
			geom.drawable.unobserveAabb(observers.remove(geom)!!)
		}

		fun update() {
			changedObjects.forEach {
				val obj = objects[it]!!
				val cur = (it.drawable.aabb.first / CHUNK_SIZE).floor()
				if (objectChunks[obj]!! != cur) {
					removeFromChunk(obj)
					addChunk(cur, obj)
				}
			}
			changedObjects.clear()
			chunks.values.forEach { it.update() }
		}

		@OptIn(ExperimentalCoroutinesApi::class)
		fun filterCollidingObjects(range: Octree.GeometryRange3d) = chunks.entries.asFlow()
			.filter {
				range.intersects(Octree.Range(
					(it.key * CHUNK_SIZE).toImmVec3d(),
					((it.key + 1) * CHUNK_SIZE).toImmVec3d(),
				))
			}
			.flatMapMerge { it.value.filterCollidingObjects(range) }
			.map { it.geom }

		fun simpleFilterRangeObjects(range: Octree.GeometryRange3d) = chunks.asSequence()
			.filter {
				range.intersects(Octree.Range(
					(it.key * CHUNK_SIZE).toImmVec3d(),
					((it.key + 1) * CHUNK_SIZE).toImmVec3d(),
				))
			}
			.flatMap { it.value.simpleFilterRangeObjects(range) }
			.map { it.geom }
	}

	private inner class GameplayRenderer(
		renderSystemHandle: RenderSystem.Handle,
		inputStatesHandle: InputStatesHandle,
	) : Component(ComponentAsdHandleImpl()) {
		private val tpsText = TextContext(renderSystemHandle, TextContext.Config(16F, 16F, ImmVec4i(255)))

		private val mouseCtxStates = MouseCtxStates(inputStatesHandle.mouseGlobalStates, asdHandle).apply {
			// There is yet no input masking, so even it is triggered on another AGIMO, this is still triggered.
			addListener(MouseState.Listener(setOf(
				MouseState.Trigger(MouseState.Key.ButtonJustDown(MouseInputHandler.Buttons.Right.id)) { true },
			)) {
				require(it is MouseState.ButtonJustDown)
				assert(it.id == MouseInputHandler.Buttons.Right.id)
				val dir = (it.pos - ImmVec2d(asdHandle.rect.width / 2, asdHandle.rect.height / 2)).normalized().run {
					ImmVec3d(x, 0.0, -y) // window direction to world direction
				}
				val pos = player.pos + dir * (.5 + .25 + .01) // radius of char + radius of proj + small gap
				worldCommands.add(WorldCommand.AddProjection(pos, dir))
			})
		}

		init {
			asdHandle.observeRect {
				tpsText.update(asdHandle.rect)
			}
		}

		override fun render(renderSystem: RenderSystem) {
			if (core.world != null) {
				val range = camera.getSpace() * 1.1 // with little tolerance
				val ceil = 3
				val floor = 10
// 				object : Octree.GeometryRange3d, Closeable {
// 					private val space = CameraSpace(
// 						player.pos.toMutVec3d().apply { y -= floor - (ceil + floor).toDouble() / 2 },
// 						ImmVec3d(range.x, (ceil + floor).toDouble(), range.y),
// 					)
// 					private var closed = false
//
// 					override fun intersects(other: Octree.Range): Boolean {
// 						check(!closed)
// 						return space.intersects(other.min, other.max)
// 					}
//
// 					override fun close() {
// 						space.close()
// 						closed = true
// 					}
// 				}.use { range ->
// 					runBlocking {
// 						@OptIn(ExperimentalCoroutinesApi::class)
// 						chunkManager.filterCollidingObjects(range).chunked(200).flowOn(Dispatchers.Default).toList().flatten().sortedWith(
// 							compareBy<World.VoidGeom> { it.pos.y }.thenBy { it.pos.z }
// 						).forEach { it.render() }
// 					}
// 				}
				val center = player.pos.toMutVec3d().apply { y -= floor - (ceil + floor).toDouble() / 2 }
				val dims = ImmVec3d(range.x, (ceil + floor).toDouble(), range.y).let {
					// CameraSpace but its bounding box
					ImmVec3d(it.x, it.y, it.z + it.y * tan(PI / 6) * 2)
				}
				val cuboid = Cuboid(center - dims / 2, Dimension3D(dims.x, dims.y, dims.z))
				chunkManager.simpleFilterRangeObjects(Octree.Range(cuboid.pt, cuboid.max())).toList().sortedWith(
					compareBy<World.VoidGeom> { it.pos.y }.thenBy { it.pos.z }
				).forEach { it.render() }
			}

			tpsText.setText("${core.tps} FPS")
			tpsText.render()
		}
	}

	internal fun renderGwrGeo(drawable: WorldObjDrawable) = camera.renderGwrGeo(drawable, geoShaders)
}
