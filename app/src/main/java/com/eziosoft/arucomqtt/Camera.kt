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

package com.eziosoft.arucomqtt

import android.util.Log
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import kotlin.math.PI

class Camera {

    fun calculateCameraPosition2(rvec: Mat, tvec: Mat): Marker {
        val R = Mat()
        Calib3d.Rodrigues(rvec, R)

        val camR = R.t()

        val camRvec = Mat()
        Calib3d.Rodrigues(R, camRvec)

        val scalar = Scalar(-1.0)
        val _camR = Mat()
        Core.multiply(camR, scalar, _camR)


        Log.d("aaa", "rvec: ${rvec.dump()}")
        Log.d("aaa", "tvec: ${tvec.dump()}")
        Log.d("aaa", "R: ${R.dump()}")
        Log.v("aaa", "camR: ${camR.dump()}")
        Log.d("aaa", "_camR: ${_camR.dump()}")
        Log.d("aaa", "camRvec: ${camRvec.dump()}")


        val mat1 = Mat(1, 3, CvType.CV_32F) // A matrix with 1 row and 3 columns
        mat1.put(0, 0, 2.0) // Set row 1 , column 1
        mat1.put(0, 1, 0.5) // Set row 1 , column 2
        mat1.put(0, 2, 1.0) // Set row 1 , column 3
        val mat2 = Mat(3, 1, CvType.CV_32F) // A matrix with 1 row and 3 columns
        mat1.put(0, 0, 2.0) // Set row 1 , column 1
        mat1.put(1, 0, 0.5) // Set row 1 , column 2
        mat1.put(2, 0, 1.0) // Set row 1 , column 3


        val camTvec = Mat()
        val s = Scalar(tvec[0, 0][0], tvec[0, 0][1], tvec[0, 0][2])
        Core.multiply(_camR, s, camTvec)

        Log.d("aaa", "camTvec: ${camTvec.dump()}")

        val marker = Marker(
            265,
            x = camTvec[0, 0][0],
            y = camTvec[0, 0][1],
            z = camTvec[0, 0][2]
        )
        return marker
    }

    fun calculateCameraPosition(marker: Marker, frame: Mat): Marker {
        val scale = 5 // scale to be able to draw, TODO change it
        val x = marker.x / scale
        val y = marker.y / scale

        var c = Cartesian(x, y)
        val p = c.toPolar()
        p.rotate(marker.heading)
        c = p.toCartesian()

        val cam = Marker(255, c.x, c.y, -marker.z, null)
        cam.heading = 2 * PI - marker.heading.addAngleRadians(PI / 2)
        return cam
    }


    companion object {
        const val CAMERA_FRONT = 98
        const val CAMERA_BACK = 99
        const val CAMERA_WIDTH = 720
        const val CAMERA_HEIGH = 480

        val CAMERA_MATRIX: Mat = Mat(3, 3, CvType.CV_32F)
        val CAMERA_DISTORTION: Mat = Mat(1, 5, CvType.CV_32F)

        init {
            CAMERA_MATRIX.put(0, 0, 565.5056849747607)
            CAMERA_MATRIX.put(0, 1, 0.0)
            CAMERA_MATRIX.put(0, 2, 360.0)

            CAMERA_MATRIX.put(1, 0, 0.0)
            CAMERA_MATRIX.put(1, 1, 565.5056849747607)
            CAMERA_MATRIX.put(1, 2, 240.0)

            CAMERA_MATRIX.put(2, 0, 0.0)
            CAMERA_MATRIX.put(2, 1, 0.0)
            CAMERA_MATRIX.put(2, 2, 1.0)

            CAMERA_DISTORTION.put(0, 0, 0.08913809371204115)
            CAMERA_DISTORTION.put(0, 1, -0.1701755981832315)
            CAMERA_DISTORTION.put(0, 2, 0.0)
            CAMERA_DISTORTION.put(0, 3, 0.0)
            CAMERA_DISTORTION.put(0, 4, 0.0)
        }
    }
}

//oneplus 5t back camera
//    I/OCV::CameraCalibrator: Average re-projection error: 0.428173
//    I/OCV::CameraCalibrator: Camera matrix: [592.6818457769918, 0, 360;
//    0, 592.6818457769918, 240;
//    0, 0, 1]
//    I/OCV::CameraCalibrator: Distortion coefficients: [0.09613719425745187;
//    -0.2039451180901536;
//    0;
//    0;
//    0]

//oneplus 5t front camera
//    I/OCV::CameraCalibrator: Average re-projection error: 0.415596
//    I/OCV::CameraCalibrator: Camera matrix: [565.5056849747607, 0, 360;
//    0, 565.5056849747607, 240;
//    0, 0, 1]
//    I/OCV::CameraCalibrator: Distortion coefficients: [0.08913809371204115;
//    -0.1701755981832315;
//    0;
//    0;
//    0]