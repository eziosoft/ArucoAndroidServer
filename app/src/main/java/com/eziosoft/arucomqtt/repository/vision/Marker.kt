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


package com.eziosoft.arucomqtt.repository.vision

import com.eziosoft.arucomqtt.helpers.extensions.round
import com.eziosoft.arucomqtt.helpers.extensions.toDegree
import com.eziosoft.arucomqtt.repository.vision.helpers.c1
import com.eziosoft.arucomqtt.repository.vision.helpers.c2
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import kotlin.math.*


data class Marker2(
    val id: Int = -1,
    val position3d: Position3d,
    val rotation: Rotation,
    val matrices: Matrices?,
    val calculateHeadingFromPixels: Boolean = false
) {

    init {
        if (calculateHeadingFromPixels) {
            rotation.z = getHeadingFromCorners(matrices?.corners!!)
        }
    }

    fun getCenterInPixels(corners: Mat): Point {
        var x = 0.0
        var y = 0.0
        for (i in 0..3) {
            x += corners[0, i][0]
            y += corners[0, i][1]
        }
        return Point(x / 4, y / 4)
    }

    fun getFrontFromCornersInPixels(corners: Mat): Point {
        var x = 0.0
        var y = 0.0
        for (i in 2..3) {
            x += corners[0, i][0]
            y += corners[0, i][1]
        }
        return Point(x / 2, y / 2)
    }

    fun getHeadingFromCorners(corners: Mat): Double {
        val front = getFrontFromCornersInPixels(corners)
        val center = getCenterInPixels(corners)
        return atan2(center.x - front.x, center.y - front.y)
    }

    private fun getMarkerSize(markerCornersInPixels: Array<Point>) = sqrt(
        (markerCornersInPixels[0].x - markerCornersInPixels[1].x).pow(2.0) +
                (markerCornersInPixels[0].y - markerCornersInPixels[1].y).pow(2.0)
    )

    override fun toString(): String {
        var s =
            "$id--X${position3d.x.round(2)} \tY${position3d.y.round(2)} \tZ${position3d.z.round(2)} " +
                    "\tH${rotation.z.toDegree().roundToInt()}"

        s += "\nrX${rotation.x.toDegree().roundToInt()}" +
                "\nrY${rotation.y.toDegree().roundToInt()}" +
                "\nrZ${rotation.z.toDegree().roundToInt()}"
        return s
    }

    fun cornersMatToPointArray(corners: Mat): Array<Point> {
        val markerCornersInPixels = arrayOfNulls<Point>(4)
        for (i in 0..3) {
            markerCornersInPixels[i] = Point(corners[0, i][0], corners[0, i][1])
        }
        return markerCornersInPixels as Array<Point>
    }

    fun draw(frame: Mat?) {
        val markerCornersInPixels = cornersMatToPointArray(matrices?.corners!!)
        val centerInPixels = getCenterInPixels(matrices.corners)
        val markerSize = getMarkerSize(markerCornersInPixels)
        val heading = getHeadingFromCorners(matrices.corners)
        try {
            Imgproc.line(frame, markerCornersInPixels[0], markerCornersInPixels[1], c1, 3)
            Imgproc.line(frame, markerCornersInPixels[1], markerCornersInPixels[2], c1, 3)
            Imgproc.line(frame, markerCornersInPixels[2], markerCornersInPixels[3], c1, 3)
            Imgproc.line(frame, markerCornersInPixels[3], markerCornersInPixels[0], c1, 3)
            Imgproc.line(
                frame,
                centerInPixels,
                Point(
                    centerInPixels.x + markerSize * 2 / 2f * sin(heading),
                    centerInPixels.y + markerSize * 2 / 2f * cos(heading)
                ),
                c2,
                5
            )
            Imgproc.putText(frame, toString(), centerInPixels, 1, 1.0, c1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class Position3d(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0)
data class Rotation(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0)
data class Matrices(val corners: Mat?, val rvec: Mat?, val tvec: Mat?)


