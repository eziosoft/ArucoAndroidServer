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

package com.eziosoft.arucomqtt.camera

import com.eziosoft.arucomqtt.Cartesian
import com.eziosoft.arucomqtt.Marker
import com.eziosoft.arucomqtt.MovingAverageFilter
import com.eziosoft.arucomqtt.extensions.addAngleRadians
import com.eziosoft.arucomqtt.extensions.logMat
import com.eziosoft.arucomqtt.extensions.normalizeAngle
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import kotlin.math.PI
import org.opencv.core.Mat

import org.opencv.core.Core
import org.opencv.core.CvType
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt


class CameraPosition {
    private val filterX = MovingAverageFilter(10)
    private val filterY = MovingAverageFilter(10)
    private val filterZ = MovingAverageFilter(10)

    fun calculateCameraPosition3(rvec: Mat, tvec: Mat): Marker {
//        #-- Obtain the rotation matrix tag->camera
//        R_ct    = np.matrix(cv2.Rodrigues(rvec)[0])
//        R_tc    = R_ct.T

        val R_ct = Mat(3, 3, CvType.CV_64F)
        val R_tc = R_ct.t()

        val _1 = Scalar(-1.0)
        val _R_tc = Mat()
        Core.multiply(R_tc, _1, _R_tc)

        val tvec_conv = Mat(3, 1, CvType.CV_64F)
        tvec_conv.put(0, 0, (tvec.t()[0, 0][0]))
        tvec_conv.put(1, 0, (tvec.t()[0, 0][1]))
        tvec_conv.put(2, 0, (tvec.t()[0, 0][2]))
        val pos_camera = Mat()

        _R_tc.logMat("_R_tc")
        tvec_conv.logMat("tvec_conv")
        tvec.logMat("tvec")
        tvec_conv.t().logMat("tvec_conv.t")

        Core.gemm(_R_tc, tvec_conv, 1.0, Mat(), 0.0, pos_camera, 0)
//        pos_camera = -R_tc*np.matrix(tvec).T

        pos_camera.logMat("poscamera")

        val marker = Marker(
            266,
            x = filterX.add(pos_camera[0, 0][0]),
            y = filterY.add(pos_camera[1, 0][0]),
            z = filterZ.add(pos_camera[2, 0][0]),
            heading = 0.0
        )
        return marker
    }

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


//        val bankX = atan2(-camR.get(1, 2)[0], R.get(1, 1)[0])
//        val headingY = atan2(-camR.get(2, 0)[0], R.get(0, 0)[0])
//        val attitudeZ = asin(camR.get(1, 0)[0]).addAngleRadians(PI / 2).normalizeAngle()

        val rotationCam = rotationMatrixToEulerAngles(camR)


        val marker = Marker(
            265,
            x = filterX.add(camTvec[0, 0][0]),
            y = filterY.add(camTvec[1, 0][0]),
            z = filterZ.add(camTvec[2, 0][0]),
            heading = rotationCam.z.addAngleRadians(PI / 2).normalizeAngle()
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


//    def rotationMatrixToEulerAngles(R):
//    assert (isRotationMatrix(R))
//
//    sy = math.sqrt(R[0, 0] * R[0, 0] + R[1, 0] * R[1, 0])
//
//    singular = sy < 1e-6
//
//    if not singular:
//    x = math.atan2(R[2, 1], R[2, 2])
//    y = math.atan2(-R[2, 0], sy)
//    z = math.atan2(R[1, 0], R[0, 0])
//    else:
//    x = math.atan2(-R[1, 2], R[1, 1])
//    y = math.atan2(-R[2, 0], sy)
//    z = 0
//
//    return np.array([x, y, z])

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

