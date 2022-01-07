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
import androidx.core.math.MathUtils.clamp
import androidx.viewbinding.BuildConfig
import com.eziosoft.arucomqtt.helpers.extensions.TWO_PI
import com.eziosoft.arucomqtt.helpers.extensions.normalizeAngle
import com.eziosoft.arucomqtt.helpers.extensions.toDegree
import com.eziosoft.arucomqtt.repository.pid.MiniPID
import com.eziosoft.mqtt_test.repository.mqtt.Mqtt
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

    private fun sendChannels(ch1: Int, ch2: Int, ch3: Int, ch4: Int) {

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
                    (100).toByte(),
                    (100).toByte(),
                    (100).toByte(),
                    (100).toByte()
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

    fun robotStop() {
        sendChannels(0, 0, 0, 0)
    }


    private val pidStearing = MiniPID(0.5, 0.00001, 0.0)
    private val pidSpeed = MiniPID(0.5, 0.0000, 0.0)

    fun robotNavigation(
        currentHeading: Double,
        headingToTarget: Double,
        distanceToTarget: Double,
        targetReached: (Boolean) -> Unit
    ) {
        val headingDifference = currentHeading.normalizeAngle() -  headingToTarget.normalizeAngle()

        var headingDiffrenceCorrected = headingDifference
        if (headingDiffrenceCorrected > PI) headingDiffrenceCorrected -= TWO_PI
        if (headingDifference < -PI) headingDiffrenceCorrected += TWO_PI

        Log.d(
            "aaa",
            "robotNavigation: heading = ${currentHeading.toDegree().toInt()}, headingTarget=${headingToTarget.toDegree().toInt()}, diff=${headingDifference.toDegree().toInt()}, diff corr= ${headingDiffrenceCorrected.toDegree().toInt()}"
        )

        pidStearing.setOutputLimits(-1.0, 1.0)
        val stearing = pidStearing.getOutput(headingDifference, 0.0)
        var ch1: Int = (stearing * 100).toInt()
        ch1 = clamp(ch1, -20, 20)

        pidStearing.setOutputLimits(-1.0, 1.0)
        val speed = pidSpeed.getOutput(distanceToTarget, 0.0)
        var ch2: Int = -(speed * 100).toInt()
        ch2 = clamp(ch2, -20, 20)

        if (distanceToTarget < WP_RADIUS) {
//            sendChannels(0, 0, 0, 0)
            targetReached(false)
        } else {
            sendChannels(ch1, ch2, 0, 0)
            targetReached(false)
        }
    }


    companion object {
        const val JOYSTICK_SEND_COMMAND_PERIOD = 200L
        const val WP_RADIUS = 100
        private const val MAIN_TOPIC = "tank"
        const val MQTT_CONTROL_TOPIC = "$MAIN_TOPIC/in"
        const val MQTT_TELEMETRY_TOPIC = "$MAIN_TOPIC/out"
        const val MQTT_STREAM_TOPIC = "$MAIN_TOPIC/stream"
    }
}