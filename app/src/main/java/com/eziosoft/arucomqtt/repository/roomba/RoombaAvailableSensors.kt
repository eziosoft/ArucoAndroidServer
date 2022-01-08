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

package com.eziosoft.arucomqtt.repository.roomba

object RoombaAvailableSensors {
    private var sensors = hashMapOf<Int, RoombaSensor>()

    @Suppress("LongParameterList")
    private fun add(
        packetID: Int,
        name: String,
        bytes: Int,
        min: Int,
        max: Int,
        unit: String = ""
    ) {
        sensors[packetID] = RoombaSensor(packetID, name, bytes, min, max, unit)
    }

    fun getSensor(packetID: Int): RoombaSensor? {
        return if (sensors.containsKey(packetID)) {
            sensors[packetID]
        } else {
            null
        }
    }

    fun isIdValid(packetID: Int) = sensors.containsKey(packetID)

    fun getChargingStateString(code: Int): String {
        return when (code) {
            0 -> "Not charging"
            1 -> "Reconditioning Charging"
            2 -> "Full Charging"
            3 -> "Trickle Charging"
            4 -> "Waiting"
            5 -> "Charging Fault Condition"
            else -> "Unknown"
        }
    }

    fun getIOModeString(code: Int): String {
        return when (code) {
            0 -> "Off"
            1 -> "Passive"
            2 -> "Safe"
            3 -> "Full"
            else -> ""
        }
    }

    init {
        add(7, "Bumps Wheeldrops", 1, 0, 15)
        add(8, "Wall", 1, 0, 1)
        add(9, "Cliff Left", 1, 0, 1)
        add(10, "Cliff Front Left", 1, 0, 1)
        add(11, "Cliff Front Right", 1, 0, 1)
        add(12, "Cliff Right", 1, 0, 1)
        add(13, "Virtual Wall", 1, 0, 1)
        add(14, "Overcurrents", 1, 0, 29)
        add(15, "Dirt Detect", 1, 0, 255)
        add(16, "Unused 1", 1, 0, 255)
        add(17, "Ir Opcode", 1, 0, 255)
        add(18, "Buttons", 1, 0, 255)
        add(19, "Distance", 2, -32768, 32767, "mm")
        add(20, "Angle", 2, -32768, 32767, "degrees")
        add(21, "Charging State", 1, 0, 6, "")
        add(22, "Voltage", 2, 0, 65535, "mV")
        add(23, "Current", 2, -32768, 32767, "mA")
        add(24, "Temperature", 1, -128, 127, "deg C")
        add(25, "Battery Charge", 2, 0, 65535, "mAh")
        add(26, "Battery Capacity", 2, 0, 65535, "mAh")
        add(27, "Wall Signal", 2, 0, 1023)
        add(28, "Cliff Left Signal", 2, 0, 4095)
        add(29, "Cliff Front Left Signal", 2, 0, 4095)
        add(30, "Cliff Front Right Signal", 2, 0, 4095)
        add(31, "Cliff Right Signal", 2, 0, 4095)
        add(32, "Unused 2", 1, 0, 255)
        add(33, "Unused 3", 2, 0, 65535)
        add(34, "Charger Available", 1, 0, 3)
        add(35, "Open Interface Mode", 1, 0, 3)
        add(36, "Song Number", 1, 0, 4)
        add(37, "Song Playing?", 1, 0, 1)
        add(38, "Oi Stream Num Packets", 1, 0, 108)
        add(39, "Velocity", 2, -500, 500, "mm/s")
        add(40, "Radius", 2, -32768, 32767, "mm")
        add(41, "Velocity Right", 2, -500, 500, "mm/s")
        add(42, "Velocity Left", 2, -500, 500, "mm/s")
        add(43, "Encoder Counts Left", 2, -32768, 32767)
        add(44, "Encoder Counts Right", 2, -32768, 32767)
        add(45, "Light Bumper", 1, 0, 127)
        add(46, "Light Bump Left", 2, 0, 4095)
        add(47, "Light Bump Front Left", 2, 0, 4095)
        add(48, "Light Bump Center Left", 2, 0, 4095)
        add(49, "Light Bump Center Right", 2, 0, 4095)
        add(50, "Light Bump Front Right", 2, 0, 4095)
        add(51, "Light Bump Right", 2, 0, 4095)
        add(52, "Ir Opcode Left", 1, 0, 255)
        add(53, "lr Opcode Right", 1, 0, 255)
        add(54, "Left Motor Current", 2, -32768, 32767, "mA")
        add(55, "Right Motor Current", 2, -32768, 32767, "mA")
        add(56, "Main Brush Current", 2, -32768, 32767, "mA")
        add(57, "Side Brush Current", 2, -32768, 32767, "mA")
        add(58, "Stasis", 1, 0, 3)

        //my sensors
        add(100, "Wakeup time", 1, 0, 32767)
        add(101, "RSSI", 1, 32768, 32767, "dB")
        add(102, "Used capacity", 1, 0, 32767, "mAh")
    }

    data class RoombaSensor(
        val packetID: Int,
        val name: String,
        val bytesCount: Int,
        val min: Int,
        val max: Int,
        val unit: String = ""
    )
}
