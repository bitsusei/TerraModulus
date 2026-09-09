/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.gfx

/*
 * Every set of directions has different meanings in different context,
 * They are kept separated for context and literal clarity and organization.
 * Those should be used with care since they are not already interconvertible.
 */

enum class Direction2 {
	Positive, Negative;
}

/**
 * Set of 4 compass directions.
 */
enum class Direction4C {
	North, South, West, East;
}

/**
 * Set of 4 vertical directions.
 */
enum class Direction4V {
	Left, Right, Up, Down;
}

/**
 * Set of 4 horizontal directions.
 */
enum class Direction4H {
	Front, Back, Left, Right;
}

/**
 * Set of 4 axial directions.
 */
enum class Direction4A {
	XPos, XNeg, YPos, YNeg;
}

/**
 * Set of 4 axial diagonal directions, by Cartesian quadrants.
 */
enum class Direction4AD {
	QuadOne, QuadTwo, QuadThree, QuadFour;
}

/**
 * Set of 8 compass directions.
 */
enum class Direction8C {
	North, South, West, East, NorthWest, NorthEast, SouthWest, SouthEast;
}

/**
 * Set of 8 vertical directions.
 */
enum class Direction8V {
	Left, Right, Up, Down, UpLeft, UpRight, DownLeft, DownRight;
}

/**
 * Set of 8 horizontal directions.
 */
enum class Direction8H {
	Front, Back, Left, Right, FrontLeft, FrontRight, BackLeft, BackRight;
}

/**
 * Set of 6 3D-relative directions.
 */
enum class Direction6R {
	Up, Down, Left, Right, Front, Back;
}

/**
 * Set of 6 3D-compass directions.
 */
enum class Direction6C {
	North, South, West, East, Up, Down;
}
