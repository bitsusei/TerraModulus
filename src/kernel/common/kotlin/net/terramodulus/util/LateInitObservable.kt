/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LateInitObservable<T : Any>(
	private val onChange: (prop: KProperty<*>, old: T?, new: T) -> Unit
) : ReadWriteProperty<Any?, T> {
	private var value: T? = null

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return value ?: throw IllegalStateException("Property ${property.name} is not initialized.")
	}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		val oldValue = this.value
		this.value = value
		onChange(property, oldValue, value)
	}
}

fun <T : Any> lateInitObservable(onChange: (prop: KProperty<*>, old: T?, new: T) -> Unit) = LateInitObservable(onChange)
