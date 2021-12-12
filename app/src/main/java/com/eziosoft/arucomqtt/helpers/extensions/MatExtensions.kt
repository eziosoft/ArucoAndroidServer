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

package com.eziosoft.arucomqtt.helpers.filters.extensions

import android.util.Log
import com.eziosoft.arucomqtt.helpers.extensions.round
import com.eziosoft.arucomqtt.repository.vision.Marker
import org.opencv.core.CvType
import org.opencv.core.Mat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun Mat.logMat(name: String) {
    Log.d("aaa", "$name:${this.type()} ${this.channels()} -> ${this.dump()}")
}

fun Mat.toList(): List<Double> {
    val arr = mutableListOf<Double>()
    arr.add(this[0, 0][0].round(2))
    arr.add(this[0, 1][0].round(2))
    arr.add(this[0, 2][0].round(2))
    arr.add(this[1, 0][0].round(2))
    arr.add(this[1, 1][0].round(2))
    arr.add(this[1, 2][0].round(2))
    arr.add(this[2, 0][0].round(2))
    arr.add(this[2, 1][0].round(2))
    arr.add(this[2, 2][0].round(2))
    return arr
}

fun Mat.logMatTOArray(name: String) {
    val arr = mutableListOf<Double>()
    arr.add(this[0, 0][0].round(2))
    arr.add(this[0, 1][0].round(2))
    arr.add(this[0, 2][0].round(2))
    arr.add(this[1, 0][0].round(2))
    arr.add(this[1, 1][0].round(2))
    arr.add(this[1, 2][0].round(2))
    arr.add(this[2, 0][0].round(2))
    arr.add(this[2, 1][0].round(2))
    arr.add(this[2, 2][0].round(2))

    Log.i("aaaa", "$name: ${arr.toString()}}")
}


fun rotationMatrixFromEuler(
    roll: Double, pitch: Double,
    yaw: Double
): Mat {
    val cp = cos(pitch)
    val sp = sin(pitch)
    val sr = sin(roll)
    val cr = cos(roll)
    val sy = sin(yaw)
    val cy = cos(yaw)

    val mat = Mat(3, 3, CvType.CV_64FC1)
    mat.put(0, 0, cp * cy)
    mat.put(0, 1, sr * sp * cy - cr * sy)
    mat.put(0, 2, cr * sp * cy + sr * sy)

    mat.put(1, 0, cp * sy)
    mat.put(1, 1, sr * sp * sy + cr * cy)
    mat.put(1, 2, cr * sp * sy - sr * cy)

    mat.put(2, 0, -sp)
    mat.put(2, 1, sr * cp)
    mat.put(2, 2, cr * cp)

    return mat
}


fun rotationMatrixToEulerAngles(R: Mat): Marker.Rotation {
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