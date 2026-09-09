/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec3.Vec3d
import net.terramodulus.engine.common.ZeroImmVec3d
import net.terramodulus.engine.common.toArray
import net.terramodulus.engine.ferricia.Physics.newPhyCollisionManager
import net.terramodulus.engine.ferricia.Physics.newPhyWorld
import net.terramodulus.engine.ferricia.Physics.omitPhyCollisionManagerSpace
import net.terramodulus.engine.ferricia.Physics.processPhyCollisionManager
import net.terramodulus.engine.ferricia.Physics.setPhyCollisionManagerFriction
import net.terramodulus.engine.ferricia.Physics.setPhyWorldGravity
import net.terramodulus.engine.ferricia.Physics.tickPhyWorld
import kotlin.properties.Delegates

class PhyWorld internal constructor(envHandle: ULong) {
	private val handle = newPhyWorld(envHandle)
	private val cmHandle = newPhyCollisionManager()

	var gravity: Vec3d by Delegates.observable(ZeroImmVec3d) { _, _, newValue ->
		setPhyWorldGravity(handle, newValue.toArray())
	}

	fun createGeomBox(lengths: DoubleArray) = PhyGeomBox.newWorld(handle, lengths)
	fun createGeomSphere(radius: Double) = PhyGeomSphere(handle, radius)
	fun createGeomPlane(params: DoubleArray) = PhyGeomPlane(handle, params)

	fun newSpace() = PhySpace(handle)
	fun newBody(mass: PhyBody.Mass) = PhyBody(handle, mass)

	fun setFriction(friction: Double) = setPhyCollisionManagerFriction(handle, friction)
	fun omitSpace(space: PhySpace) = omitPhyCollisionManagerSpace(cmHandle, space.handle)

	fun tick() {
		tickPhyWorld(handle, cmHandle)
		processPhyCollisionManager(cmHandle, handle)
	}
}
