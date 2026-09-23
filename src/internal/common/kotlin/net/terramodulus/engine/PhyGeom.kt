/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import net.terramodulus.engine.ferricia.Physics.newSolePhyGeomBox
import net.terramodulus.engine.ferricia.Physics.newSpacePhyGeomBox
import net.terramodulus.engine.ferricia.Physics.newWorldPhyGeomBox
import net.terramodulus.engine.ferricia.Physics.newWorldPhyGeomPlane
import net.terramodulus.engine.ferricia.Physics.newWorldPhyGeomSphere
import net.terramodulus.engine.ferricia.Physics.setPhyGeomNonPlaceableBits
import net.terramodulus.engine.ferricia.Physics.setPhyGeomPlaceableBits
import net.terramodulus.engine.ferricia.Physics.setPhyRawGeomPlaceablePosition

sealed class PhyGeom(internal val handle: ULong) {
	@OptIn(ExperimentalUnsignedTypes::class)
	open fun setBits(category: UInt, collide: UInt) = setPhyGeomNonPlaceableBits(handle, uintArrayOf(category, collide))
}

sealed class PlaceablePhyGeom(handle: ULong) : PhyGeom(handle) {
	fun setPosition(pos: DoubleArray) = setPhyRawGeomPlaceablePosition(handle, pos)
	@OptIn(ExperimentalUnsignedTypes::class)
	final override fun setBits(category: UInt, collide: UInt) = setPhyGeomPlaceableBits(handle, uintArrayOf(category, collide))
}

class PhyGeomBox internal constructor(handle: ULong) : PlaceablePhyGeom(handle) {
	companion object {
		internal fun newWorld(worldHandle: ULong, lengths: DoubleArray) = PhyGeomBox(newWorldPhyGeomBox(worldHandle, lengths))
		internal fun newSpace(spaceHandle: ULong, lengths: DoubleArray) = PhyGeomBox(newSpacePhyGeomBox(spaceHandle, lengths))
		fun newSole(lengths: DoubleArray) = PhyGeomBox(newSolePhyGeomBox(lengths))
	}
}

class PhyGeomSphere internal constructor(worldHandle: ULong, radius: Double) : PlaceablePhyGeom(newWorldPhyGeomSphere(worldHandle, radius))

class PhyGeomPlane internal constructor(worldHandle: ULong, params: DoubleArray) : PhyGeom(newWorldPhyGeomPlane(worldHandle, params))
