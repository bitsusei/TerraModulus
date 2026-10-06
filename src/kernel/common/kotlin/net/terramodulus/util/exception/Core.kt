/*
 * SPDX-FileCopyrightText: 2025 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.util.exception

import net.terramodulus.util.logging.logger
import java.util.function.Predicate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

private val logger = logger {}

// TODO add functions that can throw errors in debug and throw warnings in production
//   This is useful for like when a resource is closed twice, logic error but may likely run fine in production
//   May also optionally add fallback lambda to be run in production to suppress such error

@OptIn(ExperimentalContracts::class)
inline fun <R, reified X : Throwable> codeAssert(block: () -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	try {
		return block()
	} catch (t: Throwable) {
		if (t is X) throw CodeLogicFault(t) else throw t
	}
}

@OptIn(ExperimentalContracts::class)
inline fun <R> codeAssert(vararg clazz: KClass<out Throwable>, block: () -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	try {
		return block()
	} catch (t: Throwable) {
		if (clazz.any { it.isInstance(t) }) throw CodeLogicFault(t) else throw t
	}
}

@OptIn(ExperimentalContracts::class)
inline fun <R> codeAssert(predicate: Predicate<Throwable>, block: () -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	try {
		return block()
	} catch (t: Throwable) {
		if (predicate.test(t)) throw CodeLogicFault(t) else throw t
	}
}

/**
 * Records the recovered [exception] to logger.
 * This logs the `exception` as a warning.
 */
fun recordException(exception: Exception) {

}

/**
 * Pushes the recovered [exception] to application notification and records it to logger.
 * This logs the `exception` as a warning.
 */
fun notifyAndRecordException(exception: Exception) {
	// TODO exception notification
	recordException(exception)
}

/**
 * Records the suppressed [error] to logger.
 * This logs the `exception` as an error.
 */
fun recordError(error: Error) {

}

/**
 * Pushes the suppressed [error] to application notification and records it to logger.
 * This logs the `error` as an error.
 */
fun notifyAndRecordError(error: Error) {
	// TODO exception notification
	recordError(error)
}

fun triggerSessionCrash() {
	TODO()
}

fun triggerGlobalCrash(error: Error): Nothing {
	logger.error(error) {}
	TODO()
}
