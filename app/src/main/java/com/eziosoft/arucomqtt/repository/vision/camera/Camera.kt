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

package com.eziosoft.arucomqtt.repository.vision.camera

import com.eziosoft.arucomqtt.helpers.extensions.normalizeAngle
import com.eziosoft.arucomqtt.repository.vision.Marker2
import com.eziosoft.arucomqtt.repository.vision.Position3d
import com.eziosoft.arucomqtt.repository.vision.Rotation
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class Camera(
    val id: Int = -1,
    val position3d: Position3d,
    val rotation: Rotation
) {

    fun distanceTo(marker: Marker2): Double {
        return sqrt(
            (position3d.x - marker.position3d.x).pow(2)
                    + (position3d.y - marker.position3d.y).pow(2)
        )
    }

    fun distanceTo(camera: Camera): Double {
        return sqrt(
            (position3d.x - camera.position3d.x).pow(2)
                    + (position3d.y - camera.position3d.y).pow(2)
        )
    }

    fun headingTo(marker: Marker2) =
        atan2(
            position3d.x - marker.position3d.x,
            position3d.y - marker.position3d.y
        ).normalizeAngle()
}