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

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


data class Polar(val r: Double, var theta: Double) {
    fun rotate(angleRadians: Double) {
        theta += angleRadians
    }

    fun toCartesian(): Cartesian =
        Cartesian(r * cos(theta), r * sin(theta))
}

data class Cartesian(val x: Double, val y: Double) {
    fun toPolar(): Polar {
        val r = sqrt(x * x + y * y)
        val theta = atan2(y, x)
        return Polar(r, theta)
    }
}
