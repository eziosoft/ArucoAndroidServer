/*
 *  This file is part of MQTT_robot_control_Android.
 *
 *     MQTT_robot_control_Android is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2021. Bartosz Szczygiel
 *
 */

package com.eziosoft.mqtt_test.repository.roomba

data class RoombaParsedSensor @ExperimentalUnsignedTypes constructor(
    val sensorID: Int = 0,
    val b1: UByte = 0u,
    val b2: UByte = 0u,
    val unsignedValue: Int = 0,
    val name: String? = RoombaAvailableSensors.getSensor(sensorID.toInt())?.name,
    val units: String? = RoombaAvailableSensors.getSensor(sensorID.toInt())?.unit
) {
    val signedValue: Int
        get() = if (RoombaAvailableSensors.getSensor(sensorID)!!.min < 0) {
            unsignedValue.toShort()
                .toInt()
        } else {
            unsignedValue
        }

    fun toString1(): String = "$name=$signedValue$units"

    fun getNameAndSensorID() = "($sensorID)$name"


    fun toStringValueWithUnits(): String {
        return when (sensorID) {
            21 -> RoombaAvailableSensors.getChargingStateString(unsignedValue)
            35 -> RoombaAvailableSensors.getIOModeString(unsignedValue)
            else -> "$signedValue$units"
        }
    }
}
