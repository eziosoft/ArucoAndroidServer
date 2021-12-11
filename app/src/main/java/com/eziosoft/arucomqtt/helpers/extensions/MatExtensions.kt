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

package com.eziosoft.arucomqtt.helpers.filters.extensions

import android.util.Log
import com.eziosoft.arucomqtt.helpers.extensions.round
import org.opencv.core.Mat

fun Mat.logMat(name: String) {
    Log.d("aaa", "$name:${this.type()} ${this.channels()} -> ${this.dump()}")
}


fun Mat.logMatTOArray(name: String) {
    val arr = mutableListOf<Double>()
    arr.add(this[0,0][0].round(2))
    arr.add(this[0,1][0].round(2))
    arr.add(this[0,2][0].round(2))
    arr.add(this[1,0][0].round(2))
    arr.add(this[1,1][0].round(2))
    arr.add(this[1,2][0].round(2))
    arr.add(this[2,0][0].round(2))
    arr.add(this[2,1][0].round(2))
    arr.add(this[2,2][0].round(2))

    Log.i("aaaa", "$name: ${arr.toString()}}")
}