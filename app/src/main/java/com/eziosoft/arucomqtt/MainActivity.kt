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

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.CameraBridgeViewBase
import org.opencv.aruco.Aruco
import org.opencv.aruco.DetectorParameters
import org.opencv.core.Mat
import com.google.gson.Gson
import android.widget.Toast
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import com.eziosoft.arucomqtt.MainActivity
import android.os.Bundle
import android.view.WindowManager
import com.eziosoft.arucomqtt.R
import android.view.SurfaceView
import android.widget.TextView
import org.opencv.android.OpenCVLoader
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.lang.Exception
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity(), CvCameraViewListener2 {
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private val dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_ARUCO_ORIGINAL)
    private val detectorParameters = DetectorParameters.create()
    private var frame = Mat()
    private val rgb = Mat()
    private var gray = Mat()
    private val ids = Mat()
    private val allCorners: MutableList<Mat> = ArrayList()
    private val rejected: MutableList<Mat> = ArrayList()
    private val markersList: MutableList<Marker> = ArrayList()

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            if (status == SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully")
                if (checkCameraPermission()) {
                    mOpenCvCameraView!!.enableView()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Camera permission is needed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                super.onManagerConnected(status)
            }
        }
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        mOpenCvCameraView = findViewById(R.id.camera_view)
        mOpenCvCameraView.setMaxFrameSize(800, 600)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
        val tv = findViewById<TextView>(R.id.textView)

    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    public override fun onPause() {
        super.onPause()
        disableCamera()
    }

    public override fun onDestroy() {
        super.onDestroy()
        disableCamera()
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        frame = inputFrame.rgba()
        gray = inputFrame.gray()
        allCorners.clear()
        rejected.clear()
        markersList.clear()
        Aruco.detectMarkers(gray, dictionary, allCorners, ids, detectorParameters, rejected)
        if (!ids.empty()) {
//            cvtColor(frame, rgb, Imgproc.COLOR_BGRA2BGR);
//            Aruco.drawDetectedMarkers(rgb, allCorners, ids);
//            cvtColor(rgb, frame, Imgproc.COLOR_BGR2BGRA);
            for (i in 0 until ids.rows()) {
                val markerCorners = allCorners[i]
                val ID = ids[i, 0][0].toInt()
                val marker = Marker(markerCorners, ID)
                marker.draw(frame)
                markersList.add(marker)
            }
        }
        return frame
    }


    private fun disableCamera() {
        mOpenCvCameraView.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}
    override fun onCameraViewStopped() {}
    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1234)
        } else {
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "ARUCO"
    }
}