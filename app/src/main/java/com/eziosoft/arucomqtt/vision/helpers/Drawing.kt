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

package com.eziosoft.arucomqtt.vision.helpers

import com.eziosoft.arucomqtt.COLOR_PINK
import com.eziosoft.arucomqtt.COLOR_RED
import com.eziosoft.arucomqtt.helpers.filters.extensions.invertAngleRadians
import com.eziosoft.arucomqtt.vision.Marker
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.sin

const val SCALE_TO_DRAW = 10
private val path = mutableListOf<Point>() //to draw a path

fun Marker.addToPath(frame: Mat) {
    val offsetX: Double = frame.width() / 2.0
    val offsetY: Double = frame.height() / 2.0
    path.add(
        Point(
            this.x / SCALE_TO_DRAW + offsetX,
            this.y / SCALE_TO_DRAW + offsetY
        )
    )
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

fun drawRobot(frame: Mat, marker: Marker, color: Scalar) {
    val offsetX: Double = frame.width() / 2.0
    val offsetY: Double = frame.height() / 2.0

    val p = Point(
        marker.x / SCALE_TO_DRAW + offsetX,
        marker.y / SCALE_TO_DRAW + offsetY
    )
    Imgproc.circle(frame, p, 10, color, 2)
    Imgproc.line(
        frame,
        p,
        Point(
            p.x + 50 * sin(-marker.heading.invertAngleRadians()),
            p.y + 50 * cos(-marker.heading.invertAngleRadians())
        ),
        color,
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