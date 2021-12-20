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

package com.eziosoft.arucomqtt.repository.navigation

import com.eziosoft.arucomqtt.repository.vision.Marker
import org.opencv.core.Mat
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class Navigation {



    fun navigate(frame: Mat, robot:Marker, target: Marker)
    {
        val distance = robot.distanceTo(target)
        val headingToTarget = robot.headingTo(target)

    }



    companion object {
        const val WP_RADIUS = 10.0
    }
}

fun Marker.distanceTo(marker: Marker): Double =
    sqrt((this.x - marker.x).pow(2) + (this.y - marker.y).pow(2))

fun Marker.headingTo(marker: Marker) = atan2(marker.x - this.x, marker.y - this.y)
