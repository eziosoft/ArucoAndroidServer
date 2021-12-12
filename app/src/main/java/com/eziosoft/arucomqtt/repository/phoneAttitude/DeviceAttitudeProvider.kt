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

package com.eziosoft.arucomqtt.repository.phoneAttitude

/*
 * Created by Bartosz Szczygiel on 3/31/21 9:35 PM
 *  Copyright (c) 2021 . All rights reserved.
 *  Last modified 3/30/21 9:52 PM
 */


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.*
import javax.inject.Singleton


@ExperimentalCoroutinesApi
@Singleton
@Suppress("MagicNumber")
class DeviceAttitudeProvider(
    private val sensorManager: SensorManager
) {
    private val SENSOR_TYPE = Sensor.TYPE_ROTATION_VECTOR

    private val rotationVectorSensor: Sensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    private var rotMat = FloatArray(9)
    private var vals = FloatArray(3)

    lateinit var attitudeListener: DeviceAttitudeListener

    fun setDeviceAttitudeListener(deviceAttitudeListener: DeviceAttitudeListener) {
        this.attitudeListener = deviceAttitudeListener
    }

    val callback = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == SENSOR_TYPE) {
                SensorManager.getRotationMatrixFromVector(
                    rotMat,
                    event.values
                )

                SensorManager
                    .remapCoordinateSystem(
                        rotMat,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Y,
                        rotMat
                    )
                SensorManager.getOrientation(rotMat, vals)
                val azimuth = Math.toDegrees(vals[0].toDouble()) // in degrees [-180, +180]
                val pitch = Math.toDegrees(vals[1].toDouble())
                val roll = Math.toDegrees(vals[2].toDouble())

                if (this@DeviceAttitudeProvider::attitudeListener.isInitialized) {
                    attitudeListener.onDeviceAttitude(Attitude(azimuth, pitch, roll), rotMat)
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) =Unit

    }


    init {
        Log.d(TAG, "start: AttitudeProvider")
        sensorManager.registerListener(
            callback,
            rotationVectorSensor, 10000
        )
    }

    fun stop() {
        Log.d(TAG, "stop: AttitudeProvider ")
        sensorManager.unregisterListener(callback)
    }

    data class Attitude(val azimuth: Double, val pitch: Double, val roll: Double)


    companion object {
        const val TAG = "aaaa"
    }

    interface DeviceAttitudeListener {
        fun onDeviceAttitude(attitude: Attitude, rotationMatrix:FloatArray)
    }
}
