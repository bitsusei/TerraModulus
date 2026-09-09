/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.util.TypedMap

/**
 * Instances of this class must be immutable, as simple values;
 * may be data classes, data objects, enums.
 */
abstract class AgimoProperty

fun <T : AgimoProperty> T.getPropertyKey() = AgimoPropertyMap.Key(javaClass)

class AgimoPropertyMap {
	private val properties = TypedMap<AgimoProperty, Key<out AgimoProperty>>()
	private val observers = HashMap<Key<out AgimoProperty>,
		LinkedHashSet<(AgimoProperty?, AgimoProperty?) -> Unit>>()

	class Key<T : AgimoProperty>(c: Class<T>) : TypedMap.Key<T>(c)

	@Suppress("UNCHECKED_CAST")
	fun <T: AgimoProperty> getProperty(key: Key<T>) = properties[key] as T?

	fun <T: AgimoProperty> putProperty(key: Key<T>, value: T) {
		@Suppress("UNCHECKED_CAST")
		val old = properties.put(key, value) as T?
		triggerPropertyObservers(key, old, value)
	}

	@Suppress("UNCHECKED_CAST")
	fun <T: AgimoProperty> removeProperty(key: Key<T>): T? {
		val old = properties.remove(key) as T?
		triggerPropertyObservers(key, old, null)
		return old
	}

	fun <T: AgimoProperty> containsProperty(key: Key<T>) = properties.containsKey(key)

	/**
	 * @see java.util.Map.compute
	 */
	inline fun <T: AgimoProperty> computeProperty(key: Key<T>, remappingFunction: (Key<T>, T?) -> T?) {
		val v = remappingFunction(key, getProperty(key))
		if (v === null) removeProperty(key) else putProperty(key, v)
	}

	/**
	 * @see java.util.Map.computeIfAbsent
	 */
	inline fun <T: AgimoProperty> computePropertyIfAbsent(key: Key<T>, mappingFunction: (Key<T>) -> T?) {
		if (!containsProperty(key)) {
			val v = mappingFunction(key)
			if (v !== null) putProperty(key, v)
		}
	}

	/**
	 * @see java.util.Map.computeIfPresent
	 */
	inline fun <T: AgimoProperty> computePropertyIfPresent(key: Key<T>, remappingFunction: (Key<T>, T) -> T?) {
		val v = getProperty(key)
		if (v !== null) {
			val v = remappingFunction(key, v)
			if (v !== null) putProperty(key, v)
		}
	}

	/**
	 * @see java.util.Map.merge
	 */
	inline fun <T: AgimoProperty> mergeProperty(key: Key<T>, value: T, remappingFunction: (T, T) -> T?) {
		val v = getProperty(key)
		if (v !== null) {
			val v = remappingFunction(v, value)
			if (v !== null) putProperty(key, v)
		} else putProperty(key, value)
	}

	fun <T: AgimoProperty> observeProperty(key: Key<T>, l: (T?, T?) -> Unit) {
		@Suppress("UNCHECKED_CAST")
		observers.computeIfAbsent(key) { LinkedHashSet() }.add(l as (AgimoProperty?, AgimoProperty?) -> Unit)
	}

	fun <T: AgimoProperty> unobserveProperty(key: Key<T>, l: (T?, T?) -> Unit) {
		observers[key]?.remove(l)
	}

	private fun <T: AgimoProperty> triggerPropertyObservers(key: Key<T>, old: T?, new: T?) {
		observers[key]?.forEach { it(old, new) }
	}

	@Suppress("UNCHECKED_CAST")
	fun asMap(): Map<Key<*>, AgimoProperty> = properties as Map<Key<*>, AgimoProperty>

}

@Suppress("UNCHECKED_CAST")
fun <T: AgimoProperty> Map<AgimoPropertyMap.Key<*>, AgimoProperty>.getProperty(key: AgimoPropertyMap.Key<T>) =
	this[key] as T?
