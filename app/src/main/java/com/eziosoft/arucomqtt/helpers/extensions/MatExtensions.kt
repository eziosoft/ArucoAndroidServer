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
import org.opencv.core.CvType
import org.opencv.core.Mat

fun Mat.logMat(name: String) {
    Log.d("aaa", "$name:${this.type()} ${this.channels()} -> ${this.dump()}")
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

/** this conversion uses NASA standard aeroplane conventions as described on page:
 * https://www.euclideanspace.com/maths/geometry/rotations/euler/index.htm
 * Coordinate System: right hand
 * Positive angle: right hand
 * Order of euler angles: heading first, then attitude, then bank
 * matrix row column ordering:
 * [m00 m01 m02]
 * [m10 m11 m12]
 * [m20 m21 m22] */
fun toRotationMatrix( heading: Double,  bank: Double,attitude: Double): Mat {
    // Assuming the angles are in radians.
    val ch = Math.cos(heading)
    val sh = Math.sin(heading)
    val ca = Math.cos(attitude)
    val sa = Math.sin(attitude)
    val cb = Math.cos(bank)
    val sb = Math.sin(bank)

//    val ch = Math.cos(attitude)
//    val sh = Math.sin(attitude)
//    val ca = Math.cos(heading)
//    val sa = Math.sin(heading)
//    val cb = Math.cos(bank)
//    val sb = Math.sin(bank)

//    val ch = Math.cos(heading)
//    val sh = Math.sin(heading)
//    val ca = Math.cos(attitude)
//    val sa = Math.sin(attitude)
//    val cb = Math.cos(bank)
//    val sb = Math.sin(bank)


    val m00 = ch * ca
    val m01 = sh * sb - ch * sa * cb
    val m02 = ch * sa * sb + sh * cb
    val m10 = sa
    val m11 = ca * cb
    val m12 = -ca * sb
    val m20 = -sh * ca
    val m21 = sh * sa * cb + ch * sb
    val m22 = -sh * sa * sb + ch * cb

    val mat = Mat(3, 3, CvType.CV_64FC1)
    mat.put(0, 0, m00)
    mat.put(0, 1, m01)
    mat.put(0, 2, m02)
    mat.put(1, 0, m10)
    mat.put(1, 1, m11)
    mat.put(1, 2, m12)
    mat.put(2, 0, m20)
    mat.put(2, 1, m21)
    mat.put(2, 1, m22)
    return mat
}