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
import org.opencv.core.Mat
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
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_32FC1
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.cvtColor
import java.util.*
import org.opencv.core.Core

import org.opencv.core.Scalar

import org.opencv.core.MatOfDouble
import java.util.zip.DeflaterOutputStream


class MainActivity : AppCompatActivity(), CvCameraViewListener2 {
    private lateinit var binding: ActivityMainBinding

    private val DICTIONARY = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_100)
    private val CAMERA_WIDTH = 720
    private val CAMERA_HEIGH = 480

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

            var cam: Point3d? = null
            for (i in 0 until ids.rows()) { //for each marker
                val markerCorners = allCorners[i]
                val ID = ids[i, 0][0].toInt()

                val rvec =
                    Mat(3, 1, CvType.CV_64FC1) //attitude of the marker respect to camera frame
                val tvec = Mat(3, 1, CvType.CV_64FC1) //position of the marker in camera frame

                Aruco.estimatePoseSingleMarkers(
                    mutableListOf(markerCorners),
                    MARKER_LENGTH,
                    CAMERA_MATRIX,
                    CAMERA_DISTORTION,
                    rvec,
                    tvec
                )
                Aruco.drawAxis(rgb, CAMERA_MATRIX, CAMERA_DISTORTION, rvec, tvec, MARKER_LENGTH)


                val marker = Marker(
                    markerCorners,
                    ID,
                    X = tvec[0, 0][0],
                    Y = tvec[0, 0][1],
                    Z = tvec[0, 0][2]
                )



                marker.draw(rgb)
                markersList.add(marker)


                cam = Other.cameraLocation(rvec, tvec)

            }


            CoroutineScope(Dispatchers.Main).launch {
                showInfo(cam.toString())

            }
        }

        cvtColor(rgb, frame, Imgproc.COLOR_BGR2BGRA);





        return frame
    }


    private fun cameraLocation(rvec: Mat, tvec: Mat): Point3d {



        var R = Mat(3, 3, CV_32FC1)
        Calib3d.Rodrigues(rvec, R)
        val tvec_map_cam: Mat = MatOfDouble(1.0, 1.0, 1.0)
        R = R.t()
        val bankX = Math.atan2(-R[1, 2][0], R[1, 1][0])
        val headingY = Math.atan2(-R[2, 0][0], R[0, 0][0])
        val attitudeZ = Math.asin(R[1, 0][0])

        Core.multiply(R, Scalar(-1.0), R)
        Core.gemm(R, tvec, 1.0, Mat(), 0.0, tvec_map_cam, 0)


        val x = tvec_map_cam[0, 0][0]
        val y = tvec_map_cam[1, 0][0]
        val z = tvec_map_cam[2, 0][0]

        return Point3d(x,y,z,name="cam")


//        var m33 = Mat(3, 3, CV_32FC1)
//        Calib3d.Rodrigues(rvec, m33)
//
////        val tvec_map_cam: Mat = MatOfDouble(1.0, 1.0, 1.0)
//
//        var m44 = Mat(4, 4, CV_32FC1)
//
//        for (i in 0 until 3)
//            for (j in 0 until 3)
//                m44.put(i, j, m33[i, j][0])
//
//        for (i in 0 until 3)
//            m44.put(i, 3, tvec[0, 0][i])
//
//        m44.inv()
//        return Point3d(m44[0, 0][0], m44[0, 1][0], m44[0, 2][0], "cam")


//        self.retval, self.rvec, self.tvec = aruco.estimatePoseBoard(self.corners, self.ids, board, self.cameraMatrix, self.distanceCoefficients
//        self.dst, jacobian = cv2.Rodrigues(self.rvec)
//        self.extristics = np.matrix([[self.dst[0][0],self.dst[0][1],self.dst[0][2],self.tvec[0][0]],
//            [self.dst[1][0],self.dst[1][1],self.dst[1][2],self.tvec[1][0]],
//            [self.dst[2][0],self.dst[2][1],self.dst[2][2],self.tvec[2][0]],
//            [0.0, 0.0, 0.0, 1.0]
//        ])
//        self.extristics_I = self.extristics.I  # inverse matrix
//        self.worldPos = [self.extristics_I[0,3],self.extristics_I[1,3],self.extristics_I[2,3]]
//        But I st
//
//        var dst = Mat(3, 3, CV_32FC1)
//        Calib3d.Rodrigues(rvec,dst )
//        var extristics = Mat()

    }




//    cv::Point3f CameraParameters::getCameraLocation(cv::Mat Rvec,cv::Mat Tvec)
//    {
//        cv::Mat m33(3,3,CV_32FC1);
//        cv::Rodrigues(Rvec, m33)  ;
//
//        cv::Mat m44=cv::Mat::eye(4,4,CV_32FC1);
//        for (int i=0;i<3;i++)
//        for (int j=0;j<3;j++)
//        m44.at<float>(i,j)=m33.at<float>(i,j);
//
//        //now, add translation information
//        for (int i=0;i<3;i++)
//        m44.at<float>(i,3)=Tvec.at<float>(0,i);
//        //invert the matrix
//        m44.inv();
//        return  cv::Point3f( m44.at<float>(0,0),m44.at<float>(0,1),m44.at<float>(0,2));
//
//    }

    private suspend fun showInfo(extra: String) {
        var s = ""
        markersList.forEach {
            s += it.toString() + "\n"
        }

        s += "\n\n$extra"
        binding.textView.text = s
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


