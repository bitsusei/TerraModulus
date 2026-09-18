/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.engine

import java.io.InputStream

/**
 * Since for most parts, there is no definite metadata of lengths for all data,
 * it is necessary to provide at least processing fragmentation from implementation.
 */
interface BaseAsdProcessor {
	/**
	 * @param data ByteArrayInputStream backed by direct byte buffer
	 */
	fun process(data: InputStream)
}
