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
        const val JSON_MAP = "{\"points\":[{\"x\":130.38136140758348,\"y\":-2429.4349097191325},{\"x\":155.8137144417773,\"y\":-1699.7591618381841},{\"x\":660.182095609415,\"y\":-1809.7197920823291},{\"x\":862.1366650446619,\"y\":-149.9216709939696},{\"x\":1096.0427533479565,\"y\":1655.8203413221563},{\"x\":330.9467785871092,\"y\":1912.492670410523},{\"x\":224.66278204428983,\"y\":1590.5045137148431},{\"x\":103.92286933420988,\"y\":1323.8195410504536},{\"x\":-1619.8333627704326,\"y\":1547.151547986858},{\"x\":-1902.3208023949041,\"y\":1946.8094798765972},{\"x\":-2314.168517309196,\"y\":2023.3716545851985},{\"x\":-2682.559206502032,\"y\":1154.315695147873},{\"x\":-1086.1007327198236,\"y\":518.4527837130023},{\"x\":-1015.9292327058522,\"y\":-11.06182941430356},{\"x\":-1062.9505827850392,\"y\":-104.18011622060166},{\"x\":-2392.7999666977494,\"y\":-88.43228877981103},{\"x\":-2503.1173391253556,\"y\":-457.59785636621984},{\"x\":-848.4542599168817,\"y\":-752.3577691578491},{\"x\":-614.9488039061207,\"y\":-773.5995810952087},{\"x\":-611.9285193715192,\"y\":-780.6440572988179},{\"x\":-428.1892257225678,\"y\":-1588.69352923896},{\"x\":-411.42424125636444,\"y\":-1918.6794746643998},{\"x\":-594.9349875268072,\"y\":-2272.2845985084778}]}\n"
    }
}
