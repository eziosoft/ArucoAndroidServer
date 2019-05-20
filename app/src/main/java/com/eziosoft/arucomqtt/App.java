package com.eziosoft.arucomqtt;

import android.app.Application;
import android.util.Log;
import org.opencv.android.OpenCVLoader;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //load opencv
        if (!OpenCVLoader.initDebug())
            Log.e("OpenCv", "Unable to load OpenCV");
        else
            Log.d("OpenCv", "OpenCV loaded");
    }
}
