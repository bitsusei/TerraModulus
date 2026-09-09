/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.impl

import net.terramodulus.mui.gui.agim.Layout
import net.terramodulus.mui.gui.gfx.RenderSystem
import net.terramodulus.mui.gui.agim.Screen
import net.terramodulus.mui.gui.agim.ScreenManager
import net.terramodulus.mui.gui.asd.AsdHandle

class TitleScreen(
	managerHandle: ScreenManager.Handle,
	asdHandle: AsdHandle.Container,
	renderSystemHandle: RenderSystem.Handle,
) : Screen(managerHandle, asdHandle) {
	override val layout =
		SingletonLayout(this, BlankComponent(ComponentAsdHandleImpl()), SingletonLayout.Config.Absolute.Full)
}
