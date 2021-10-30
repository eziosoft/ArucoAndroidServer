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
import android.widget.Toast
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import android.os.Bundle
import android.view.WindowManager
import android.view.SurfaceView
import android.widget.TextView
import org.opencv.android.OpenCVLoader
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.eziosoft.arucomqtt.databinding.ActivityMainBinding
import org.opencv.core.CvType
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.cvtColor
import java.util.*

class MainActivity : AppCompatActivity(), CvCameraViewListener2 {
    private lateinit var binding: ActivityMainBinding

    private val DICTIONARY = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_100)
    private val CAMERA_WIDTH = 800
    private val CAMERA_HEIGH = 600

    private val MARKER_LENGTH = 10F
    private val CAMERA_MATRIX: Mat = Mat(3, 3, CvType.CV_32F)
    private val CAMERA_DISTORTION: Mat = Mat(1, 5, CvType.CV_32F)

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
        cvtColor(frame, rgb, Imgproc.COLOR_BGRA2BGR);

        allCorners.clear()
        rejected.clear()
        markersList.clear()


        Aruco.detectMarkers(
            gray,
            DICTIONARY,
            allCorners,
            ids,
            detectorParameters,
            rejected,
            CAMERA_MATRIX,
            CAMERA_DISTORTION
        )

        if (!ids.empty()) {
//            cvtColor(frame, rgb, Imgproc.COLOR_BGRA2BGR);
//            Aruco.drawDetectedMarkers(rgb, allCorners, ids);
//            cvtColor(rgb, frame, Imgproc.COLOR_BGR2BGRA);
            for (i in 0 until ids.rows()) {
                val markerCorners = allCorners[i]
                val ID = ids[i, 0][0].toInt()
                val marker = Marker(markerCorners, ID)
                marker.draw(rgb)
                markersList.add(marker)

            }


            markersList.filter { it.ID == 0 }.forEach {
                var rvec = Mat()
                var tvecs = Mat()

                Aruco.estimatePoseSingleMarkers(
                    mutableListOf(it.corners),
                    MARKER_LENGTH,
                    CAMERA_MATRIX,
                    CAMERA_DISTORTION,
                    rvec,
                    tvecs
                )
                Aruco.drawAxis(rgb, CAMERA_MATRIX, CAMERA_DISTORTION, rvec, tvecs, MARKER_LENGTH)
            }
        }

        cvtColor(rgb, frame, Imgproc.COLOR_BGR2BGRA);
        return frame
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

    companion object {
        private const val TAG = "ARUCO"
    }
}