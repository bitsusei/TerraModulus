/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.kui

import net.terramodulus.mui.uid.KeyboardDevice

class KeyboardInputHandler internal constructor(keyboardDevice: KeyboardDevice) {
	typealias KeyId = KeyboardDevice.KeyId

	sealed class KeyPredicate : InputSystem.InputPredicateBase<KeyPredicate.Helper, KeyPredicate>() {
		// Automatically asserted helper
		class Helper internal constructor(private val keys: Map<KeyId, Key>) : InputSystem.InputPredicateBase.Helper {
			operator fun get(key: KeyId) = keys[key]!!
		}

		@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
		abstract override fun test(keys: Helper): Boolean

		data class Down(val x: KeyId) : KeyPredicate() {
			override fun test(keys: Helper) = keys[x].down
		}

		data class JustDown(val x: KeyId) : KeyPredicate() {
			override fun test(keys: Helper) = keys[x].justDown
		}

		data class JustUp(val x: KeyId) : KeyPredicate() {
			override fun test(keys: Helper) = keys[x].justUp
		}
	}

	sealed interface InnerKeys {
		val down: KeyPredicate.Down
		val justDown: KeyPredicate.JustDown
		val justUp: KeyPredicate.JustUp
		fun matches(other: KeyId): Boolean // Is this useful?
	}

	private class KeysImpl(private val id: KeyId) : InnerKeys {
		override val down get() = KeyPredicate.Down(id)
		override val justDown get() = KeyPredicate.JustDown(id)
		override val justUp get() = KeyPredicate.JustUp(id)
		override fun matches(other: KeyId) = id == other
	}

	// Values refer to ferricia::mui::KeyboardKey
	enum class Keys(private val id: KeyId) : InnerKeys by KeysImpl(id) {
		A(KeyId(0u)),
		B(KeyId(1u)),
		C(KeyId(2u)),
		D(KeyId(3u)),
		E(KeyId(4u)),
		F(KeyId(5u)),
		G(KeyId(6u)),
		H(KeyId(7u)),
		I(KeyId(8u)),
		J(KeyId(9u)),
		K(KeyId(10u)),
		L(KeyId(11u)),
		M(KeyId(12u)),
		N(KeyId(13u)),
		O(KeyId(14u)),
		P(KeyId(15u)),
		Q(KeyId(16u)),
		R(KeyId(17u)),
		S(KeyId(18u)),
		T(KeyId(19u)),
		U(KeyId(20u)),
		V(KeyId(21u)),
		W(KeyId(22u)),
		X(KeyId(23u)),
		Y(KeyId(24u)),
		Z(KeyId(25u)),
		Space(KeyId(40u)),
		Minus(KeyId(41u)),
		Equals(KeyId(42u)),
		LShift(KeyId(205u)),
	}

	object KeysScope {
		val A = Keys.A
		val B = Keys.B
		val C = Keys.C
		val D = Keys.D
		val E = Keys.E
		val F = Keys.F
		val G = Keys.G
		val H = Keys.H
		val I = Keys.I
		val J = Keys.J
		val K = Keys.K
		val L = Keys.L
		val M = Keys.M
		val N = Keys.N
		val O = Keys.O
		val P = Keys.P
		val Q = Keys.Q
		val R = Keys.R
		val S = Keys.S
		val T = Keys.T
		val U = Keys.U
		val V = Keys.V
		val W = Keys.W
		val X = Keys.X
		val Y = Keys.Y
		val Z = Keys.Z
		val Space = Keys.Space
		val Minus = Keys.Minus
		val Equals = Keys.Equals
		val LShift = Keys.LShift
	}

	private val keys = HashMap<KeyId, Key>()

	init {
		keyboardDevice.iterKeys().forEach { (k, v) ->
			keys[k] = Key(v)
		}
	}

	// Large difference if specific keyboards can be specifically handled
	class Key internal constructor(internal val raw: KeyboardDevice.Key) {
		val down: Boolean get() = raw.down
		internal var justChanged = false

		val justDown: Boolean get() = down && justChanged
		val justUp: Boolean get() = !down && justChanged
	}

	fun condition(predicate: KeysScope.() -> KeyPredicate) = KeysScope.predicate().test(KeyPredicate.Helper(keys))

	sealed class KeyEvent private constructor(internal open val key: KeyId) {
		data class Down(override val key: KeyId) : KeyEvent(key)
		data class Up(override val key: KeyId) : KeyEvent(key)
	}

	internal fun update(events: Sequence<KeyEvent>) {
		keys.values.forEach { it.justChanged = false }
		events.forEach {
			// Note: This may not handle the case where a key is just down less than a tick.
			// This also assumes that keyboard states are consistent across time frames.
			keys[it.key]!!.justChanged = true
			when (it) {
				is KeyEvent.Down -> keys[it.key]!!.raw.down = true
				is KeyEvent.Up -> keys[it.key]!!.raw.down = false
			}
		}
	}
}
