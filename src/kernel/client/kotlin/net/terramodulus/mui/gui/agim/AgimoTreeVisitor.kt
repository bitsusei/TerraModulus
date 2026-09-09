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
	}

	sealed interface ComponentNode {
		val component: Component
	}

	class SimpleComponentNode(override val component: Component) : ComponentNode

	class PaneNode(override val component: AbstractPane) : ContainerNode(component.layout), ComponentNode

	class ScreenTree(screens: Sequence<Screen>) {
		val list = LinkedList(screens.map { ScreenNode(it) }.toList())
	}

	class ScreenNode(val screen: Screen) : ContainerNode(screen.layout) {
		val menus = MenuTree(screen.visit().visit())
	}

	class MenuTree(menus: Sequence<Menu>) {
		val list = LinkedList(menus.map { MenuNode(it) }.toList())
	}

	class MenuNode(val menu: Menu) : ContainerNode(menu.layout)
}
