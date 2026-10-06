/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.mui.gui.asd.AsdHandle

/**
 * **AGIM Container**, direct subclasses are explicitly defined.
 */
sealed interface Container {
	val asdHandle: AsdHandle//.Container

	val layout: Layout
}
