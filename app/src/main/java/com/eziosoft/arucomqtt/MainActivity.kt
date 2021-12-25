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
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.eziosoft.arucomqtt.databinding.ActivityMainBinding
import com.eziosoft.arucomqtt.helpers.extensions.collectLatestLifecycleFLow
import com.eziosoft.arucomqtt.repository.Repository
import com.eziosoft.arucomqtt.repository.mqtt.BROKER_URL
import com.eziosoft.arucomqtt.repository.vision.Marker2
import com.eziosoft.arucomqtt.repository.vision.Matrices
import com.eziosoft.arucomqtt.repository.vision.Position3d
import com.eziosoft.arucomqtt.repository.vision.camera.calibration.CameraConfiguration.Companion.CAMERA_DISTORTION
import com.eziosoft.arucomqtt.repository.vision.camera.calibration.CameraConfiguration.Companion.CAMERA_FRONT
import com.eziosoft.arucomqtt.repository.vision.camera.calibration.CameraConfiguration.Companion.CAMERA_HEIGH
import com.eziosoft.arucomqtt.repository.vision.camera.calibration.CameraConfiguration.Companion.CAMERA_MATRIX
import com.eziosoft.arucomqtt.repository.vision.camera.calibration.CameraConfiguration.Companion.CAMERA_WIDTH
import com.eziosoft.arucomqtt.repository.vision.camera.calibration.CameraConfiguration.Companion.DICTIONARY
import com.eziosoft.arucomqtt.repository.vision.camera.calibration.CameraConfiguration.Companion.MARKER_LENGTH
import com.eziosoft.arucomqtt.repository.vision.helpers.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.aruco.Aruco
import org.opencv.aruco.DetectorParameters
import org.opencv.core.*
import org.opencv.core.CvType.CV_32FC1
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.cvtColor
import java.util.*
import javax.inject.Inject
import kotlin.math.*


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CvCameraViewListener2 {
    @Inject
    lateinit var repository: Repository

    private lateinit var binding: ActivityMainBinding

    private var captureCalibrationFrame = false
    private var calibrate = false

    private val detectorParameters = DetectorParameters.create()
    private var frame = Mat()
    private var rgb = Mat()

    private val ids = Mat()
    private val allCorners: MutableList<Mat> = ArrayList()
    private val rejected: MutableList<Mat> = ArrayList()
    private val markersList: MutableList<Marker2> = ArrayList()

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
            cameraView.setCameraIndex(CAMERA_FRONT)
            cameraView.setMaxFrameSize(CAMERA_WIDTH, CAMERA_HEIGH)
            cameraView.visibility = SurfaceView.VISIBLE
            cameraView.setCvCameraViewListener(this@MainActivity)
        }

        setupListeners()
        setUpCollectors()

        repository.connectToMQTT(BROKER_URL)
    }

    private fun setUpCollectors() {
        collectLatestLifecycleFLow(repository.connectionStatus) {
            repository.publishMap(true)
        }
    }

    private fun setupListeners() {
        binding.captureB.setOnClickListener {
            captureCalibrationFrame = true
        }

        binding.captureB.isVisible = false
        binding.calB.setOnCheckedChangeListener { _, checked ->
            binding.captureB.isVisible = checked
            calibrate = checked
        }

        binding.mapB.setOnClickListener {
            repository.map.addPoint(repository.cameraPosition.getLastCamera3Position())
            repository.publishMap(false)
        }

        binding.clearMapB.setOnClickListener {
            repository.map.clear()
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

    @Suppress("LongMethod")
    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        if (calibrate) {
            frame = inputFrame.rgba()
            val gray = inputFrame.gray()
            repository.cameraCalibrator.processFrame(gray, frame)

            if (captureCalibrationFrame) {
                captureCalibrationFrame = false
                repository.cameraCalibrator.addCorners()
                val calSummary = repository.cameraCalibrator.calibrate()
                log(calSummary)
            }
            return frame
        } else {
            frame = inputFrame.rgba()
            cvtColor(frame, rgb, Imgproc.COLOR_BGRA2BGR) // Convert to BGR


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

            drawPath(rgb)

            if (!ids.empty()) {
                for (i in 0 until ids.rows()) { // for each marker
                    val markerCorners = allCorners[i]
                    val id = ids[i, 0][0].toInt()
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

                    val marker = Marker2(
                        id = id,
                        Position3d(
                            x = tvec[0, 0][0],
                            y = tvec[0, 0][1],
                            z = tvec[0, 0][2]
                        ), null,
                        Matrices(
                            corners = markerCorners,
                            rvec = rvec,
                            tvec = tvec
                        )
                    )

                    Aruco.drawAxis(rgb, CAMERA_MATRIX, CAMERA_DISTORTION, rvec, tvec, MARKER_LENGTH)
                    markersList.add(marker)
                }
            }

            repository.map.draw(rgb, COLOR_RED)
            processMarkers(rgb)
            drawCenterLines(rgb)

            cvtColor(rgb, frame, Imgproc.COLOR_BGR2BGRA) //back to BGRA
            showInfo()
        }

        return if (binding.tryCalB.isChecked) {
            val mat = Mat()
            Imgproc.undistort(
                rgb,
                mat,
                repository.cameraCalibrator.cameraMatrix,
                repository.cameraCalibrator.distortionCoefficients
            )
            mat
        } else {
            frame
        }
    }

    private fun drawAllMarkers() {
        markersList.map {
            it.draw(rgb)
        }
    }

    private fun processMarkers(frame: Mat) {
        var cam3 = Marker()

        markersList.filter { it.id == 0 }.map { filteredMarker ->// draw path of marker 0
            filteredMarker.draw(frame)

            val cam1 = repository.cameraPosition.calculateCameraPosition1(filteredMarker)
            markersList.add(cam1)
            drawRobot(
                rgb,
                cam1,
                COLOR_RED
            )

            val cam2 = repository.cameraPosition.calculateCameraPosition2(filteredMarker)
            markersList.add(cam2)
            drawRobot(
                rgb,
                cam2,
                COLOR_PINK
            )

            cam3 = repository.cameraPosition.calculateCameraPosition3(filteredMarker, cam2)
            markersList.add(cam3)
            drawRobot(
                rgb,
                cam3,
                COLOR_GREEN
            )

            cam3.addToPath(rgb)
            publishCameraLocation(cam3)
        }


        val headingToTarget = cam3.headingTo(Marker())
        drawRobot(
            rgb,
            cam3,
            COLOR_GREEN,
            headingToTarget.invertAngleRadians()
        )


    }

    private fun publishCameraLocation(cam: Marker) {
        repository.publishCameraLocation(cam)
    }

    private fun log(str: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.textView.text = str
        }
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

    override fun onCameraViewStarted(width: Int, height: Int) = Unit
    override fun onCameraViewStopped() = Unit
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
        private const val TAG = "aaa"
    }
}
