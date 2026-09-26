/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import net.terramodulus.engine.ferricia.Physics.addStaticSpaceSetGeom
import net.terramodulus.engine.ferricia.Physics.removeStaticSpaceSetGeom
import net.terramodulus.engine.ferricia.Physics.updateStaticSpaceSetIgnored

class StaticSpaceSet internal constructor(private val handle: ULong) {
	fun addGeom(geom: PlaceablePhyGeom) = addStaticSpaceSetGeom(handle, geom.handle)

	fun removeGeom(geom: PlaceablePhyGeom) = removeStaticSpaceSetGeom(handle, geom.handle)

	internal fun updateIgnored(cm: ULong) = updateStaticSpaceSetIgnored(handle, cm)
}
