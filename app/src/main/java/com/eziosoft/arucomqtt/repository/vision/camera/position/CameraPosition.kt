
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
import com.eziosoft.arucomqtt.repository.vision.Marker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI

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


    private lateinit var cam1: Marker
    private var cam2 = Marker(1002, 0.0, 0.0, 0.0)
    private lateinit var cam3: Marker


    init {
        deviceAttitudeProvider.setDeviceAttitudeListener(this)
    }


    override fun onDeviceAttitude(
        attitude: DeviceAttitudeProvider.Attitude,
        rotationMatrix: FloatArray
    ) {
        rotationMatrixFromAcc = calculateRotationMatrixFromAccAngles(attitude)
    }

    fun calculateRotationMatrixFromAccAngles(deviceAttitude: DeviceAttitudeProvider.Attitude): Mat {
        val attitudeCorrected = DeviceAttitudeProvider.Attitude(
            deviceAttitude.azimuth,
            deviceAttitude.roll.toRadian().invertAngleRadians().normalizeAngle().toDegree(),
            -deviceAttitude.pitch
        )

        return rotationMatrixFromEuler(
            attitudeCorrected.pitch.toRadian(),
            attitudeCorrected.roll.toRadian(),
            cam2.heading //use heading from camera2 from marker not from compass
//            attitudeCorrected.azimuth.toRadian().addAngleRadians(10.0.toRadian())
//                .normalizeAngle()
        )
    }

    fun getLastCamera1Position() = cam1
    fun getLastCamera2Position() = cam2
    fun getLastCamera3Position() = cam3

    private var rotationMatrixFromAcc = Mat(3, 3, CvType.CV_64F)
    fun calculateCameraPosition3(cam: Marker): Marker {
        val camR = rotationMatrixFromAcc

        val _camR = Mat()
        val _1 = Scalar(-1.0)
        Core.multiply(camR, _1, _camR)

        val tvec_conv = Mat(3, 1, CvType.CV_64F)
        tvec_conv.put(0, 0, cam.tvec?.get(0, 0)?.get(0)!!)
        tvec_conv.put(1, 0, cam.tvec.get(0, 0)?.get(1)!!)
        tvec_conv.put(2, 0, cam.tvec[0, 0][2])


        val camTvec = Mat(1, 3, CvType.CV_64F)
        Core.gemm(_camR, tvec_conv, 1.0, Mat(), 0.0, camTvec, 0)

        val rotationCam = rotationMatrixToEulerAngles(camR)

        val marker = Marker(
            1003,
            y = filterYcam3.add(-camTvec[0, 0][0]),
            x = filterXcam3.add(camTvec[1, 0][0]),
            z = filterZcam3.add(camTvec[2, 0][0]),
            rotation = rotationCam
        )

        camTvec.release()
        camR.release()
        _camR.release()
        tvec_conv.release()

        cam3 = marker
        return marker
    }

    fun calculateCameraPosition2(cam: Marker): Marker {
        val R = Mat(3, 3, CvType.CV_32FC1)
        Calib3d.Rodrigues(cam.rvec, R)
        val camR = R.t()

        val _camR = Mat()
        val _1 = Scalar(-1.0)
        Core.multiply(camR, _1, _camR)

        val tvec_conv = Mat(3, 1, CvType.CV_64F)
        tvec_conv.put(0, 0, cam.tvec?.get(0, 0)?.get(0)!!)
        tvec_conv.put(1, 0, cam.tvec.get(0, 0)?.get(1)!!)
        tvec_conv.put(2, 0, cam.tvec[0, 0][2])


        val camTvec = Mat(1, 3, CvType.CV_64F)
        Core.gemm(_camR, tvec_conv, 1.0, Mat(), 0.0, camTvec, 0)

        val rotationCam = rotationMatrixToEulerAngles(camR)
        rotationCam.offsetZ(PI_2)

        val marker = Marker(
            1002,
            x = filterXcam2.add(camTvec[0, 0][0]),
            y = filterYcam2.add(camTvec[1, 0][0]),
            z = filterZcam2.add(camTvec[2, 0][0]),
            rotation = rotationCam
        )

        camTvec.release()
        camR.release()
        _camR.release()
        tvec_conv.release()
        R.release()

        cam2 = marker
        return marker
    }

    fun calculateCameraPosition1(marker: Marker): Marker {
        val x = marker.x
        val y = marker.y

        var c = Cartesian(x, y)
        val p = c.toPolar()
        p.rotate(marker.heading)
        c = p.toCartesian()

        val cam = Marker(1001, c.x, -c.y, marker.z, null)
        cam.heading = 2 * PI - marker.heading.addAngleRadians(PI / 2)
        cam1 = cam
        return cam
    }


}

