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

package com.eziosoft.arucomqtt.repository.vision.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.SizeF
import kotlin.math.atan

class CameraHelpers {
    fun getCameraParameters(context: Context): CameraParameters {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraCharacteristics =
            cameraManager.getCameraCharacteristics("1") // hardcoded first back camera id

        val focalLength =
            cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                ?.firstOrNull()!!
        val sensorSize =
            cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)!!
        val horizontalAngle =
            (2f * atan((sensorSize.width / (focalLength * 2f)).toDouble())) * 180.0 / Math.PI
        val verticalAngle =
            (2f * atan((sensorSize.height / (focalLength * 2f)).toDouble())) * 180.0 / Math.PI

        return CameraParameters(focalLength, sensorSize, horizontalAngle, verticalAngle)
    }

    data class CameraParameters(
        val focalLength: Float,
        val sensorSize: SizeF,
        val horizontalAngle: Double,
        val verticalAngle: Double
    )
}