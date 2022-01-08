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

package com.eziosoft.arucomqtt.repository.map

import com.eziosoft.arucomqtt.repository.vision.camera.Camera
import com.eziosoft.arucomqtt.repository.vision.helpers.SCALE_TO_DRAW
import com.google.gson.Gson
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Map @Inject constructor(gson: Gson) {
    private var points = mutableListOf<Point>()

    init {
        points = gson.fromJson(JSON_MAP, Map::class.java).points
    }

    fun addPoint(camera: Camera) {
        points.add(Point(camera.position3d.x, camera.position3d.y))
    }

    fun clear() {
        points.clear()
    }

    fun draw(frame: Mat, color: Scalar) {
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

            Imgproc.polylines(frame, matOfPointList, false, color, 3)
        }
    }

    companion object {
        const val JSON_MAP =
            "{\"points\":[{\"x\":414.2613885185448,\"y\":-2508.4230075420232},{\"x\":408.9950785841881,\"y\":-1786.6212285817105},{\"x\":685.3720415911254,\"y\":-1764.416516464842},{\"x\":971.0198244909537,\"y\":1613.7940782341998},{\"x\":274.7204314119417,\"y\":1938.3050378050727},{\"x\":264.8659278984802,\"y\":1786.7908771903003},{\"x\":115.01850973264713,\"y\":1609.8713950938961},{\"x\":-1630.781625734832,\"y\":1622.7813596340352},{\"x\":-2831.1059218778137,\"y\":1629.6475644483758},{\"x\":-3068.0166563828966,\"y\":698.7532669564218},{\"x\":-1370.5329317920155,\"y\":402.61862886912945},{\"x\":-1044.4626546667746,\"y\":-369.14244265266086},{\"x\":-1263.5956832768393,\"y\":-693.5868906646695},{\"x\":-696.458303996175,\"y\":-785.6736054488522},{\"x\":-365.6398650901332,\"y\":-857.1207842339554},{\"x\":-292.30276435244787,\"y\":-1984.521193459193},{\"x\":-434.13454546122705,\"y\":-3380.0141192199785}]}"
    }
}
