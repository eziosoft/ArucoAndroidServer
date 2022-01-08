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
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.SizeF
import org.opencv.core.CvType
import org.opencv.core.Mat
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.tan

class CameraHelpers @Inject constructor() {
    fun getCameraParameters(context: Context, cameraID: String): CameraParameters {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraCharacteristics =
            cameraManager.getCameraCharacteristics(cameraID)


        val focalLength =
            cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                ?.firstOrNull()!!
        val sensorSize =
            cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)!!
        val horizontalAngle =
            2f * atan((sensorSize.width / (focalLength * 2f)).toDouble()) * 180.0 / Math.PI
        val verticalAngle =
            2f * atan((sensorSize.height / (focalLength * 2f)).toDouble()) * 180.0 / Math.PI

        val resolutions =
            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)



        return CameraParameters(
            focalLength,
            sensorSize,
            horizontalAngle,
            verticalAngle,
            resolutions
        )
    }

    fun focalLengthToPixels(widthHeightInPixels: Int, viewAngle: Double) =
        widthHeightInPixels * 0.5 / tan(viewAngle * 0.5 * PI / 180)

    fun focalLengthToPixels(cameraParameters: CameraParameters, width: Int): Double =
        (width / cameraParameters.sensorSize.height * cameraParameters.focalLength).toDouble()

    fun createCameraMatrix(
        focalLengthX: Double,
        focalLengthY: Double,
        width: Int,
        height: Int
    ): Mat {
        val cx = width / 2.0
        val cy = height / 2.0

        val mat = Mat(3, 3, CvType.CV_32F)
        mat.put(0, 0, focalLengthX)
        mat.put(0, 1, 0.0)
        mat.put(0, 2, cx)

        mat.put(1, 0, 0.0)
        mat.put(1, 1, focalLengthY)
        mat.put(1, 2, cy)

        mat.put(2, 0, 0.0)
        mat.put(2, 1, 0.0)
        mat.put(2, 2, 1.0)

        return mat
    }

    data class CameraParameters(
        val focalLength: Float,
        val sensorSize: SizeF,
        val horizontalAngle: Double,
        val verticalAngle: Double,
        val resolutions: StreamConfigurationMap?
    )
}