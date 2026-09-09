/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import com.cout970.math.quaternion.ImmQuatd
import com.cout970.math.vec2.Vec2d
import com.cout970.math.vec3.ImmVec3d
import com.cout970.math.vec3.Vec3d
import com.cout970.math.vec3.Vec3f
import com.cout970.math.vec3.div
import com.cout970.math.vec3.dot
import com.cout970.math.vec3.normalized
import com.cout970.math.vec3.plus
import com.cout970.math.vec3.times
import com.cout970.math.vec3.toImmVec3f
import com.cout970.math.vec4.ImmVec4i
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
import net.terramodulus.mui.gui.agim.Component
import net.terramodulus.mui.gui.agim.Screen
import net.terramodulus.mui.gui.agim.ScreenManager
import net.terramodulus.mui.gui.agim.event.ScreenEvent
import net.terramodulus.mui.gui.asd.AsdHandle
import net.terramodulus.mui.gui.gfx.Direction6C
import net.terramodulus.mui.gui.gfx.GuiLine
import net.terramodulus.mui.gui.gfx.InsetsD
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.gfx.TextContext
import net.terramodulus.mui.kui.KeyboardInputHandler
import net.terramodulus.util.logging.logger
import net.terramodulus.void.World
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private val WHITE = ImmVec4i(255, 255, 255, 255)
private val RED = ImmVec4i(255, 0, 0, 255)
private val GREEN = ImmVec4i(0, 255, 0, 255)
private val BLUE = ImmVec4i(0, 0, 255, 255)
private val STD_SCALE = ImmVec3d(.5, .5, .5)
private val IDENT_ROT = ImmQuatd(1.0, .0, .0, .0)
private const val MASS = 1.0
private const val MAX_SPEED = PI * PI // reachable by autonomous movement
private const val MAX_ACC = PI * PI // without other forces, reaching MAX_SPEED in one second
private const val MOVE_EPSILON = .1 // smallest acc to apply
private const val MIN_GRAVITY = 1.0
private const val MAX_GRAVITY = 20.0
private const val MIN_FRICTION = 1.0 / 16.0
private const val MAX_FRICTION = 64.0
private const val MIN_ZOOM = 1.0 / 4.0
private const val MAX_ZOOM = 4

private val logger = logger {}

internal class GameplayScreen(
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

	private lateinit var player: PlayerVoidGeom
	override val layout = CompositeLayout(this)
	private val mouseDebugTrackingLayer = MouseDebugTrackingLayer(renderSystemHandle.canvasHandle, inputStatesHandle)

	init {
		renderSystemHandle.setBackgroundColor(0F, 0F, 0F, 0F)
		managerHandle.open { p1: ScreenManager.Handle, p2: AsdHandle.Container, p3: RenderSystem.Handle ->
			WorldInitScreen(p1, p2, p3).apply {
				core.world = World(Ymir(), progressBar)
				addListener(ScreenEvent.Close::class.java) {
					this@GameplayScreen.layout.update {
						add(SingletonLayout(
							this@GameplayScreen,
							GameplayRenderer(),
							SingletonLayout.Config.Absolute.Full,
						))
						add(SingletonLayout(
							this@GameplayScreen,
							ButtonComponent(ComponentAsdHandleImpl(), inputStatesHandle) {
								SingletonLayout(this, TextDisplayComponent(
									ComponentAsdHandleImpl(),
									renderSystemHandle,
									TextContext.Config(24.0F, 24.0F, ImmVec4i(255)),
								).apply {
									text = "Button"
								}, SingletonLayout.Config.Absolute.Full)
							},
							SingletonLayout.Config.Absolute.Insets(InsetsD(20.0, 0.0, 0.0, 300.0)),
						))
						add(SingletonLayout(
							this@GameplayScreen,
							SimplePane(ComponentAsdHandleImpl()) {
								SingletonLayout(this, SizedPane(
									ComponentAsdHandleImpl(),
									CheckboxComponent(
										ComponentAsdHandleImpl(),
										inputStatesHandle,
										renderSystemHandle.canvasHandle,
									) { mouseDebugTrackingLayer.enabled = it },
									SizedPane.Config(100u, 100u),
								), SingletonLayout.Config.Aligned(
									SingletonLayout.Config.ObjectFit.Contain,
									SingletonLayout.Config.AlignmentConfig.withX(0.0),
								))
							},
							SingletonLayout.Config.Absolute.Insets(InsetsD(20.0, 0.0, 0.0, 350.0)),
						))
						add(SingletonLayout(
							this@GameplayScreen,
							mouseDebugTrackingLayer,
							SingletonLayout.Config.Absolute.Full,
						))
					}
					this@GameplayScreen.addListener(ScreenEvent.Update::class.java) {
						update0(it.muiIoI)
					}
				}
			}
		}
	}

	private inner class MouseDebugTrackingLayer(
		private val handle: RenderSystem.CanvasHandle,
		inputStatesHandle: InputStatesHandle,
	) : Component(ComponentAsdHandleImpl()) {
		private var prevPos: Vec2d? = null
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
			}
			mouseCtxStates.addListener(MouseState.Listener(
				setOf(MouseState.Trigger(MouseState.Key.Movement) { true })
			) {
				when (it) {
					is MouseState.Movement -> {
						if (prevPos != null) {
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
			}
		}
	}

	private inner class Ymir : World.Ymir {
		private val cubeGeom = SimpleMesh3dGeomCube(canvasHandle.canvas, 2F)
		private val sphereGeom = SimpleMesh3dGeomSphere(canvasHandle.canvas, 1F)

		override fun wrapCube(phyGeom: PhyGeom, x: Double, y: Double, z: Double): VoidGeom = EnvVoidGeom(phyGeom,
			WorldObjDrawable(cubeGeom, randomColor(), ImmVec3d(x, y, z), STD_SCALE, IDENT_ROT),
			ImmVec3d(x, y, z),
		)

		private fun randomColor() = when (Random.nextInt(3)) {
			0 -> RED
			1 -> GREEN
			2 -> BLUE
			else -> throw AssertionError("Invalid color")
		}

		override fun wrapChar(phyBody: PhyBody): VoidGeom {
			player = PlayerVoidGeom(phyBody,
				WorldObjDrawable(sphereGeom, WHITE, ImmVec3d(0.0, 1.0, 0.0), STD_SCALE, IDENT_ROT)
			)
			return player
		}
	}

	private abstract inner class VoidGeom(val drawable: WorldObjDrawable) : World.VoidGeom {
		override fun render() {
			renderGwrGeo(drawable)
		}
	}

	private inner class EnvVoidGeom(override val phyGeom: PhyGeom, drawable: WorldObjDrawable, override val pos: Vec3d) :
		VoidGeom(drawable), World.EnvVoidGeom

	private inner class PlayerVoidGeom(override val phyBody: PhyBody, drawable: WorldObjDrawable) :
		VoidGeom(drawable), World.PlayerVoidGeom {
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
			if (projVel < MAX_SPEED) {
				// Let v_d be the delta velocity in direction of d,
				//     a_d be the delta acceleration to be made.
				// v_t = MAX_SPEED - v_p, must be positive
				// a_d = dir * clamp(v_t / 1 s, EPSILON, MAX)
				val deltaVel = MAX_SPEED - projVel
				val deltaAcc = dir * deltaVel.coerceIn(MOVE_EPSILON, MAX_ACC)
				phyBody.addForce(deltaAcc * MASS)
			}
		}

		override fun render() {
			drawable.setPos(phyBody.pos)
			camera.refreshPos(phyBody.pos.toImmVec3f().toArray())
			super.render()
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

	private fun update0(muiIoI: ScreenManager.MuiIoI) {
		val inputSystem = muiIoI.inputSystem
		// Those keys are not related to GUI, so they are fine to be here.
		if (inputSystem.condition { keyboard { Q.justDown } }) {
			// Query position of sphere
			logger.info { "Position: ${player.pos.display()}" }
		}
		if (inputSystem.condition { keyboard { R.justDown } }) {
			// Query velocity of sphere
			// Note: Acceleration is hard to be queried as force is zeroed after each world step
			logger.info { "Velocity: ${player.phyBody.linearVel.display()}" }
		}
		if (inputSystem.condition { keyboard { U.justDown } }) {
			// Query gravity of world and gravity mode of (influence to) sphere
			logger.info { "Gravity: ${core.world!!.gravity.display()}; influence: ${player.phyBody.gravityMode}" }
		}
		if (inputSystem.condition { keyboard { I.justDown } }) {
			// Toggle gravity mode of (influence to) sphere
			player.phyBody.gravityMode = !player.phyBody.gravityMode
			logger.info { "Gravity influence toggled: ${player.phyBody.gravityMode}" }
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
			} else {
				logger.info {
					"Gravity minimized: ${core.world!!.gravity.display()}".let {
						if (!player.phyBody.gravityMode) "$it (ineffective)" else it
					}
				}
			}
		}
		if (inputSystem.condition { keyboard { J.justDown } }) {
			// Query friction states
			logger.info { "Friction: ${core.world!!.friction}; mode: ${core.world!!.frictionMode}" }
		}
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
			} else {
				logger.info {
					"Friction minimized: ${core.world!!.friction}".let {
						if (core.world!!.frictionMode != World.FrictionMode.Limited) "$it (ineffective)" else it
					}
				}
			}
		}
		if (inputSystem.condition { keyboard { N.justDown } }) {
			// Reset velocity of sphere to zero
			player.phyBody.linearVel = ZeroImmVec3d
			logger.info { "Reset velocity to zero" }
		}
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
			} else {
				logger.info { "Zoom maximized: ${camera.zoomLevel}" }
			}
		}
		if (inputSystem.condition { keyboard { Minus.justDown } }) {
			// Zoom out camera
			if (camera.zoomLevel > MIN_ZOOM) {
				camera.zoomLevel /= 2
				logger.info { "Zoomed out: ${camera.zoomLevel}" }
			} else {
				logger.info { "Zoom minimized: ${camera.zoomLevel}" }
			}
		}

		val dirs = ArrayList<Vec3d>()
		Direction6C.entries.forEach { if (inputSystem.condition { keyboard { it.toKey().down } }) dirs.add(it.toVector()) }
		player.move(dirs.fold(ZeroImmVec3d, Vec3d::plus))
	}

	private inner class GameplayRenderer : Component(ComponentAsdHandleImpl()) {
		override fun render(renderSystem: RenderSystem) {
			if (core.world != null) core.world!!.objects.values.sortedWith(
				compareBy<World.VoidGeom> { it.pos.y }.thenBy { it.pos.z }
			).forEach { it.render() }
		}
	}

	internal fun renderGwrGeo(drawable: WorldObjDrawable) = camera.renderGwrGeo(drawable, geoShaders)
}
