/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim.event

import net.terramodulus.mui.gui.agim.ScreenManager

sealed interface ScreenEvent {
	data class Update(val muiIoI: ScreenManager.MuiIoI) : ScreenEvent
	data object Close : ScreenEvent
	data class Generic<T : GenericEvent>(val generic: T) : ScreenEvent
}
