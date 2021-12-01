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
import org.opencv.aruco.Aruco
import org.opencv.aruco.DetectorParameters
import android.widget.Toast
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import android.os.Bundle
import android.view.WindowManager
import android.view.SurfaceView
import org.opencv.android.OpenCVLoader
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.eziosoft.arucomqtt.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.*
import org.opencv.core.CvType.CV_32FC1
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.cvtColor

import java.util.*
import kotlin.math.*


class MainActivity : AppCompatActivity(), CvCameraViewListener2 {
    private lateinit var binding: ActivityMainBinding

    private val path = mutableListOf<Point>() //to draw a path

    private val DICTIONARY = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_100)
    private val CAMERA_WIDTH = 720
    private val CAMERA_HEIGH = 480

    private val MARKER_LENGTH = 10F
    private val CAMERA_MATRIX: Mat = Mat(3, 3, CvType.CV_32F)
    private val CAMERA_DISTORTION: Mat = Mat(1, 5, CvType.CV_32F)

    var a = 0.0

    init {
//        [467.74270306499267, 0.0, 320.5,
//        0.0, 467.74270306499267, 240.5,
//        0.0, 0.0, 1.0]
        CAMERA_MATRIX.put(0, 0, 467.74270306499267)
        CAMERA_MATRIX.put(0, 1, 0.0)
        CAMERA_MATRIX.put(0, 2, 320.5)

        CAMERA_MATRIX.put(1, 0, 0.0)
        CAMERA_MATRIX.put(1, 1, 467.74270306499267)
        CAMERA_MATRIX.put(1, 2, 240.5)

        CAMERA_MATRIX.put(2, 0, 0.0)
        CAMERA_MATRIX.put(2, 1, 0.0)
        CAMERA_MATRIX.put(2, 2, 1.0)

        CAMERA_DISTORTION.put(0, 0, 0.0)
        CAMERA_DISTORTION.put(0, 1, 0.0)
        CAMERA_DISTORTION.put(0, 2, 0.0)
        CAMERA_DISTORTION.put(0, 3, 0.0)
        CAMERA_DISTORTION.put(0, 4, 0.0)
    }


    private val detectorParameters = DetectorParameters.create()
    private var frame = Mat()
    private var rgb = Mat()

    private val ids = Mat()
    private val allCorners: MutableList<Mat> = ArrayList()
    private val rejected: MutableList<Mat> = ArrayList()
    private val markersList: MutableList<Marker> = ArrayList()

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            if (status == SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully")
                if (checkCameraPermission()) {
                    binding.cameraView.enableView()
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            cameraView.setMaxFrameSize(CAMERA_WIDTH, CAMERA_HEIGH)
            cameraView.visibility = SurfaceView.VISIBLE
            cameraView.setCvCameraViewListener(this@MainActivity)
        }
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


    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        frame = inputFrame.rgba()
        cvtColor(frame, rgb, Imgproc.COLOR_BGRA2BGR) // Convert to BGR

//        Core.flip(frame, frame, 1) // flip front camera
//        Core.flip(gray, gray, 1) // flip front camera

        allCorners.clear()
        rejected.clear()
        markersList.clear()

        Aruco.detectMarkers(
            rgb,
            DICTIONARY,
            allCorners,
            ids,
            detectorParameters,
            rejected,
            CAMERA_MATRIX,
            CAMERA_DISTORTION
        )

        if (!ids.empty()) {
            for (i in 0 until ids.rows()) { // for each marker
                val markerCorners = allCorners[i]
                val ID = ids[i, 0][0].toInt()

                val rvec = Mat(3, 1, CV_32FC1) //attitude of the marker respect to camera frame
                val tvec = Mat(3, 1, CV_32FC1) //position of the marker in camera frame

                Aruco.estimatePoseSingleMarkers(
                    mutableListOf(markerCorners),
                    MARKER_LENGTH,
                    CAMERA_MATRIX,
                    CAMERA_DISTORTION,
                    rvec,
                    tvec
                )

                val marker = Marker(
                    markerCorners,
                    ID,
                    X = tvec[0, 0][0],
                    Y = tvec[0, 0][1],
                    Z = tvec[0, 0][2]
                )

//                Aruco.drawAxis(rgb, CAMERA_MATRIX, CAMERA_DISTORTION, rvec, tvec, MARKER_LENGTH)
                markersList.add(marker)
            }


        }


        calculateCameraPosition(rgb)
        drawPath(rgb)
        drawCenterLines(rgb)
        showInfo()

        cvtColor(rgb, frame, Imgproc.COLOR_BGR2BGRA) //back to BGRA
        return frame
    }


    private fun calculateCameraPosition(frame: Mat) {
        markersList.filter { it.ID == 0 }.map { filteredMarker ->// draw path of marker 0
            val x = 2.0 * filteredMarker.X
            val y = 2.0 * filteredMarker.Y
            a += 1

            var c = Cartesian(x, y)
            val p = c.toPolar()
            p.rotate(filteredMarker.heading)
            c = p.toCartesian()

            val cam = Marker(null, 255, c.x, c.y, -filteredMarker.Z)
            cam.heading = 2 * PI - filteredMarker.heading.addAngleRadians(PI/2)

            drawRobot(
                frame,
                cam.getCenterInWorld(frame.width() / 2, frame.height() / 2),
                cam.heading
            )

            path.add(cam.getCenterInWorld(frame.width() / 2, frame.height() / 2))

            filteredMarker.draw(rgb)
            markersList.add(cam)
        }
    }

    private fun drawPath(frame: Mat) {
        if (path.isNotEmpty()) {
            val matOfPoint = MatOfPoint()
            matOfPoint.fromList(path)
            val matOfPointList = arrayListOf(matOfPoint)

            Imgproc.polylines(frame, matOfPointList, false, COLOR_RED, 2)
            if (path.size > 5000) {
                path.removeAt(0)
            }
        }
    }

    private fun drawRobot(frame: Mat, point: Point, heading: Double) {

        Imgproc.circle(frame, point, 10, COLOR_GREEN, 2)
        Imgproc.line(
            frame,
            point,
            Point(point.x + 50 * sin(heading), point.y + 50 * cos(heading)),
            COLOR_GREEN,
            2
        )
    }

    private fun drawCenterLines(frame: Mat) {
        Imgproc.line(
            frame,
            Point(frame.width() / 2.0, 0.0),
            Point(frame.width() / 2.0, frame.height().toDouble()),
            COLOR_PINK,
            1
        )
        Imgproc.line(
            frame,
            Point(0.0, frame.height() / 2.0),
            Point(frame.width().toDouble(), frame.height() / 2.0),
            COLOR_PINK,
            1
        )
    }


    private fun showInfo(extra: String = "") {
        CoroutineScope(Dispatchers.Main).launch {
            var s = ""
            markersList.forEach {
                s += it.toString() + "\n"
            }

            s += "\n\n$extra"
            binding.textView.text = s
        }
    }


    private fun disableCamera() {
        binding.cameraView.disableView()
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


    public override fun onPause() {
        super.onPause()
        disableCamera()
    }

    public override fun onDestroy() {
        super.onDestroy()
        disableCamera()
    }

    companion object {
        private const val TAG = "ARUCO"

        @Transient
        val COLOR_PINK = Scalar(255.0, 0.0, 255.0)

        @Transient
        val COLOR_RED = Scalar(255.0, 0.0, 0.0)

        @Transient
        val COLOR_GREEN = Scalar(0.0, 255.0, 0.0)
    }
}


