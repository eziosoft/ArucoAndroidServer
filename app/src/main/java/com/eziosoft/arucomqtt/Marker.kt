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
import org.opencv.imgproc.Imgproc
import kotlin.math.*

data class Marker(
    val id: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val corners: Mat? = null,
    var centerInPixels: Point = Point(0.0, 0.0),
    var heading: Double = 0.0
) {

    private var size: Int = 0
    private val markerCornersInPixels = arrayOfNulls<Point>(4)


    init {
        corners?.let {
            centerInPixels = getMarkerCenterInPixels(corners)
            heading = getMarkerHeadingInRadians(corners)
            for (i in 0..3) {
                markerCornersInPixels[i] = Point(corners[0, i][0], corners[0, i][1])
            }
            size = sqrt(
                (markerCornersInPixels[0]!!.x - markerCornersInPixels[1]!!.x).pow(2.0) +
                        (markerCornersInPixels[0]!!.y - markerCornersInPixels[1]!!.y).pow(2.0)
            ).toInt()
        }
    }


    override fun toString(): String {
        return "$id--X${x.round(2)} Y${y.round(2)} Z${z.round(2)} H${heading.round(2)}(${
            heading.toDegree().roundToInt()
        })"
    }


    fun getPositionInWorldCoordinates(offsetX: Int = 0, offsetY: Int = 0) = Point(x + offsetX, y + offsetY)


    private fun getMarkerHeadingInRadians(corners: Mat): Double {
        val up = getMarkerFront(corners)
        return atan2(up.x - centerInPixels.x, up.y - centerInPixels.y)
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

    private fun getMarkerFront(corners: Mat): Point {
        var x = 0.0
        var y = 0.0
        for (i in 0..1) {
            x += corners[0, i][0]
            y += corners[0, i][1]
        }
        return Point(x / 2, y / 2)
    }

    fun draw(frame: Mat?) {
        Imgproc.line(frame, markerCornersInPixels[0], markerCornersInPixels[1], c1, 3)
        Imgproc.line(frame, markerCornersInPixels[1], markerCornersInPixels[2], c1, 3)
        Imgproc.line(frame, markerCornersInPixels[2], markerCornersInPixels[3], c1, 3)
        Imgproc.line(frame, markerCornersInPixels[3], markerCornersInPixels[0], c1, 3)
        Imgproc.line(
            frame,
            centerInPixels,
            Point(
                centerInPixels.x + size * 2 / 2f * sin(heading),
                centerInPixels.y + size * 2 / 2f * cos(heading)
            ),
            c2,
            5
        )
        Imgproc.putText(frame, toString(), centerInPixels, 1, 1.0, c1)
    }



}


