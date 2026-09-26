/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.kui

// TODO Tentative solution, may be rewritten later.
/**
 * Simple integrated access interface to various input handlers
 */
class InputSystem internal constructor(private val kuiManager: KuiManager) {
	sealed class InputPredicateBase<H : InputPredicateBase.Helper, P : InputPredicateBase<H, P>> {
		sealed interface Helper

		internal abstract fun test(helper: H): Boolean

		class And<H : Helper, P : InputPredicateBase<H, P>>(val x: P, val y: P) : InputPredicateBase<H, P>() {
			override fun test(helper: H) = x.test(helper) && y.test(helper)
		}

		class Or<H : Helper, P : InputPredicateBase<H, P>>(val x: P, val y: P) : InputPredicateBase<H, P>() {
			override fun test(helper: H) = x.test(helper) || y.test(helper)
		}

		class Not<H : Helper, P : InputPredicateBase<H, P>>(val x: P) : InputPredicateBase<H, P>() {
			override fun test(helper: H) = !x.test(helper)
		}

		@Suppress("UNCHECKED_CAST")
		operator fun not() = Not(this as P)
		@Suppress("UNCHECKED_CAST")
		infix fun and(other: P) = And(this as P, other)
		@Suppress("UNCHECKED_CAST")
		infix fun or(other: P) = Or(this as P, other)
	}

	inner class InputsScope {
		fun keyboard(predicate: KeyboardInputHandler.KeysScope.() -> KeyboardInputHandler.KeyPredicate) =
			kuiManager.keyboardInputHandler.condition(predicate)
	}

	private val inputsScope = InputsScope()

	fun condition(predicate: InputsScope.() -> Boolean) = inputsScope.predicate()

	internal sealed class InputEvent private constructor() {
		data class Keyboard(val inner: KeyboardInputHandler.KeyEvent) : InputEvent()
		data class Mouse(val inner: MouseInputHandler.Event) : InputEvent()
	}

	internal fun update(events: Sequence<InputEvent>) {
		kuiManager.keyboardInputHandler.update(events.filterIsInstance<InputEvent.Keyboard>().map { it.inner })
	}
}
