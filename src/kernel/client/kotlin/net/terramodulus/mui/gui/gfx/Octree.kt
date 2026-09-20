/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

import com.cout970.math.vec3.ImmVec3d
import com.cout970.math.vec3.Vec3d
import com.cout970.math.vec3.minus
import com.cout970.math.vec3.plus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.merge
import kotlin.math.max
import kotlin.math.min

private const val LOOSENESS = 1.5
private const val THRESHOLD = 8

/**
 * Octree
 *
 * References:
 * - https://en.wikipedia.org/wiki/Octree
 * - https://github.com/jglrxavpok/kotlin-dynamic-octree
 * - https://github.com/mcserep/NetOctree
 */
class Octree<D : Octree.Data>(center: Vec3d, halfLength: Double, private val minSize: Double) {
	interface GeometryRange3d {
		fun intersects(other: Range): Boolean
	}

	/**
	 * Cuboid expressed in a range enclosed by two corners
	 */
	data class Range(val min: Vec3d, val max: Vec3d) : GeometryRange3d {
		override fun intersects(other: Range) = max(min.x, other.min.x) <= min(max.x, other.max.x) &&
			max(min.y, other.min.y) <= min(max.y, other.max.y) &&
			max(min.z, other.min.z) <= min(max.z, other.max.z)

		fun contains(other: Range) = min.x <= other.min.x && max.x >= other.max.x &&
			min.y <= other.min.y && max.y >= other.max.y &&
			min.z <= other.min.z && max.z >= other.max.z
	}

	private data class NodeRange(val center: Vec3d, val halfLength: Double) {
		fun toRange() = Range(center - halfLength, center + halfLength)
	}

	private data class BranchKey(val x: Direction2S, val y: Direction2S, val z: Direction2S)

	private inner class Node(val range: NodeRange) {
		val looseRange = NodeRange(range.center, range.halfLength * LOOSENESS)

		private var branches: Branches? = null

		inner class Branches {
			private val branches = buildMap {
				val quarter = range.halfLength / 2
				Direction2S.entries.forEach { x ->
					Direction2S.entries.forEach { y ->
						Direction2S.entries.forEach { z ->
							put(BranchKey(x, y, z), Node(NodeRange(ImmVec3d(
								range.center.x + when (x) {
									Direction2S.Positive -> quarter
									Direction2S.Negative -> -quarter
								},
								range.center.y + when (y) {
									Direction2S.Positive -> quarter
									Direction2S.Negative -> -quarter
								},
								range.center.z + when (z) {
									Direction2S.Positive -> quarter
									Direction2S.Negative -> -quarter
								},
							), quarter)))
						}
					}
				}
			}

			fun isEmpty() = branches.values.all { it.isEmpty() }

			/**
			 * Tries to add [leaf] to a branch if any branch fits.
			 */
			fun tryAdd(leaf: D): Boolean {
				val ctr = leaf.aabb.center()
				val x = ctr.x.compareTo(range.center.x).let {
					when {
						it > 0 -> Direction2S.Positive
						it < 0 -> Direction2S.Negative
						else -> null
					}
				}
				val y = ctr.x.compareTo(range.center.y).let {
					when {
						it > 0 -> Direction2S.Positive
						it < 0 -> Direction2S.Negative
						else -> null
					}
				}
				val z = ctr.x.compareTo(range.center.z).let {
					when {
						it > 0 -> Direction2S.Positive
						it < 0 -> Direction2S.Negative
						else -> null
					}
				}

				if (x != null && y != null && z != null) {
					val branch = branches[BranchKey(x, y, z)]!!
					if (branch.range.toRange().contains(leaf.range)) {
						branch.add(leaf)
						return true
					}
				}

				return false
			}

			@OptIn(ExperimentalCoroutinesApi::class)
			fun filterCollidingObjects(range: GeometryRange3d) =
				branches.values.asFlow()
					.filter { range.intersects(it.looseRange.toRange()) }
					.flatMapMerge { it.filterCollidingObjects(range) }

			fun simpleFilterRangeObjects(range: GeometryRange3d) =
				branches.values.asSequence()
					.filter { range.intersects(it.looseRange.toRange()) }
					.flatMap { it.simpleFilterRangeObjects(range) }
		}

		private val directObjects = mutableSetOf<D>()

		fun isEmpty(): Boolean = directObjects.isEmpty() && branches.let { it == null || it.isEmpty() }

		fun contains(range: Range) = this.looseRange.toRange().contains(range)

		fun add(leaf: D) {
			objects.computeIfAbsent(leaf) { mutableListOf() }.add(this)
			if (branches == null && directObjects.size > THRESHOLD && range.halfLength >= minSize) {
				branches = Branches().apply {
					directObjects.iterator().run {
						while (hasNext()) {
							val e = next()
							if (tryAdd(e)) remove()
						}
					}
				}
			}

			if (branches?.tryAdd(leaf) != true)
				directObjects.add(leaf)
		}

		/**
		 * Checks if [leaf] can fit a branch if any, then moves.
		 */
		fun adjustFit(leaf: D) = branches?.tryAdd(leaf) == true

		/**
		 * Caveat: Shall ensure that [leaf] is directly stored here.
		 */
		fun remove(leaf: D) = assert(directObjects.remove(leaf))

		fun clearBranchesIfEmpty() {
			if (branches.let { it != null && it.isEmpty() }) branches = null
		}

		fun filterCollidingObjects(range: GeometryRange3d): Flow<D> {
			val seq = directObjects.asFlow().filter { range.intersects(it.range) }
			return branches.let { if (it == null) seq else merge(seq, it.filterCollidingObjects(range)) }
		}

		fun simpleFilterRangeObjects(range: GeometryRange3d): Sequence<D> {
			val seq = directObjects.asSequence().filter { range.intersects(it.range) }
			return branches.let { if (it == null) seq else seq + it.simpleFilterRangeObjects(range) }
		}
	}

	abstract class Data {
		abstract val aabb: Cuboid

		val range get() = Range(aabb.pt, aabb.max())

		abstract fun observeAabb(observer: () -> Unit)

		abstract fun unobserveAabb(observer: () -> Unit)
	}

	private val rootNode = Node(NodeRange(center, halfLength))

	private val objects = mutableMapOf<D, MutableList<Node>>()

	private val changedObjects = mutableSetOf<D>()
	private val observers = mutableMapOf<D, () -> Unit>()

	fun add(leaf: D) {
		assert(!objects.contains(leaf))
		rootNode.add(leaf)
		observers[leaf] = {
			changedObjects.add(leaf)
			Unit
		}.apply { leaf.observeAabb(this) }
	}

	fun remove(leaf: D) {
		assert(objects.contains(leaf))
		changedObjects.remove(leaf)
		leaf.unobserveAabb(observers.remove(leaf)!!)
		removeNested(objects.remove(leaf)!!, leaf)
	}

	fun isEmpty() = rootNode.isEmpty()

	private fun removeNested(list: List<Node>, leaf: D) {
		list.last().remove(leaf)
		if (list.size > 1) list[list.size - 2].clearBranchesIfEmpty()
	}

	fun filterCollidingObjects(range: GeometryRange3d) = rootNode.filterCollidingObjects(range)

	fun simpleFilterRangeObjects(range: GeometryRange3d) = rootNode.simpleFilterRangeObjects(range)

	fun update() {
		changedObjects.forEach {
			objects[it]!!.run {
				var last = last()
				if (!last.adjustFit(it) && !last.contains(it.range)) {
					removeNested(this, it)
					removeLast()
					while (size > 1) {
						last = last()
						if (last.range.toRange().contains(it.range)) break
						removeLast()
					}
					last.add(it)
				}
			}
		}
		changedObjects.clear()
	}
}
