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

package com.eziosoft.arucomqtt.vision.camera

import com.eziosoft.arucomqtt.Cartesian
import com.eziosoft.arucomqtt.vision.Marker
import com.eziosoft.arucomqtt.MovingAverageFilter
import com.eziosoft.arucomqtt.helpers.filters.extensions.PI_2
import com.eziosoft.arucomqtt.helpers.filters.extensions.addAngleRadians
import com.eziosoft.arucomqtt.helpers.filters.extensions.logMat
import com.eziosoft.arucomqtt.helpers.filters.extensions.normalizeAngle
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import kotlin.math.PI
import org.opencv.core.Mat

import org.opencv.core.Core
import org.opencv.core.CvType
import kotlin.math.atan2
import kotlin.math.sqrt


class CameraPosition {
    private val filterX = MovingAverageFilter(15)
    private val filterY = MovingAverageFilter(15)
    private val filterZ = MovingAverageFilter(15)

    fun calculateCameraPosition2(cam: Marker): Marker {
        val R = Mat(3, 3, CvType.CV_32FC1)
        Calib3d.Rodrigues(cam.rvec, R)
        val camR = R.t()


        val _camR = Mat(1, 3, CvType.CV_64F)
        val _1 = Scalar(-1.0)
        Core.multiply(camR, _1, _camR)

        val tvec_conv = Mat(3, 1, CvType.CV_64F)
        tvec_conv.put(0, 0, (cam.tvec?.get(0, 0)?.get(0)!!))
        tvec_conv.put(1, 0, (cam.tvec.get(0, 0)?.get(1)!!))
        tvec_conv.put(2, 0, (cam.tvec[0, 0][2]))


        val camTvec = Mat(1, 3, CvType.CV_64F)
        Core.gemm(_camR, tvec_conv, 1.0, Mat(), 0.0, camTvec, 0)

        val rotationCam = rotationMatrixToEulerAngles(camR)
        rotationCam.offsetZ(PI_2)

        val marker = Marker(
            265,
            x = filterX.add(camTvec[0, 0][0]),
            y = filterY.add(camTvec[1, 0][0]),
            z = filterZ.add(camTvec[2, 0][0]),
            rotation = rotationCam
        )

        camTvec.release()
        camR.release()
        _camR.release()
        tvec_conv.release()
        R.release()
        return marker
    }


    fun calculateCameraPosition(marker: Marker, frame: Mat): Marker {
        val x = marker.x
        val y = marker.y

        var c = Cartesian(x, y)
        val p = c.toPolar()
        p.rotate(marker.heading)
        c = p.toCartesian()

        val cam = Marker(255, c.x, -c.y, marker.z, null)
        cam.heading = 2 * PI - marker.heading.addAngleRadians(PI / 2)
        return cam
    }


    private fun rotationMatrixToEulerAngles(R: Mat): Marker.Rotation {
        val sy = sqrt(R[0, 0][0] * R[0, 0][0] + R[1, 0][0] * R[1, 0][0])
        val singular = sy < 1e-6

        return if (!singular) {
            val x = atan2(R[2, 1][0], R[2, 2][0])
            val y = atan2(-R[2, 0][0], sy)
            val z = atan2(R[1, 0][0], R[0, 0][0])
            Marker.Rotation(x, y, z)
        } else {
            val x = atan2(-R[1, 2][0], R[1, 1][0])
            val y = atan2(-R[2, 0][0], sy)
            val z = 0.0
            Marker.Rotation(x, y, z)
        }

    }

}

