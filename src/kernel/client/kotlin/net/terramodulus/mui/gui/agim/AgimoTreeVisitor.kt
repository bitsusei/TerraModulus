/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import java.util.LinkedList

internal abstract class AgimoTreeVisitor {
	internal fun interface ScreenTreeVisitor {
		fun visit(): Sequence<Screen>
	}

	internal fun interface MenuTreeVisitor {
		fun visit(): Sequence<Menu>
	}

	class RootNode(screens: Sequence<Screen>, menus: Sequence<Menu>) {
		val screens = ScreenTree(screens)
		val menus = MenuTree(menus)
	}

	sealed class ContainerNode(val layout: Layout) {
		val elements = LinkedList(layout.components.map {
			if (it is AbstractPane) PaneNode(it) else SimpleComponentNode(it)
		}.toList())

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as ContainerNode

			if (layout != other.layout) return false
			if (elements != other.elements) return false

			return true
		}

		override fun hashCode(): Int {
			var result = layout.hashCode()
			result = 31 * result + elements.hashCode()
			return result
		}
	}

	sealed interface ComponentNode {
		val component: Component
	}

	class SimpleComponentNode(override val component: Component) : ComponentNode {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as SimpleComponentNode

			return component == other.component
		}

		override fun hashCode(): Int {
			return component.hashCode()
		}
	}

	class PaneNode(override val component: AbstractPane) : ContainerNode(component.layout), ComponentNode {
		override fun equals(other: Any?): Boolean {
			return super.equals(other) && component == (other as PaneNode).component
		}

		override fun hashCode(): Int {
			var result = super.hashCode()
			result = 31 * result + component.hashCode()
			return result
		}
	}

	class ScreenTree(screens: Sequence<Screen>) {
		val list = LinkedList(screens.map { ScreenNode(it) }.toList())
	}

	class ScreenNode(val screen: Screen) : ContainerNode(screen.layout) {
		val menus = MenuTree(screen.visit().visit())

		override fun equals(other: Any?): Boolean {
			return super.equals(other) && screen == (other as ScreenNode).screen && menus == other.menus
		}

		override fun hashCode(): Int {
			var result = super.hashCode()
			result = 31 * result + screen.hashCode()
			result = 31 * result + menus.hashCode()
			return result
		}
	}

	class MenuTree(menus: Sequence<Menu>) {
		val list = LinkedList(menus.map { MenuNode(it) }.toList())
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as MenuTree

			return list == other.list
		}

		override fun hashCode() = list.hashCode()
	}

	class MenuNode(val menu: Menu) : ContainerNode(menu.layout) {
		override fun equals(other: Any?): Boolean {
			return super.equals(other) && menu == (other as MenuNode).menu
		}

		override fun hashCode(): Int {
			var result = super.hashCode()
			result = 31 * result + menu.hashCode()
			return result
		}
	}
}
