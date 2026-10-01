/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

/*
 * Every set of anchor position directions has different meanings in different context,
 * They are kept separated for context and literal clarity and organization.
 * Those should be used with care since they are not already interconvertible.
 */

/**
 * Set of 4 anchor position directions
 */
enum class Anchor4 {
	TopLeft, TopRight, BottomLeft, BottomRight;
}

/**
 * Set of 5 anchor position directions
 */
enum class Anchor5 {
	TopLeft, TopRight, BottomLeft, BottomRight, Center;
}
