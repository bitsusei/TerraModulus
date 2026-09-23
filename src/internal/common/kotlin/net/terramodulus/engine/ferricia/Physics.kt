/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine.ferricia

@OptIn(ExperimentalUnsignedTypes::class)
internal object Physics {
	/**
	 * @return PhyEnv pointer
	 */
	@JvmName("newPhyEnv")
	external fun newPhyEnv(): ULong

	@JvmName("dropPhyEnv")
	external fun dropPhyEnv(handle: ULong)

	/**
	 * @return PhyCollisionManager pointer
	 */
	@JvmName("newPhyCollisionManager")
	external fun newPhyCollisionManager(): ULong

	/**
	 * @param handle PhyCollisionManager pointer
	 * @param worldHandle PhyWorld pointer
	 */
	@JvmName("processPhyCollisionManager")
	external fun processPhyCollisionManager(handle: ULong, worldHandle: ULong)

	/**
	 * @param handle PhyCollisionManager pointer
	 * @param friction Coulomb friction coefficient, mu
	 */
	@JvmName("setPhyCollisionManagerFriction")
	external fun setPhyCollisionManagerFriction(handle: ULong, friction: Double)

	/**
	 * @param handle PhyCollisionManager pointer
	 * @param spaceHandle OdeSpace pointer
	 */
	@JvmName("omitPhyCollisionManagerSpace")
	external fun omitPhyCollisionManagerSpace(handle: ULong, spaceHandle: ULong)

	/**
	 * @param handle PhyEnv pointer
	 * @return PhyWorld pointer
	 */
	@JvmName("newPhyWorld")
	external fun newPhyWorld(handle: ULong): ULong

	/**
	 * @param handle PhyWorld pointer
	 * @param gravity `[x, y, z]`
	 */
	@JvmName("setPhyWorldGravity")
	external fun setPhyWorldGravity(handle: ULong, gravity: DoubleArray)

	/**
	 * @param handle PhyWorld pointer
	 * @param cmHandle PhyCollisionManager pointer
	 */
	@JvmName("tickPhyWorld")
	external fun tickPhyWorld(handle: ULong, cmHandle: ULong)

	/**
	 * @param mass total mass value
	 * @param radius radius of sphere
	 * @return OdeMass pointer
	 */
	@JvmName("newMassSphereTotal")
	external fun newMassSphereTotal(mass: Double, radius: Double): ULong

	/**
	 * @param handle PhyWorld pointer
	 * @return OdeSpace pointer
	 */
	@JvmName("newPhyWorldSpace")
	external fun newPhyWorldSpace(handle: ULong): ULong

	/**
	 * @param handle PhyWorld pointer
	 * @param massHandle OdeMass pointer; this is consumed and dropped
	 * @return PhyBody pointer
	 */
	@JvmName("newPhyBody")
	external fun newPhyBody(handle: ULong, massHandle: ULong): ULong

	/**
	 * @param handle PhyBody pointer
	 * @param geomHandle PhyRawGeomPlaceable pointer
	 */
	@JvmName("addPhyBodyGeom")
	external fun addPhyBodyGeom(handle: ULong, geomHandle: ULong)

	/**
	 * @param handle PhyBody pointer; OdeBody must be valid
	 * @param data [x, y, z]
	 */
	@JvmName("setPhyBodyPos")
	external fun setPhyBodyPos(handle: ULong, data: DoubleArray)

	/**
	 * @param handle PhyBody pointer; OdeBody must be valid
	 * @param data [x, y, z]
	 */
	@JvmName("setPhyBodyLinearVel")
	external fun setPhyBodyLinearVel(handle: ULong, data: DoubleArray)

	/**
	 * @param handle PhyBody pointer; OdeBody must be valid
	 * @param mode whether the World's gravity influences
	 */
	@JvmName("setPhyBodyGravityMode")
	external fun setPhyBodyGravityMode(handle: ULong, mode: Boolean)

	/**
	 * @param handle PhyBody pointer; OdeBody must be valid
	 */
	@JvmName("getPhyBodyPos")
	external fun getPhyBodyPos(handle: ULong): DoubleArray

	/**
	 * @param handle PhyBody pointer; OdeBody must be valid
	 */
	@JvmName("getPhyBodyLinearVel")
	external fun getPhyBodyLinearVel(handle: ULong): DoubleArray

	/**
	 * @param handle PhyBody pointer; OdeBody must be valid
	 * @param data [x, y, z]
	 */
	@JvmName("addPhyBodyForce")
	external fun addPhyBodyForce(handle: ULong, data: DoubleArray)

	/**
	 * @param handle PhyWorld pointer
	 * @param lengths x, y, z lengths
	 * @return PhyRawGeomPlaceable pointer
	 */
	@JvmName("newWorldPhyGeomBox")
	external fun newWorldPhyGeomBox(handle: ULong, lengths: DoubleArray): ULong

	/**
	 * @param handle OdeSpace pointer
	 * @param lengths x, y, z lengths
	 * @return PhyRawGeomPlaceable pointer
	 */
	@JvmName("newSpacePhyGeomBox")
	external fun newSpacePhyGeomBox(handle: ULong, lengths: DoubleArray): ULong

	/**
	 * @param lengths x, y, z lengths
	 * @return PhyRawGeomPlaceable pointer
	 */
	@JvmName("newSolePhyGeomBox")
	external fun newSolePhyGeomBox(lengths: DoubleArray): ULong

	/**
	 * @param handle PhyWorld pointer
	 * @return PhyRawGeomPlaceable pointer
	 */
	@JvmName("newWorldPhyGeomSphere")
	external fun newWorldPhyGeomSphere(handle: ULong, radius: Double): ULong

	/**
	 * @param handle PhyWorld pointer
	 * @param params `[a, b, c, d]`, where `a*x+b*y+c*z = d`, and (a, b, c) as normal unit vector
	 * @return PhyRawGeomNonPlaceable pointer
	 */
	@JvmName("newWorldPhyGeomPlane")
	external fun newWorldPhyGeomPlane(handle: ULong, params: DoubleArray): ULong

	/**
	 * @param handle PhyRawGeomNonPlaceable pointer
	 * @param bits `[a, b]`, where `a` is "category" bits and `b` is "collide" bits
	 */
	@JvmName("setPhyGeomNonPlaceableBits")
	external fun setPhyGeomNonPlaceableBits(handle: ULong, bits: UIntArray)

	/**
	 * @param handle PhyRawGeomPlaceable pointer
	 * @param bits `[a, b]`, where `a` is "category" bits and `b` is "collide" bits
	 */
	@JvmName("setPhyGeomPlaceableBits")
	external fun setPhyGeomPlaceableBits(handle: ULong, bits: UIntArray)

	/**
	 * @param handle PhyRawGeomPlaceable pointer
	 * @param pos x, y, z position
	 */
	@JvmName("setPhyRawGeomPlaceablePosition")
	external fun setPhyRawGeomPlaceablePosition(handle: ULong, pos: DoubleArray)

	/**
	 * @param handle PhyRawGeomPlaceable pointer
	 * @return x, y, z position
	 */
	@JvmName("getPhyRawGeomPlaceablePosition")
	external fun getPhyRawGeomPlaceablePosition(handle: ULong): DoubleArray

	/**
	 * @param handle PhyWorld pointer
	 * @return StaticSpaceSet pointer
	 */
	@JvmName("newPhyWorldStaticSpaceSet")
	external fun newPhyWorldStaticSpaceSet(handle: ULong): ULong

	/**
	 * @param setHandle StaticSpaceSet pointer
	 * @param geomHandle PhyRawGeomPlaceable pointer
	 */
	@JvmName("addStaticSpaceSetGeom")
	external fun addStaticSpaceSetGeom(setHandle: ULong, geomHandle: ULong)

	/**
	 * @param setHandle StaticSpaceSet pointer
	 * @param geomHandle PhyRawGeomPlaceable pointer
	 */
	@JvmName("removeStaticSpaceSetGeom")
	external fun removeStaticSpaceSetGeom(setHandle: ULong, geomHandle: ULong)

	/**
	 * @param setHandle StaticSpaceSet pointer
	 * @param cmHandle PhyCollisionManager pointer
	 */
	@JvmName("updateStaticSpaceSetIgnored")
	external fun updateStaticSpaceSetIgnored(setHandle: ULong, cmHandle: ULong)
}
