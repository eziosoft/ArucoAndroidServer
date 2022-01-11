/*
 *     This file is part of ArucoAndroidServer.
 *
 *     ArucoAndroidServer is free software: you can redistribute it and/or modify
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
 */

package com.eziosoft.arucomqtt.repository.robotControl

import android.util.Log
import androidx.viewbinding.BuildConfig
import com.eziosoft.arucomqtt.repository.mqtt.Mqtt
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin

@Singleton
class RobotControl @Inject constructor(private val mqtt: Mqtt) {

    private var timer = 0L
    var alarm = false

    fun sendJoystickData(angle: Int, strength: Int, precision: Boolean) {
        // Log.d("aaa", "handleJoystick: angle=$angle  strength=$strength")

        var x = cos(Math.toRadians(angle.toDouble())) * strength / 100f
        var y = sin(Math.toRadians(angle.toDouble())) * strength / 100f

        if (precision) {
            x /= 4f
            y /= 4f
        }

        val ch1 = (-x * 100).toInt()
        val ch2 = (y * 100).toInt()
        val ch3 = 0
        val ch4 = 0

        if (BuildConfig.DEBUG) {
            Log.d("bbb", "$ch1 $ch2 $ch3 $ch4")
        }

        if (ch1 == 0 && ch2 == 0 || ch3 == 0 && ch4 == 0 || System.currentTimeMillis() > timer) {
            timer = System.currentTimeMillis() + JOYSTICK_SEND_COMMAND_PERIOD
            if (mqtt.isConnected()) {
                sendChannels(ch1, ch2, ch3, ch4)
            }
        }
    }

    fun sendChannels(ch1: Int, ch2: Int, ch3: Int, ch4: Int) {

        val bytes: ByteArray
        if (!alarm) {
            bytes =
                byteArrayOf(
                    '$'.toByte(), 5,
                    (ch1 + 100).toByte(),
                    (ch2 + 100).toByte(),
                    (ch3 + 100).toByte(),
                    (ch4 + 100).toByte()
                )
        } else {
            bytes =
                byteArrayOf(
                    '$'.toByte(), 5,
                    100.toByte(),
                    100.toByte(),
                    100.toByte(),
                    100.toByte()
                )
        }

        if (mqtt.isConnected()) {
            mqtt.publishMessage(
                message = bytes,
                topic = MQTT_CONTROL_TOPIC,
                retain = false,
            ) { messageSent, throwable ->
                throwable?.let {
                    Log.e("aaa", "sendChannels: ", throwable)
                }
            }
        }
    }

    fun sendCommandsChannels(command:ROOMBA_COMMANDS) {
        sendChannels(0, 0, command.ch3, command.ch4)
    }

    fun initRoomba()
    {
        sendCommandsChannels(ROOMBA_COMMANDS.START)
        sendCommandsChannels(ROOMBA_COMMANDS.STARTSTREAM)
    }

    fun endRoomba()
    {
        sendCommandsChannels(ROOMBA_COMMANDS.STOPSTREAM)
        sendCommandsChannels(ROOMBA_COMMANDS.STOP)
    }



//    buttonStart.id -> viewModel.sendCommandsChannels(2, 0)
//    buttonStop.id -> viewModel.sendCommandsChannels(1, 0)
//    buttonStopBrush.id -> viewModel.sendCommandsChannels(11, 0)
//    buttonStartBrush.id -> viewModel.sendCommandsChannels(10, 0)
//    buttonClean.id -> viewModel.sendCommandsChannels(12, 0)
//    buttonDock.id -> viewModel.sendCommandsChannels(3, 0)
//    buttonUnDock.id -> viewModel.sendCommandsChannels(4, 0)
//    buttonPowerOff.id -> viewModel.sendCommandsChannels(5, 0)
//    buttonStartStream.id -> viewModel.sendCommandsChannels(20, 0)
//    buttonPauseStream.id -> viewModel.sendCommandsChannels(21, 0)



    fun robotStop() {
        sendChannels(0, 0, 0, 0)
    }

    companion object {
        const val JOYSTICK_SEND_COMMAND_PERIOD = 200L
        private const val MAIN_TOPIC = "tank"
        const val MQTT_CONTROL_TOPIC = "$MAIN_TOPIC/in"
        const val MQTT_TELEMETRY_TOPIC = "$MAIN_TOPIC/out"
        const val MQTT_STREAM_TOPIC = "$MAIN_TOPIC/stream"

        enum class ROOMBA_COMMANDS(val ch3: Int, val ch4: Int) {
            START(2, 0),
            STOP(1, 0),
            START_BRUSH(10, 0),
            STOP_BRUSH(11, 0),
            CLEAN(12, 0),
            DOCK(3, 0),
            UNDOCK(4, 0),
            POWEROFF(5, 0),
            STARTSTREAM(20, 0),
            STOPSTREAM(21, 0)
        }
    }
}
