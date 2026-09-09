/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.uid

sealed interface Device {
	val id: Id

	@JvmInline
	value class Id(private val value: UInt)
}
