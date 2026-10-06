/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.util

tailrec fun gcd(a: Int, b: Int): Int {
	return if (b == 0) a else gcd(b, a % b)
}

tailrec fun gcd(a: UInt, b: UInt): UInt {
	return if (b == 0u) a else gcd(b, a % b)
}

tailrec fun gcd(a: Long, b: Long): Long {
	return if (b == 0L) a else gcd(b, a % b)
}

tailrec fun gcd(a: ULong, b: ULong): ULong {
	return if (b == 0uL) a else gcd(b, a % b)
}
