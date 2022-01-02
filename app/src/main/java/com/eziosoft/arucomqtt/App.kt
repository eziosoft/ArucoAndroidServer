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

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader
import com.microsoft.appcenter.crashes.Crashes

import com.microsoft.appcenter.analytics.Analytics

import dagger.hilt.android.internal.Contexts.getApplication

import com.microsoft.appcenter.AppCenter
import dagger.hilt.android.internal.Contexts


@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        //load opencv
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCv", "Unable to load OpenCV")
        } else {
            Log.d(
                "OpenCv",
                "OpenCV loaded"
            )
        }

        AppCenter.start(
            this, BuildConfig.APPCENTER_APP_SECRET,
            Analytics::class.java, Crashes::class.java
        )
     }
}