/*
 * SPDX-FileCopyrightText: 2026 TerraModulus Team and Contributors
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package net.terramodulus.mui.uid

/**
 * User Interface Device (UID) Manager
 */
internal class UidManager {
	class Devices<D : Device> internal constructor() {
		private val devices = mutableMapOf<Device.Id, D>()

		fun count() = devices.count()

		fun exists(id: Device.Id) = devices.containsKey(id)

		internal fun add(device: D) {
			devices[device.id] = device
		}

		internal fun remove(deviceId: Device.Id) {
			if (devices.remove(deviceId) == null)
				throw IllegalStateException("Device [$javaClass] $deviceId does not exist")
		}
	}

	typealias KeyboardDevices = Devices<KeyboardDevice>
	typealias MouseDevices = Devices<MouseDevice>

	// not yet used
// 	val keyboardDevices = KeyboardDevices()
// 	val mouseDevices = MouseDevices()
	val keyboardDevice = KeyboardDevice(Device.Id(0u))
	val mouseDevice = MouseDevice(Device.Id(0u))
}
