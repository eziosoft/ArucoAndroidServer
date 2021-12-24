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

package com.eziosoft.arucomqtt.helpers.extensions

import kotlin.math.PI


const val TWO_PI = 2 * PI
const val PI_2 = PI / 2

fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()

@Suppress("MagicNumber")
fun Double.toRadian(): Double = this / 180 * Math.PI

@Suppress("MagicNumber")
fun Double.toDegree(): Double = this * 180.0 / Math.PI
fun Float.toDegree(): Double = this * 180.0 / Math.PI
fun Double.invertAngleRadians() = (this + Math.PI) % TWO_PI
fun Double.mirrorAngleRadians() = (TWO_PI - this) % TWO_PI
fun Double.addAngleRadians(angleRadians: Double) = (this + angleRadians) % TWO_PI

fun Double.normalizeAngle(): Double {
    var normalized = this % TWO_PI
    normalized = (normalized + TWO_PI) % TWO_PI
    return if (normalized <= Math.PI) normalized else normalized - TWO_PI
}
