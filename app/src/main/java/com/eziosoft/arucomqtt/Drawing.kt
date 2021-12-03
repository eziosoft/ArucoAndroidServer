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
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.sin

private val path = mutableListOf<Point>() //to draw a path

fun Point.addToPath() {
    path.add(this)
}

fun drawPath(frame: Mat) {
    if (path.isNotEmpty()) {
        val matOfPoint = MatOfPoint()
        matOfPoint.fromList(path)
        val matOfPointList = arrayListOf(matOfPoint)

        Imgproc.polylines(frame, matOfPointList, false, COLOR_RED, 2)
        if (path.size > 5000) {
            path.removeAt(0)
        }
    }
}

fun drawRobot(frame: Mat, point: Point, heading: Double) {
    Imgproc.circle(frame, point, 10, COLOR_GREEN, 2)
    Imgproc.line(
        frame,
        point,
        Point(point.x + 50 * sin(heading), point.y + 50 * cos(heading)),
        COLOR_GREEN,
        2
    )
}

fun drawCenterLines(frame: Mat) {
    Imgproc.line(
        frame,
        Point(frame.width() / 2.0, 0.0),
        Point(frame.width() / 2.0, frame.height().toDouble()),
        COLOR_PINK,
        1
    )
    Imgproc.line(
        frame,
        Point(0.0, frame.height() / 2.0),
        Point(frame.width().toDouble(), frame.height() / 2.0),
        COLOR_PINK,
        1
    )
}