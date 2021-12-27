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

package com.eziosoft.arucomqtt.repository.vision.camera.position

import com.eziosoft.arucomqtt.Cartesian
import com.eziosoft.arucomqtt.MovingAverageFilter
import com.eziosoft.arucomqtt.helpers.extensions.*
import com.eziosoft.arucomqtt.helpers.filters.extensions.rotationMatrixFromEuler
import com.eziosoft.arucomqtt.helpers.filters.extensions.rotationMatrixToEulerAngles
import com.eziosoft.arucomqtt.repository.phoneAttitude.DeviceAttitudeProvider
import com.eziosoft.arucomqtt.repository.vision.Marker2
import com.eziosoft.arucomqtt.repository.vision.Position3d
import com.eziosoft.arucomqtt.repository.vision.Rotation
import com.eziosoft.arucomqtt.repository.vision.camera.Camera
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class CameraPosition @ExperimentalCoroutinesApi
@Inject constructor(
    deviceAttitudeProvider: DeviceAttitudeProvider
) :
    DeviceAttitudeProvider.DeviceAttitudeListener {
    private val filterXcam2 = MovingAverageFilter(15)
    private val filterYcam2 = MovingAverageFilter(15)
    private val filterZcam2 = MovingAverageFilter(15)

    private val filterXcam3 = MovingAverageFilter(5)
    private val filterYcam3 = MovingAverageFilter(5)
    private val filterZcam3 = MovingAverageFilter(1)

    private lateinit var cam3: Camera

    private lateinit var currentDeviceAttitude: DeviceAttitudeProvider.Attitude

    init {
        deviceAttitudeProvider.setDeviceAttitudeListener(this)
    }


    override fun onDeviceAttitude(
        attitude: DeviceAttitudeProvider.Attitude,
        rotationMatrix: FloatArray
    ) {
        currentDeviceAttitude = attitude
    }

    private fun calculateRotationMatrixFromAccAngles(
        deviceAttitude: DeviceAttitudeProvider.Attitude,
        cam2Heading: Double
    ): Mat {
        val attitudeCorrected = DeviceAttitudeProvider.Attitude(
            deviceAttitude.azimuth,
            deviceAttitude.roll.toRadian().invertAngleRadians().normalizeAngle().toDegree(),
            -deviceAttitude.pitch
        )

        return rotationMatrixFromEuler(
            attitudeCorrected.pitch.toRadian(),
            attitudeCorrected.roll.toRadian(),
            cam2Heading
            //use heading from camera2 from marker not from compass
        )
    }

    fun getLastCamera3Position() = cam3

    fun calculateCameraPosition3(marker: Marker2, cam2: Camera): Camera {
        val camR = calculateRotationMatrixFromAccAngles(
            currentDeviceAttitude,
            cam2.rotation.z
        )

        val _camR = Mat()
        val _1 = Scalar(-1.0)
        Core.multiply(camR, _1, _camR)

        val tvec_conv = Mat(3, 1, CvType.CV_64F)
        tvec_conv.put(0, 0, marker.matrices?.tvec?.get(0, 0)?.get(0)!!)
        tvec_conv.put(1, 0, marker.matrices.tvec.get(0, 0)?.get(1)!!)
        tvec_conv.put(2, 0, marker.matrices.tvec[0, 0][2])


        val camTvec = Mat(1, 3, CvType.CV_64F)
        Core.gemm(_camR, tvec_conv, 1.0, Mat(), 0.0, camTvec, 0)

        val rotationCam = rotationMatrixToEulerAngles(camR)
        rotationCam.z = rotationCam.z

        val cam = Camera(
            1003,
            Position3d(
                x = filterYcam3.add(camTvec[0, 0][0]),
                y = filterXcam3.add(camTvec[1, 0][0]),
                z = filterZcam3.add(camTvec[2, 0][0])
            ),
            rotation = rotationCam
        )

        camTvec.release()
        camR.release()
        _camR.release()
        tvec_conv.release()

        cam3 = cam
        return cam
    }

    fun calculateCameraPosition2(marker: Marker2): Camera {
        val R = Mat(3, 3, CvType.CV_32FC1)
        Calib3d.Rodrigues(marker.matrices?.rvec, R)
        val camR = R.t()

        val _camR = Mat()
        val _1 = Scalar(-1.0)
        Core.multiply(camR, _1, _camR)

        val tvec_conv = Mat(3, 1, CvType.CV_64F)
        tvec_conv.put(0, 0, marker.matrices?.tvec?.get(0, 0)?.get(0)!!)
        tvec_conv.put(1, 0, marker.matrices.tvec.get(0, 0)?.get(1)!!)
        tvec_conv.put(2, 0, marker.matrices.tvec[0, 0][2])


        val camTvec = Mat(1, 3, CvType.CV_64F)
        Core.gemm(_camR, tvec_conv, 1.0, Mat(), 0.0, camTvec, 0)

        val rotationCam = rotationMatrixToEulerAngles(camR)

        val cam = Camera(
            1002, Position3d(
                x = filterXcam2.add(camTvec[0, 0][0]),
                y = filterYcam2.add(camTvec[1, 0][0]),
                z = filterZcam2.add(camTvec[2, 0][0])
            ),
            rotation = rotationCam
        )

        camTvec.release()
        camR.release()
        _camR.release()
        tvec_conv.release()
        R.release()
        return cam
    }

    fun calculateCameraPosition1(marker: Marker2): Camera {
        val x = marker.position3d.x
        val y = marker.position3d.y

        var c = Cartesian(x, y)
        val p = c.toPolar()
        p.rotate(marker.rotation.z)
        c = p.toCartesian()

        return Camera(
            1001,
            Position3d(c.x, -c.y, marker.position3d.z),
            Rotation(
                x = 0.0,
                y = 0.0,
                z = marker.rotation.z.addAngleRadians(PI_2).invertAngleRadians().normalizeAngle()
            )
        )
    }
}
