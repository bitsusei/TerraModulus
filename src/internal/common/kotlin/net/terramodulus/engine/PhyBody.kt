/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import com.cout970.math.vec3.Vec3d
import net.terramodulus.engine.common.ImmVec3dFromArray
import net.terramodulus.engine.common.toArray
import net.terramodulus.engine.ferricia.Physics.addPhyBodyForce
import net.terramodulus.engine.ferricia.Physics.addPhyBodyGeom
import net.terramodulus.engine.ferricia.Physics.getPhyBodyLinearVel
import net.terramodulus.engine.ferricia.Physics.getPhyBodyPos
import net.terramodulus.engine.ferricia.Physics.newMassSphereTotal
import net.terramodulus.engine.ferricia.Physics.newPhyBody
import net.terramodulus.engine.ferricia.Physics.setPhyBodyGravityMode
import net.terramodulus.engine.ferricia.Physics.setPhyBodyLinearVel
import net.terramodulus.engine.ferricia.Physics.setPhyBodyPos
import kotlin.properties.Delegates

class PhyBody internal constructor(worldHandle: ULong, mass: Mass) {
	private val handle: ULong = newPhyBody(worldHandle, mass.handle)
	sealed class Mass(internal val handle: ULong) {
		class SphereTotal(mass: Double, radius: Double) : Mass(newMassSphereTotal(mass, radius))
	}

	var pos: Vec3d
		get() = ImmVec3dFromArray(getPhyBodyPos(handle))
		set(value) = setPhyBodyPos(handle, value.toArray())

	var linearVel: Vec3d
		get() = ImmVec3dFromArray(getPhyBodyLinearVel(handle))
		set(value) = setPhyBodyLinearVel(handle, value.toArray())

	var gravityMode: Boolean by Delegates.observable(true) { _, _, newValue ->
		setPhyBodyGravityMode(handle, newValue)
	}

	fun addGeom(geom: PhyGeom) = addPhyBodyGeom(handle, geom.handle)

	fun addForce(force: Vec3d) = addPhyBodyForce(handle, force.toArray())
}
