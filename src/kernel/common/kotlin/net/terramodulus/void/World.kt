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

class World(commander: Ymir, progressBar: ProgressBar) : Closeable {
	private val env = PhyEnv()
	private val world = env.createWorld()

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
		// Spawn point
		objects[ObjId.randomUnique(objects)] = commander.wrapCube(createCube(.0, .0, .0), .0, .0, .0)
		// Main Character
		objects[ObjId.randomUnique(objects)] = commander.wrapChar(
			world.newBody(PhyBody.Mass.SphereTotal(1.0, .5)).apply {
				addGeom(createGeomSphere(.5))
				pos = ImmVec3d(0.0, 1.0, 0.0)
			}
		)
		Thread {
			var ready = false // intermediate state to prevent cross-thread processing by listener invocation
			while (!ready) {
				progressBar.addReadyListener {
					ready = true
				}
				Thread.sleep(1)
			}
			// Test Objects
			randomCubes(commander, progressBar).forEach { objects[ObjId.randomUnique(objects)] = it }
			// Running in parallel
			Thread {
				val timeSource = TimeSource.Monotonic
				val interval = 1.seconds / 20 // 20 Hz
				var lastMark = timeSource.markNow()
				while(true) {
					// uncalculated ticks are not accumulated at this stage, *skipped* instead
					tick()
					val now = timeSource.markNow()
					// remaining time after elapsed time used to maintain stable interval
					val rem = interval - (now - lastMark)
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
		fun wrapCube(phyGeom: PhyGeom, x: Double, y: Double, z: Double): VoidGeom

		/** Always at (0, 1, 0) */
		fun wrapChar(phyBody: PhyBody): VoidGeom
	}

	/** A wrapper containing rendering context, with a geom of dimensions of 1mx1mx1m */
	interface VoidGeom {
		fun render()

		val pos: Vec3d
	}

	interface EnvVoidGeom : VoidGeom {
		val phyGeom: PhyGeom
	}

	interface PlayerVoidGeom : VoidGeom {
		val phyBody: PhyBody
	}

	// Source: https://en.wikipedia.org/wiki/Maze_generation_algorithm
	private fun randomCubes(commander: Ymir, progressBar: ProgressBar): ArrayList<VoidGeom> {
		val list = ArrayList<VoidGeom>()
		var i = 0
// 		val radius = 12
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
	// 						logger.info { "Generating: ${++i}/$total" }
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
								list.add(commander.wrapCube(createCube(pt.x, pt.y, pt.z), pt.x, pt.y, pt.z))
							}
						}
					}
				}
			}
		}
		return list
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
