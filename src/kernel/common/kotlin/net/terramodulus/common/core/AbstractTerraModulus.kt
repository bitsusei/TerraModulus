/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.common.core

import java.io.Closeable

/**
 * Do not use/extend this outside TerraModulus game code.
 *
 * If this is Multiplatform, this could be `expect` with subclasses `actual`.
 *
 * @constructor Cannot be `internal` because `client` and `server` are other modules.
 */
abstract class AbstractTerraModulus : Closeable {
	abstract fun run()
}
