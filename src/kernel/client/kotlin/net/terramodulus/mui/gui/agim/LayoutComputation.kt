/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.mui.gui.asd.AsdHandle

class LayoutComputationUnit @PublishedApi internal constructor(
	val computation: LayoutHandle.() -> Map<AsdHandle, AgimoPropertyMap>,
	val dependencies: Map<AsdHandle, Set<AgimoPropertyMap.Key<*>>>,
	val results: Map<AsdHandle, Set<AgimoPropertyMap.Key<*>>>,
) {
	companion object {
		inline operator fun invoke(
			dependencies: MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>.() -> Unit,
			results: MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>.() -> Unit,
			noinline computation: LayoutHandle.() -> Map<AsdHandle, AgimoPropertyMap>,
		) = LayoutComputationUnit(computation,
			mutableMapOf<AsdHandle, Set<AgimoPropertyMap.Key<*>>>().apply(dependencies),
			mutableMapOf<AsdHandle, Set<AgimoPropertyMap.Key<*>>>().apply(results),
		)
	}
}

class LayoutComputationGroup @PublishedApi internal constructor(
	val conditions: () -> Set<LayoutComputationUnit>,
// 	val conditions: LayoutHandle.() -> Set<LayoutComputationUnit>,
	val dependencies: MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>,
) {
	companion object {
		inline operator fun invoke(
			dependencies: MutableMap<AsdHandle, Set<AgimoPropertyMap.Key<*>>>.() -> Unit,
			noinline conditions: () -> Set<LayoutComputationUnit>,
// 			noinline conditions: LayoutHandle.() -> Set<LayoutComputationUnit>,
		) = LayoutComputationGroup(conditions,
			mutableMapOf<AsdHandle, Set<AgimoPropertyMap.Key<*>>>().apply(dependencies),
		)
	}
}

// class LayoutComputationDependency(val key: AgimoPropertyMap.Key<*>) { // Dependencies saved in a Set (immutable)
// }

// class LayoutComputationResult() { // Results saved in a Map (immutable)
// }
