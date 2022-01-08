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


package com.eziosoft.arucomqtt.repository.vision.helpers

import com.eziosoft.arucomqtt.helpers.extensions.invertAngleRadians
import com.eziosoft.arucomqtt.repository.vision.Marker
import com.eziosoft.arucomqtt.repository.vision.camera.Camera
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.sin

const val SCALE_TO_DRAW = 10
private val path = mutableListOf<Point>() //to draw a path

fun Camera.addToPath(frame: Mat) {
    val offsetX: Double = frame.width() / 2.0
    val offsetY: Double = frame.height() / 2.0
    path.add(
        Point(
            this.position3d.x / SCALE_TO_DRAW + offsetX,
            this.position3d.y / SCALE_TO_DRAW + offsetY
        )
    )
}

const val PATH_LENGTH = 500
fun drawPath(frame: Mat) {
    if (path.isNotEmpty()) {
        val matOfPoint = MatOfPoint()
        matOfPoint.fromList(path)
        val matOfPointList = arrayListOf(matOfPoint)

        Imgproc.polylines(frame, matOfPointList, false, COLOR_DARK_GREEN, 2)
        if (path.size > PATH_LENGTH) {
            path.removeAt(0)
        }
    }
}


@Suppress("MagicNumber")
fun drawTarget(frame: Mat, marker: Marker, color: Scalar) {
    val offsetX: Double = frame.width() / 2.0
    val offsetY: Double = frame.height() / 2.0

    val p = Point(
        marker.position3d.x / SCALE_TO_DRAW + offsetX,
        marker.position3d.y / SCALE_TO_DRAW + offsetY
    )
    Imgproc.circle(frame, p, 10, color, 2)
}

@Suppress("MagicNumber")
fun drawCameraPosition(frame: Mat, camera: Camera, color: Scalar, headingToTarget: Double? = null) {
    val offsetX: Double = frame.width() / 2.0
    val offsetY: Double = frame.height() / 2.0

    val p = Point(
        camera.position3d.x / SCALE_TO_DRAW + offsetX,
        camera.position3d.y / SCALE_TO_DRAW + offsetY
    )
    Imgproc.circle(frame, p, 10, color, 2)

    if (headingToTarget != null) {
        Imgproc.line(
            frame,
            p,
            Point(
                p.x + 80 * sin(headingToTarget),
                p.y + 80 * cos(headingToTarget)
            ),
            COLOR_WHITE,
            2
        )
    }

    Imgproc.arrowedLine(
        frame,
        p,
        Point(
            p.x + 50 * sin(camera.rotation.z),
            p.y + 50 * cos(camera.rotation.z)
        ),
        color,
        3
    )

    Imgproc.putText(frame, camera.id.toString(), p, 1, 2.0, COLOR_WHITE)
}

// TODO NOT WORKING
fun drawMarkerOnMap(frame: Mat, marker: Marker, color: Scalar) {
    val offsetX: Double = frame.width() / 2.0
    val offsetY: Double = frame.height() / 2.0

    val p = Point(
        marker.rotation.x / SCALE_TO_DRAW + offsetX,
        marker.rotation.y / SCALE_TO_DRAW + offsetY
    )
    Imgproc.rectangle(
        frame,
        Point(p.x - 10, p.y - 10),
        Point(p.x + 10, p.y + 10),
        color,
        1
    )
    Imgproc.line(
        frame,
        p,
        Point(
            p.x + 10 * sin(-marker.rotation.z.invertAngleRadians()),
            p.y + 10 * cos(-marker.rotation.z.invertAngleRadians())
        ),
        color,
        2
    )
    Imgproc.putText(frame, marker.id.toString(), p, 1, 2.0, color)
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
