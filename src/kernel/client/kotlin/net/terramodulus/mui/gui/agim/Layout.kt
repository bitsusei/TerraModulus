/*
 * SPDX-FileCopyrightText: 2025-2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.gui.agim

import net.terramodulus.mui.gui.gfx.RenderSystem
import java.io.Closeable
import java.util.ArrayDeque

/**
 * [Layout] is always mutable.
 *
 * **Layout** is defined only when all its managed components all belong to the container
 * associated with this layout manager *exclusively*.
 */
abstract class Layout(protected val container: Container) : Closeable {
	companion object {
		const val ALIGN_START = 0F
		const val ALIGN_CENTER = .5F
		const val ALIGN_END = 1F
	}

	abstract val components: Sequence<Component>

	private val layoutOperations = ArrayDeque<Operation>()

	fun interface Operation {
		fun Layout.operate()
	}

	/**
	 * Query [Operation] on the [Layout] structures, potentially changing any components and configurations.
	 *
	 * It is required to use this when this layout is being initialized
	 * or any layout configuration is being changed.
	 */
	fun operate(operation: Operation) {
		layoutOperations.add(operation)
	}

	/**
	 * Updates the layout output using pending operations added via [operate],
	 * and the resultant layout configurations by invoking [layOut] internally if any operation exists.
	 */
	fun update() {
		val nonEmpty = layoutOperations.isNotEmpty()
		while (layoutOperations.isNotEmpty()) {
			with(layoutOperations.removeFirst()) { this@Layout.operate() }
		}
		if (nonEmpty) updated = true
	}

	/**
	 * Whether this [Layout] has been updated (as in [update]) at this moment.
	 * This should only be updated by [update] and [LayoutManager].
	 */
	internal var updated = false

	/**
	 * Lays out the managed [components] by this [Layout] manager.
	 *
	 * In most cases, only the `layoutHandle`s of the managed `components` should be (re)assigned.
	 * Otherwise, no other state-changing operations should be done beside this.
	 */
	protected abstract fun layOut(handle: LayoutHandle): Sequence<LayoutComputationGroup>

	internal fun layOutInternal(handle: LayoutHandle) = layOut(handle)

	/**
	 * Renders this [Layout] with underlying managed [components].
	 */
	internal fun render(renderSystem: RenderSystem) = components.forEach { it.render(renderSystem) }

	/**
	 * Must be invoked when this [Layout] is no longer in use.
	 */
	fun clear() { // Not sure whether there is the necessity to separate this from [close].
// 		container.asdHandle.unobserveRect(containerObserver)
	}

	override fun close() {
		clear()
	}

	/**
	 * Should not rely on indices in the [Layout] since they are not meaningful.
	 */
	abstract class Group(container: Container) : Layout(container) {
		/**
		 * Checks if the specified [component] is contained in this layout.
		 * One should not rely on this function for efficiency and effectiveness.
		 */
		abstract fun contains(component: Component): Boolean

		/**
		 * Adds the [component] to this layout.
		 */
		abstract fun add(component: Component)

		/**
		 * Removes the [component] from this layout.
		 * Any error may occur if `target` does not exist in the layout.
		 */
		abstract fun remove(component: Component)

		/**
		 * Adds the [component] just before the [target] in this layout.
		 * Any error may occur if `target` does not exist in the layout.
		 */
		abstract fun addBefore(target: Component, component: Component)

		/**
		 * Adds the [component] just after the [target] in this layout.
		 * Any error may occur if `target` does not exist in the layout.
		 */
		abstract fun addAfter(target: Component, component: Component)

		/**
		 * Replaces the [target] in this layout by the [component].
		 * Any error may occur if `target` does not exist in the layout.
		 */
		abstract fun replace(target: Component, component: Component)
	}

	abstract class ElementGroup<E : Any> protected constructor(
		container: Container,
		protected open val elements: ElementList<E>,
	) : Group(container) {
		final override val components = elements.componentsView.asSequence()

		override fun contains(component: Component): Boolean = elements.contains(component)

		/**
		 * Adds the [component] with the default element to this layout.
		 */
		abstract override fun add(component: Component)

		/**
		 * Adds the [component] with the [element] to this layout.
		 */
		open fun add(component: Component, element: E) = elements.add(component, element)

		override fun remove(component: Component) = elements.remove(component)

		/**
		 * Adds the [component] with the default element just before the [target] in this layout.
		 * Any error may occur if `target` does not exist in the layout.
		 */
		abstract override fun addBefore(target: Component, component: Component)

		/**
		 * Adds the [component] with the default element just after the [target] in this layout.
		 * Any error may occur if `target` does not exist in the layout.
		 */
		abstract override fun addAfter(target: Component, component: Component)

		/**
		 * Replaces the [target] in this layout by the [component] reusing `target`'s `element`.
		 * Any error may occur if `target` does not exist in the list.
		 */
		open fun replaceKeep(target: Component, component: Component) =
			elements.replaceKeep(target, component)

		/**
		 * Replaces the [target] in this layout by the [component] with the default element.
		 * Any error may occur if `target` does not exist in the layout.
		 */
		abstract override fun replace(target: Component, component: Component)

		/**
		 * Replaces the [target] in this layout by the [component] with the [element].
		 * Any error may occur if `target` does not exist in the layout.
		 */
		open fun replace(target: Component, component: Component, element: E) =
			elements.replace(target, component, element)
	}

	protected fun componentsNullableSequence(vararg components: () -> Component?) =
		sequenceOf(*components).mapNotNull { it() }

	protected fun componentsSequence(vararg components: () -> Component) =
		sequenceOf(*components).map { it() }

	/**
	 * Internal list of the layout elements.
	 */
	protected class ElementList<E : Any> private constructor(
        private val components: MutableList<Component>, // order is defined here
        private val elementMap: MutableMap<Component, E>, // elements are mapped here
	) : Iterable<Pair<Component, E>> {
		companion object {
			fun <E : Any> withComponentsDefault(default: () -> E, vararg components: Component): ElementList<E> {
				val map = hashMapOf<Component, E>()
				components.forEach { map[it] = default() }
				return ElementList(mutableListOf(*components), map)
			}

			fun <E : Any> withComponentsDefault(default: () -> E, components: Collection<Component>): ElementList<E> {
				val map = hashMapOf<Component, E>()
				components.forEach { map[it] = default() }
				return ElementList(ArrayList(components), map)
			}

			fun <E : Any> withElements(vararg elements: Pair<Component, E>): ElementList<E> =
				ElementList(elements.mapTo(ArrayList(elements.size)) { it.first }, hashMapOf(*elements))

			fun <E : Any> withElements(elements: Map<Component, E>): ElementList<E> =
				ElementList(elements.mapTo(ArrayList(elements.size)) { it.key }, HashMap(elements))
		}

		val componentsView: Collection<Component> = components

		override fun iterator(): Iterator<Pair<Component, E>> = object : Iterator<Pair<Component, E>> {
			private val it = components.iterator()

			override fun hasNext() = it.hasNext()

			override fun next(): Pair<Component, E> {
				val e = it.next()
				return e to elementMap[e]!!
			}
		}

		/**
		 * Checks if the specified [component] is contained in this list.
		 * One should not rely on this function for efficiency and effectiveness.
		 */
		fun contains(component: Component): Boolean = components.contains(component)

		/**
		 * Adds the [component] with the [element] to this list.
		 */
		fun add(component: Component, element: E) {
			components.add(component)
			elementMap[component] = element
		}

		/**
		 * Removes the [component] from this list.
		 */
		fun remove(component: Component) {
			components.remove(component)
			elementMap.remove(component)
		}

		/**
		 * Adds the [component] with the [element] just before the [target] in this list.
		 * Any error may occur if `target` does not exist in the list.
		 */
		fun addBefore(target: Component, component: Component, element: E) {
			components.add(components.indexOf(target), component)
			elementMap[component] = element
		}

		/**
		 * Adds the [component] with the [element] just after the [target] in this list.
		 * Any error may occur if `target` does not exist in the list.
		 */
		fun addAfter(target: Component, component: Component, element: E) {
			components.add(components.indexOf(target) + 1, component)
			elementMap[component] = element
		}

		/**
		 * Replaces the [target] in this list by the [component] reusing `target`'s `element`.
		 * Any error may occur if `target` does not exist in the list.
		 */
		fun replaceKeep(target: Component, component: Component) {
			components.add(components.indexOf(target), component)
			elementMap[component] = elementMap[target]!!
			remove(target)
		}

		/**
		 * Replaces the [target] in this list by the [component] with the [element].
		 * Any error may occur if `target` does not exist in the list.
		 */
		fun replace(target: Component, component: Component, element: E) {
			components.add(components.indexOf(target), component)
			remove(target)
			elementMap[target] = element
		}
	}
}
