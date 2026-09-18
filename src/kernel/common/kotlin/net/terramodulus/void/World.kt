/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.void

import com.cout970.math.vec3.ImmVec3d
import com.cout970.math.vec3.Vec3d
import com.cout970.math.vec3.plus
import net.terramodulus.engine.PhyBody
import net.terramodulus.engine.PhyEnv
import net.terramodulus.engine.PhyGeom
import net.terramodulus.engine.PhyGeomBox
import net.terramodulus.util.logging.logger
import java.io.Closeable
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private val logger = logger {}

class World(commander: Ymir.Builder, progressBar: ProgressBar) : Closeable {
	private val env = PhyEnv()
	private val world = env.createWorld()
	var timePerTick = Duration.ZERO
		private set
	var tps = 0
		private set

	var gravity: Vec3d by world::gravity
	var frictionMode: FrictionMode by Delegates.observable(FrictionMode.Infinite) { _, _, new ->
		when (new) {
			FrictionMode.Zero -> world.setFriction(0.0)
			FrictionMode.Limited -> world.setFriction(friction)
			FrictionMode.Infinite -> world.setFriction(Double.POSITIVE_INFINITY)
		}
	}
	var friction: Double by Delegates.observable(1.0) { _, _, new ->
		if (frictionMode == FrictionMode.Limited) world.setFriction(new)
	}

	enum class FrictionMode {
		Zero, Limited, Infinite
	}

	val objects = HashMap<ObjId, VoidGeom>()
	val mainSpace = world.newSpace()
	// Floor at y=-100
	val floor = world.createGeomPlane(doubleArrayOf(0.0, 1.0, 0.0, -100.0))

	interface ProgressBar {
		fun addReadyListener(listener: () -> Unit)

		fun setProgress(progress: Double)
	}

	init {
		gravity = ImmVec3d(0.0, -9.81, 0.0)
		floor.setBits(1u, 1u.inv())
		world.omitSpace(mainSpace)
		val commander = commander.build(object : YmirAgent {
			override fun genCube(commander: Ymir, pos: Vec3d) {
				objects[ObjId.randomUnique(objects)] =
					commander.wrapCube(createCube(pos.x, pos.y, pos.z), pos)
			}

			override fun genChar(commander: Ymir, pos: Vec3d) {
				objects[ObjId.randomUnique(objects)] = commander.wrapChar(
					world.newBody(PhyBody.Mass.SphereTotal(1.0, .5)).apply {
						addGeom(createGeomSphere(.5))
						this.pos = pos
					},
					pos,
				)
			}
		})
		Thread {
			var ready = false // intermediate state to prevent cross-thread processing by listener invocation
			while (!ready) {
				progressBar.addReadyListener {
					ready = true
				}
				Thread.sleep(1)
			}
			commander.generateWorld(progressBar)
			// Running in parallel
			Thread {
				val timeSource = TimeSource.Monotonic
				val interval = 1.seconds / 20 // 20 Hz
				var lastMark = timeSource.markNow()
				var lastSec = timeSource.markNow()
				var ticks = 0
				while(true) {
					// uncalculated ticks are not accumulated at this stage, *skipped* instead
					tick()
					val now = timeSource.markNow()
					ticks++
					if (now - lastSec >= 1.seconds) {
						tps = ticks
						ticks = 0
						lastSec = now
					}
					// remaining time after elapsed time used to maintain stable interval
					val rem = interval - (now - lastMark)
					timePerTick = (now - lastMark)
					if (rem > Duration.ZERO) { // sleeps the remaining time only when it is positive
						Thread.sleep(rem.inWholeMilliseconds)
					}
					// makes sure timing does not include slept time
					lastMark = timeSource.markNow()
				}
			}.start()
		}.start()
	}

	interface Ymir {
		fun wrapCube(phyGeom: PhyGeom, pos: Vec3d): VoidGeom

		fun wrapChar(phyBody: PhyBody, pos: Vec3d): VoidGeom

		/**
		 * Caveat: This is run in parallel, so code involving any graphic context should not be included here.
		 */
		fun generateWorld(progressBar: ProgressBar)

		interface Builder {
			fun build(agent: YmirAgent): Ymir
		}
	}

	interface YmirAgent {
		fun genCube(commander: Ymir, pos: Vec3d)

		fun genChar(commander: Ymir, pos: Vec3d)
	}

	/** A wrapper containing rendering context, with a geom of dimensions of 1mx1mx1m */
	interface VoidGeom {
		fun render()

		val pos: Vec3d

		val phyGeoms: Sequence<PhyGeom>
	}

	interface EnvVoidGeom : VoidGeom {
		val phyGeom: PhyGeom
		override val phyGeoms: Sequence<PhyGeom> get() = sequenceOf(phyGeom)
	}

	interface PlayerVoidGeom : VoidGeom {
		val phyBody: PhyBody
		override val phyGeoms: Sequence<PhyGeom> get() = phyBody.geoms.asSequence()
	}

	private fun createCube(x: Double, y: Double, z: Double): PhyGeomBox {
		val cube = createGeomBox(1.0, 1.0, 1.0)
		cube.setPosition(doubleArrayOf(x, y, z))
		cube.setBits(1u, 1u.inv())
		return cube
	}

	internal fun createGeomBox(x: Double, y: Double, z: Double) = mainSpace.createGeomBox(doubleArrayOf(x, y, z))
	internal fun createGeomSphere(radius: Double) = world.createGeomSphere(radius)

	fun tick() = world.tick()

	override fun close() {
		env.close()
	}
}
