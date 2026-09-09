/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.util

import java.util.function.BiFunction
import java.util.function.Function

/**
 * A map with access by references to unique keys with type of values specified,
 * matching the pattern of **Typed Anchor Dynamic Mapping**.
 * Localized constrains may be applied by copying this universal structure.\
 * All bulk functions are all unsupported for type safety.
 * @param T base class for map values
 */
open class TypedMap<T, K : TypedMap.Key<out T>>
	private constructor(private val map: MutableMap<K, T>) : MutableMap<K, T> by map {
	constructor() : this(HashMap())

	open class Simple<T> : TypedMap<T, Key<out T>>()

	/**
	 * A unique, immutable key that defines and enforces the type for values of a [TypedMap].
	 * By hiding public access to the map instance, constrains may be applied by subclassing this.
	 */
	open class Key<U>(val type: Class<U>) {
		companion object {
			inline operator fun <reified T : Any> invoke() = Key(T::class.java)
		}
		override fun hashCode() = type.hashCode()
		override fun equals(other: Any?): Boolean {
			if (other === null) return false
			if (this === other) return true
			if (other !is Key<*>) return false
			if (javaClass != other.javaClass) return false
			return type == other.type
		}
	}
}
