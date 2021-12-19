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

import com.eziosoft.mqtt_test.repository.roomba.RoombaAvailableSensors
import com.eziosoft.mqtt_test.repository.roomba.RoombaParsedSensor
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis


@Singleton
class RoombaSensorParser @Inject constructor() {
    var logging = false
    private var state = STATE.HEADER
    private var n: UByte = 0u
    private var ni = 0
    private var chksum: UByte = 0u
    private var b1: UByte = 0u
    private var b2: UByte = 0u
    private var value = 0
    private var packetID: Int = 0

    private var isParsing = false
    private val sensors = arrayListOf<RoombaParsedSensor>()

    private var sensorListener: SensorListener? = null

    fun setListener(sensorListener: SensorListener) {
        this.sensorListener = sensorListener
    }

    @ExperimentalUnsignedTypes
    @Suppress("LongMethod", "ComplexMethod")
    suspend fun parse(bytes: UByteArray) {
        val elapsed = measureTimeMillis {
            isParsing = true
            for (i in bytes.indices) {
                val b = bytes[i]

                when (state) {
                    STATE.HEADER -> if (b == 19.toUByte()) {
                        chksum = 0u
                        n = 0u
                        ni = 0
                        chksum = 0u
                        b1 = 0u
                        b2 = 0u
                        value = 0
                        packetID = 0
                        chksum = (chksum + b).toUByte()
                        sensors.clear()
                        state = STATE.N
                    }
                    STATE.N -> {
                        n = b
                        chksum = (chksum + b).toUByte()
                        state = STATE.PACKET_ID
                    }
                    STATE.PACKET_ID -> {
                        packetID = b.toInt()
                        if (!RoombaAvailableSensors.isIdValid(packetID.toInt())) {
                            state = STATE.HEADER //not valid packet id
                            if (logging) println("---------->Not valid packet id")
                        }
                        chksum = (chksum + b).toUByte()
                        ni++
                        if (ni > n.toInt()) state = STATE.HEADER
                        state = STATE.PACKET_DATA1
                    }
                    STATE.PACKET_DATA1 -> {
                        b1 = b
                        chksum = (chksum + b).toUByte()
                        ni++
                        if (ni > n.toInt()) state = STATE.HEADER
//                    if(logging)println("$n == $ni")

                        when (RoombaAvailableSensors.getSensor(packetID.toInt())?.bytesCount) {
                            1 -> state = STATE.CHKSUM_OR_NEXT_SENSOR
                            2 -> state = STATE.PACKET_DATA2
                        }
                    }

                    STATE.PACKET_DATA2 -> {
                        b2 = b
                        chksum = (chksum + b).toUByte()
                        ni++
                        if (ni > n.toInt()) state = STATE.HEADER
                        state = STATE.CHKSUM_OR_NEXT_SENSOR
                    }

                    STATE.CHKSUM_OR_NEXT_SENSOR -> {
                        if (RoombaAvailableSensors.getSensor(packetID.toInt())?.bytesCount == 2) {
                            value = (b1.toInt() * 256 + b2.toInt()).toInt()
                        } else {
                            value = b1.toInt()
                        }
                        sensors.add(RoombaParsedSensor(packetID, b1, b2, value))

                        if (n.toInt() == ni) { //check check sum and return sensors
                            chksum = (chksum + b).toUByte()
                            if (chksum == 0.toUByte()) {//check sum correct
                                if (logging) {
                                    println("chksum OK")
                                    println(sensors.toString())
                                }
                                withContext(Dispatchers.Main) {
                                    sensorListener?.onSensors(sensors.toList(), true)
                                }
                            } else {
                                if (logging) println("chksum FAILED")
                                withContext(Dispatchers.Main) {
                                    sensorListener?.onSensors(sensors.toList(), false)
                                }
                                state = STATE.HEADER
                            }
                            state = STATE.HEADER
                        } else { // next packet
                            if (logging) println("->next sensor")
                            b1 = 0u
                            b2 = 0u
                            value = 0
                            packetID = b.toInt()
                            chksum = (chksum + b).toUByte()
                            ni++
                            state = STATE.PACKET_DATA1
                        }

                        if (n.toInt() < ni) state = STATE.HEADER
                    }
                }

                if (logging) print("$b " + state.name)
                if (logging) println(" n=$n id=$packetID b1=$b1 b2=$b2 ni=$ni")
            }
            isParsing = false
        }
        if (logging) println("---------------------------------->$elapsed")
    }

    interface SensorListener {
        fun onSensors(sensors: List<RoombaParsedSensor>, checksumOK: Boolean)
    }

    companion object {
        enum class STATE {
            HEADER, N, PACKET_ID, PACKET_DATA1, PACKET_DATA2, CHKSUM_OR_NEXT_SENSOR
        }
    }
}