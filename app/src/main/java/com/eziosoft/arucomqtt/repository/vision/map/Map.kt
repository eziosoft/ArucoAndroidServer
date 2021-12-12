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
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
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

    fun addPoint(marker: Marker) {
        points.add(Point(marker.x, marker.y))
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
        const val JSON_MAP = "{\n" +
                "  \"points\": [\n" +
                "    {\n" +
                "      \"x\": 684.1716658974966,\n" +
                "      \"y\": -1574.5772643895893\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 1215.7413244479708,\n" +
                "      \"y\": 1594.8722189637338\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 644.409975196228,\n" +
                "      \"y\": 1814.2995197399825\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 547.0137087813526,\n" +
                "      \"y\": 1654.12241726796\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 331.3595237834618,\n" +
                "      \"y\": 1503.4708229012601\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -1342.3757419219378,\n" +
                "      \"y\": 1684.6064400570194\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -2202.217113848879,\n" +
                "      \"y\": 2187.273499589698\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -2661.5228234193155,\n" +
                "      \"y\": 1030.2692345608393\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -787.3030088238808,\n" +
                "      \"y\": 385.2037665952815\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -719.0368099572619,\n" +
                "      \"y\": -161.67254978819395\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -647.7468217757439,\n" +
                "      \"y\": -529.718662597305\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -399.8360510789418,\n" +
                "      \"y\": -825.5874134436923\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -528.1308462452289,\n" +
                "      \"y\": -1771.6925923090068\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -638.5100962167296,\n" +
                "      \"y\": -2945.350657677304\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 70.88668446072617,\n" +
                "      \"y\": -3592.116946055457\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 299.45939672723176,\n" +
                "      \"y\": -1610.311406766219\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 617.4919657572252,\n" +
                "      \"y\": -1570.6384924937781\n" +
                "    }\n" +
                "  ]\n" +
                "}"
    }
}
