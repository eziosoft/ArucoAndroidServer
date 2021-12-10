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

import org.opencv.aruco.Aruco
import org.opencv.core.CvType
import org.opencv.core.Mat

class CameraConfiguration {
    companion object {
        val DICTIONARY = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_100)
        const val MARKER_LENGTH = 170F //170mm

        const val CAMERA_FRONT = 98
        const val CAMERA_BACK = 99
        const val CAMERA_WIDTH = 1280
        const val CAMERA_HEIGH = 720

        val CAMERA_MATRIX: Mat = Mat(3, 3, CvType.CV_32F)
        val CAMERA_DISTORTION: Mat = Mat(1, 5, CvType.CV_32F)

        init {
            CAMERA_MATRIX.put(0, 0, 1273.417222540211)
            CAMERA_MATRIX.put(0, 1, 0.0)
            CAMERA_MATRIX.put(0, 2, 640.0)

            CAMERA_MATRIX.put(1, 0, 0.0)
            CAMERA_MATRIX.put(1, 1, 1273.417222540211)
            CAMERA_MATRIX.put(1, 2, 360.0)

            CAMERA_MATRIX.put(2, 0, 0.0)
            CAMERA_MATRIX.put(2, 1, 0.0)
            CAMERA_MATRIX.put(2, 2, 1.0)

            CAMERA_DISTORTION.put(0, 0, 0.1632096059891273)
            CAMERA_DISTORTION.put(0, 1, -0.5399431125824645)
            CAMERA_DISTORTION.put(0, 2, 0.0)
            CAMERA_DISTORTION.put(0, 3, 0.0)
            CAMERA_DISTORTION.put(0, 4, 0.0)
        }
    }
}
//front
//FPS@1280x720
//I/OCV::CameraCalibrator: Average re-projection error: 0.533803
//I/OCV::CameraCalibrator: Camera matrix: [1273.417222540211, 0, 640;
//0, 1273.417222540211, 360;
//0, 0, 1]
//I/OCV::CameraCalibrator: Distortion coefficients: [0.1632096059891273;
//-0.5399431125824645;
//0;
//0;
//0]

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
