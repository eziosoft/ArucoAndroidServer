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

data class Point3d(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
//        val bankX: Double = 0.0,
//        val headingY: Double = 0.0,
//        val attitudeZ: Double = 0.0,
    val name: String
) {
    override fun toString() =
        "$name-${x.round(2)} ${y.round(2)} ${z.round(2)}"

}