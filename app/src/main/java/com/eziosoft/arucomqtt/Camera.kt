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
import org.opencv.core.*
import kotlin.math.PI
import org.opencv.core.Mat

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.MatOfDouble


class Camera {

    fun calculateCameraPosition2(rvec: Mat, tvec: Mat): Marker {
        val R = Mat(3, 3, CvType.CV_64F)
        Calib3d.Rodrigues(rvec, R)

        val camR = R.t()

        val camRvec = Mat()
        Calib3d.Rodrigues(R, camRvec)

        val scalar = Scalar(-1.0)
        var _camR = Mat(1,3,CvType.CV_64F)
        Core.multiply(camR, scalar, _camR)


        var camTvec = Mat(1,3,CvType.CV_64F)


        val tvec_conv=Mat(1,3,CvType.CV_64F)
        val d = rvec.get(0,0)[0]
        Log.d("aaa", "double : $d")
        tvec_conv.put(0,0,1.0)
        tvec_conv.put(0,1,2.0)
        tvec_conv.put(0,2,3.0)

        _camR.convertTo(_camR, CvType.CV_64F)

        logMat(tvec, "tvec")
        logMat(tvec_conv, "tvec_conv")
        logMat(_camR, "_camR")
        logMat(camR, "camR")

        Core.multiply(tvec_conv, tvec_conv, camTvec)
//        Core.gemm(_camR,tvec_conv , 1.0, Mat(), 0.0, camTvec, 0)


        val marker = Marker(
            265,
            x = 0.0,//camTvec[0, 0][0],
            y = 0.0,// camTvec[0, 0][1],
            z = 0.0// camTvec[0, 0][2]
        )
        return marker
    }


    fun logMat(m: Mat, name: String) {
        Log.d("aaa", "$name:${m.type()} -> ${m.dump()}")
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