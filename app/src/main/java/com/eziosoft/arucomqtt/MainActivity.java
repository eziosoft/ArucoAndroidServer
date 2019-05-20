package com.eziosoft.arucomqtt;


import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Mat;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "ARUCO";
    private CameraBridgeViewBase mOpenCvCameraView;

    private Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_ARUCO_ORIGINAL);
    private DetectorParameters detectorParameters = DetectorParameters.create();

    private Mat frame = new Mat();
    private Mat rgb = new Mat();
    private Mat gray = new Mat();
    private Mat ids = new Mat();
    private List<Mat> allCorners = new ArrayList<>();
    private List<Mat> rejected = new ArrayList<>();
    private List<Marker> markersList = new ArrayList<>();

    private Gson gson = new Gson();
    private String json = "{}";

    private Server serverSocket = new Server(new Server.EventListener() {
        @Override
        public void onEvent(String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "client connected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    });


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = findViewById(R.id.camera_view);
        mOpenCvCameraView.setMaxFrameSize(800, 600);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        final TextView tv = findViewById(R.id.textView);
        tv.setText(String.format("%s:%s", getIPAddress(true), String.valueOf(serverSocket.getServerSocketPort())));
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        serverSocket.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
        serverSocket.stop();
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frame = inputFrame.rgba();
        gray = inputFrame.gray();
        allCorners.clear();
        rejected.clear();
        markersList.clear();
        Aruco.detectMarkers(gray, dictionary, allCorners, ids, detectorParameters, rejected);

        if (!ids.empty()) {
//            cvtColor(frame, rgb, Imgproc.COLOR_BGRA2BGR);
//            Aruco.drawDetectedMarkers(rgb, allCorners, ids);
//            cvtColor(rgb, frame, Imgproc.COLOR_BGR2BGRA);

            for (int i = 0; i < ids.rows(); i++) {
                Mat markerCorners = allCorners.get(i);
                int ID = (int) ids.get(i, 0)[0];

                Marker marker = new Marker(markerCorners, ID);
                marker.draw(frame);
                markersList.add(marker);
            }

        }
        json = gson.toJson(markersList);
        serverSocket.setMessage("{\"aruco\":" + json + "}\n");
        return frame;
    }


    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }


    public void disableCamera() {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {
    }


}