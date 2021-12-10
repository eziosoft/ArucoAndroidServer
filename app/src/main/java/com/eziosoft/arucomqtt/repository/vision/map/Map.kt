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

package com.eziosoft.arucomqtt.repository.vision.map

import com.eziosoft.arucomqtt.repository.vision.helpers.COLOR_GREEN
import com.eziosoft.arucomqtt.repository.vision.helpers.SCALE_TO_DRAW
import com.eziosoft.arucomqtt.repository.vision.Marker
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class Map {
    private val points = mutableListOf<Point>()

    fun addPoint(marker: Marker) {
        points.add(Point(marker.x, marker.y))
    }

    fun clear() {
        points.clear()
    }

    fun draw(frame: Mat) {
        if (points.isNotEmpty()) {
            val points = points.map {
                val offsetX: Double = frame.width() / 2.0
                val offsetY: Double = frame.height() / 2.0
                Point(
                    it.x / SCALE_TO_DRAW + offsetX,
                    it.y / SCALE_TO_DRAW + offsetY
                )
            }

            val matOfPoint = MatOfPoint()
            matOfPoint.fromList(points)
            val matOfPointList = arrayListOf(matOfPoint)

            Imgproc.polylines(frame, matOfPointList, false, COLOR_GREEN, 2)
        }
    }
}
