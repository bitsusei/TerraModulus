/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.uid

import kotlin.collections.set

class KeyboardDevice internal constructor(override val id: Device.Id) : Device {
	internal class Key internal constructor() {
		var down = false
	}

	@JvmInline
	value class KeyId(private val id: UInt)

	private val keys = HashMap<KeyId, Key>()

	init {
		// Refers to ferricia::mui::KeyboardKey
		for (i in 0u..242u) keys[KeyId(i)] = Key()
	}

	internal fun getKey(key: KeyId) = keys[key]
	internal fun iterKeys() = keys.asSequence()
}
