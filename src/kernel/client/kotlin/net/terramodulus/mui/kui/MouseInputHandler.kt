/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.kui

import net.terramodulus.mui.uid.MouseDevice
import kotlin.sequences.forEach

class MouseInputHandler(mouseDevice: MouseDevice) {
	typealias ButtonId = MouseDevice.ButtonId

	sealed class ButtonPredicate : InputSystem.InputPredicateBase<ButtonPredicate.Helper, ButtonPredicate>() {
		// Automatically asserted helper
		class Helper internal constructor(private val buttons: Map<ButtonId, Button>) : InputSystem.InputPredicateBase.Helper {
			operator fun get(button: ButtonId) = buttons[button]!!
		}

		@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
		abstract override fun test(buttons: Helper): Boolean

		data class Down(val x: ButtonId) : ButtonPredicate() {
			override fun test(buttons: Helper) = buttons[x].down
		}

		data class JustDown(val x: ButtonId) : ButtonPredicate() {
			override fun test(buttons: Helper) = buttons[x].justDown
		}

		data class JustUp(val x: ButtonId) : ButtonPredicate() {
			override fun test(buttons: Helper) = buttons[x].justUp
		}
	}

	sealed interface InnerButtons {
		val id: ButtonId
		val down: ButtonPredicate.Down
		val justDown: ButtonPredicate.JustDown
		val justUp: ButtonPredicate.JustUp
		fun matches(other: ButtonId): Boolean // Is this useful?
	}

	private class ButtonsImpl(override val id: ButtonId) : InnerButtons {
		override val down get() = ButtonPredicate.Down(id)
		override val justDown get() = ButtonPredicate.JustDown(id)
		override val justUp get() = ButtonPredicate.JustUp(id)
		override fun matches(other: ButtonId) = id == other
	}

	// Values refer to ferricia::mui::KeyboardKey
	enum class Buttons(override val id: ButtonId) : InnerButtons by ButtonsImpl(id) {
		Left(ButtonId(0u)),
		Middle(ButtonId(1u)),
		Right(ButtonId(2u)),
		X1(ButtonId(3u)),
		X2(ButtonId(4u)),
	}

	object ButtonsScope {
		val Left = Buttons.Left
		val Middle = Buttons.Middle
		val Right = Buttons.Right
		val X1 = Buttons.X1
		val X2 = Buttons.X2
	}

	private val buttons = HashMap<ButtonId, Button>()

	init {
		mouseDevice.iterButtons().forEach { (k, v) ->
			buttons[k] = Button(v)
		}
	}

	// Large difference if specific keyboards can be specifically handled
	class Button internal constructor(internal val raw: MouseDevice.Button) {
		val down: Boolean get() = raw.down
		internal var justChanged = false

		val justDown: Boolean get() = down && justChanged
		val justUp: Boolean get() = !down && justChanged
	}

	fun condition(predicate: ButtonsScope.() -> ButtonPredicate) = ButtonsScope.predicate().test(ButtonPredicate.Helper(buttons))

	internal sealed class Event private constructor() {
		sealed class Button private constructor(open val key: ButtonId) : Event() {
			data class Down(override val key: ButtonId) : Button(key)
			data class Up(override val key: ButtonId) : Button(key)
		}
		data class Movement(val delX: Float, val delY: Float) : Event()
	}

	internal fun update(events: Sequence<Event>) {
		buttons.values.forEach { it.justChanged = false }
		events.forEach {
			when (it) {
				is Event.Button -> {
					// Note: This may not handle the case where a key is just down less than a tick.
					// This also assumes that keyboard states are consistent across time frames.
					buttons[it.key]!!.justChanged = true
					when (it) {
						is Event.Button.Down -> buttons[it.key]!!.raw.down = true
						is Event.Button.Up -> buttons[it.key]!!.raw.down = false
					}
				}
				is Event.Movement -> {} // TODO
			}
		}
	}
}
