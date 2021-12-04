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


class LowPassFilter {
    private var oldX = 0.0

    fun add(x: Double, alpha: Double = 0.7): Double {
        oldX = alpha * oldX + (1.0 - alpha) * x
        return oldX
    }
}

class MovingAverage(private val size: Int) {
    private var total = 0.0
    private var index = 0
    private val samples: DoubleArray = DoubleArray(size)
    fun add(x: Double): Double {
        total -= samples[index]
        samples[index] = x
        total += x
        if (++index == size) index = 0 // cheaper than modulus

        return average
    }

    val average: Double
        get() = total / size

    init {
        for (i in 0 until size) samples[i] = 0.0
    }
}


