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


package com.eziosoft.arucomqtt.vision

import com.eziosoft.arucomqtt.c1
import com.eziosoft.arucomqtt.c2
import com.eziosoft.arucomqtt.helpers.filters.extensions.addAngleRadians
import com.eziosoft.arucomqtt.helpers.filters.extensions.normalizeAngle
import com.eziosoft.arucomqtt.helpers.filters.extensions.round
import com.eziosoft.arucomqtt.helpers.filters.extensions.toDegree
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.lang.Exception
import kotlin.math.*

data class Marker(
    val id: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val corners: Mat? = null,
    var heading: Double = 0.0,
    val rotation: Rotation? = null,
    val rvec: Mat? = null,
    val tvec: Mat? = null
) {

    data class Rotation(var x: Double, var y: Double, var z: Double) {
        fun offsetX(angleRad: Double) {
            x = x.addAngleRadians(angleRad).normalizeAngle()
        }

        fun offsetY(angleRad: Double) {
            y = y.addAngleRadians(angleRad).normalizeAngle()
        }

        fun offsetZ(angleRad: Double) {
            z = z.addAngleRadians(angleRad).normalizeAngle()
        }
    }

    private val markerCornersInPixels = arrayOfNulls<Point>(4)
    private lateinit var centerInPixels: Point


    init {
        heading = rotation?.z ?: 0.0
        corners?.let {
            centerInPixels = getMarkerCenterInPixels(it)
            for (i in 0..3) {
                markerCornersInPixels[i] = Point(it[0, i][0], it[0, i][1])
            }
            heading = rotation?.z ?: getHeadingAngleFromPixels(it)
        }

    }

    private fun getMarkerCenterInPixels(corners: Mat): Point {
        var x = 0.0
        var y = 0.0
        for (i in 0..3) {
            x += corners[0, i][0]
            y += corners[0, i][1]
        }
        return Point(x / 4, y / 4)
    }

    private fun getHeadingAngleFromPixels(corners: Mat): Double {
        val up = getMarkerFront(corners)
        return atan2(up.x - centerInPixels.x, up.y - centerInPixels.y)
    }

    private fun getMarkerFront(corners: Mat): Point {
        var x = 0.0
        var y = 0.0
        for (i in 0..1) {
            x += corners[0, i][0]
            y += corners[0, i][1]
        }
        return Point(x / 2, y / 2)
    }

    private fun getMarkerSize() = sqrt(
        (markerCornersInPixels[0]!!.x - markerCornersInPixels[1]!!.x).pow(2.0) +
                (markerCornersInPixels[0]!!.y - markerCornersInPixels[1]!!.y).pow(2.0)
    )


    fun draw(frame: Mat?) {
        try {
            Imgproc.line(frame, markerCornersInPixels[0], markerCornersInPixels[1], c1, 3)
            Imgproc.line(frame, markerCornersInPixels[1], markerCornersInPixels[2], c1, 3)
            Imgproc.line(frame, markerCornersInPixels[2], markerCornersInPixels[3], c1, 3)
            Imgproc.line(frame, markerCornersInPixels[3], markerCornersInPixels[0], c1, 3)
            Imgproc.line(
                frame,
                centerInPixels,
                Point(
                    centerInPixels.x + getMarkerSize() * 2 / 2f * sin(heading),
                    centerInPixels.y + getMarkerSize() * 2 / 2f * cos(heading)
                ),
                c2,
                5
            )
            Imgproc.putText(frame, toString(), centerInPixels, 1, 1.0, c1)
        } catch (e: Exception) {
        }
    }

    override fun toString(): String {
        var s = "$id--X${x.round(2)} \tY${y.round(2)} \tZ${z.round(2)} " +
                "\tH${heading.toDegree().roundToInt()}"

        if (rotation != null) {
            s += "\nrX${rotation.x.toDegree().roundToInt()}" +
                    "\nrY${rotation.y.toDegree().roundToInt()}" +
                    "\nrZ${rotation.z.toDegree().roundToInt()}"
        }
        return s
    }
}


