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

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

data class Marker(val corners: Mat,  val ID: Int) {
    private val center: Point
    private val heading: Double
    private val size: Int
    private val markerCorners = arrayOfNulls<Point>(4)

    @Transient
    private val c1 = Scalar(255.0, 100.0, 0.0)

    @Transient
    private val c2 = Scalar(255.0, 0.0, 255.0)

    private fun getMarkerHeading(corners: Mat): Double {
        val up = getMarkerUp(corners)
        return atan2(up.x - center.x, up.y - center.y)
    }

    private fun getMarkerCenter(corners: Mat): Point {
        val c = corners[0, 0]
        var x = 0.0
        var y = 0.0
        for (i in 0..3) {
            x += corners[0, i][0]
            y += corners[0, i][1]
        }
        return Point(x / 4, y / 4)
    }

    private fun getMarkerUp(corners: Mat): Point {
        val c = corners[0, 0]
        var x = 0.0
        var y = 0.0
        for (i in 0..1) {
            x += corners[0, i][0]
            y += corners[0, i][1]
        }
        return Point(x / 2, y / 2)
    }

    fun draw(frame: Mat?) {
        Imgproc.line(frame, markerCorners[0], markerCorners[1], c1, 3)
        Imgproc.line(frame, markerCorners[1], markerCorners[2], c1, 3)
        Imgproc.line(frame, markerCorners[2], markerCorners[3], c1, 3)
        Imgproc.line(frame, markerCorners[3], markerCorners[0], c1, 3)
        Imgproc.line(
            frame,
            center,
            Point(
                center.x + size / 2f * Math.sin(heading),
                center.y + size / 2f * Math.cos(heading)
            ),
            c2,
            5
        )
        Imgproc.putText(frame, ID.toString(), center, 1, 1.0, c1)
    }

    init {
        center = getMarkerCenter(corners)
        heading = getMarkerHeading(corners)
        for (i in 0..3) {
            markerCorners[i] = Point(corners[0, i][0], corners[0, i][1])
        }
        size = sqrt(
            (markerCorners[0]!!.x - markerCorners[1]!!.x).pow(2.0) + (markerCorners[0]!!.y - markerCorners[1]!!.y).pow(2.0)
        ).toInt()
    }
}