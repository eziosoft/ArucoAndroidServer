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

import android.util.Log
import androidx.core.math.MathUtils
import com.eziosoft.arucomqtt.helpers.extensions.TWO_PI
import com.eziosoft.arucomqtt.helpers.extensions.normalizeAngle
import com.eziosoft.arucomqtt.helpers.extensions.toDegree
import com.eziosoft.arucomqtt.helpers.extensions.toRadian
import com.eziosoft.arucomqtt.repository.robotControl.RobotControl
import com.eziosoft.arucomqtt.repository.vision.Marker
import com.eziosoft.arucomqtt.repository.vision.Position3d
import com.eziosoft.arucomqtt.repository.vision.Rotation
import com.eziosoft.arucomqtt.repository.vision.camera.Camera
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.absoluteValue

@Singleton
class Navigation @Inject constructor(val robotControl: RobotControl) {

    private val pidStear =
        MiniPID(0.5, 0.00001, 0.0)
    private val pidSpeed =
        MiniPID(0.5, 0.0000, 0.0)

    private var target = Marker(
        position3d = Position3d(),
        rotation = Rotation(),
        matrices = null
    )

    fun setTarget(t: Marker) {
        target = t
    }

    fun getTarget() = target


    fun robotNavigation(
        robotLocation: Camera,
        targetReached: (Boolean) -> Unit
    ) {
        val currentHeading = robotLocation.rotation.z.normalizeAngle()
        val headingToTarget = robotLocation.headingTo(target)
        val distanceToTarget = robotLocation.distanceTo(target)
        val headingDifference = currentHeading - headingToTarget.normalizeAngle()

        var headingDifferenceCorrected = headingDifference
        if (headingDifferenceCorrected > PI) headingDifferenceCorrected -= TWO_PI
        if (headingDifference < -PI) headingDifferenceCorrected += TWO_PI

        Log.d(
            "aaa",
            "robotNavigation: heading = ${
                currentHeading.toDegree().toInt()
            }, headingTarget=${
                headingToTarget.toDegree().toInt()
            }, diff=${
                headingDifference.toDegree().toInt()
            }, diff corr= ${headingDifferenceCorrected.toDegree().toInt()}"
        )

        pidStear.setOutputLimits(-1.0, 1.0)
        val stearing = pidStear.getOutput(headingDifferenceCorrected, 0.0)
        var ch1: Int = (stearing * 100).toInt()
        ch1 = MathUtils.clamp(ch1, -20, 20)

        pidStear.setOutputLimits(-1.0, 1.0)
        var speed = pidSpeed.getOutput(distanceToTarget, 0.0)

        if (headingDifferenceCorrected.absoluteValue > 45.0.toRadian()) speed = 0.0
        var ch2: Int = -(speed * 100).toInt()
        ch2 = MathUtils.clamp(ch2, -20, 20)


        if (distanceToTarget < WP_RADIUS) {
            targetReached(true)
        } else {
            robotControl.sendChannels(ch1, ch2, 0, 0)
            targetReached(false)
        }
    }


    companion object {
        const val WP_RADIUS = 10.0
    }
}
