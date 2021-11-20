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

package com.eziosoft.arucomqtt;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;

public class Other {

    static Point3d cameraLocation(Mat rvec, Mat tvec) {
        Mat R_ct = new Mat(3, 3, CvType.CV_32FC1);
        Calib3d.Rodrigues(rvec.row(0), R_ct);
        Mat R_tc = R_ct.t();
        Core.multiply(R_tc, new Scalar(-1), R_tc);
        Mat pos_camera =new Mat(3, 3, CvType.CV_32FC1);
//        Core.gemm(R_tc.inv(), tvec.t(), 1, new Mat(), 0, pos_camera, 0);
        Core.multiply(R_tc,tvec.t(),pos_camera);

        Log.d("aaaa", "cameraLocation: " + pos_camera.toString());
        final double x = pos_camera.get(0,0)[0];
//        final double y = pos_camera.get(1, 0)[0];
//        final double z = pos_camera.get(2, 0)[0];

        return new Point3d(0,0,0, "cam");
    }
}
