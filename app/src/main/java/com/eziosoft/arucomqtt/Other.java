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

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;

public class Other {

    static Point3d cameraLocation(Mat rvec, Mat tvec) {
//        final QuaternionHelper q = new QuaternionHelper();

        Mat R = new Mat(3, 3, CvType.CV_32FC1);
        Calib3d.Rodrigues(rvec, R);
        // see publishers before for documentation
        final Mat tvec_map_cam = new MatOfDouble(1.0, 1.0, 1.0);
        R = R.t();
        final double bankX = Math.atan2(-R.get(1, 2)[0], R.get(1, 1)[0]);
        final double headingY = Math.atan2(-R.get(2, 0)[0], R.get(0, 0)[0]);
        final double attitudeZ = Math.asin(R.get(1, 0)[0]);
//        q.setFromEuler(bankX, headingY, attitudeZ);
        Core.multiply(R, new Scalar(-1), R);
        Core.gemm(R, tvec, 1, new Mat(), 0, tvec_map_cam, 0);
        R.release();
//        final org.ros.rosjava_geometry.Quaternion rotation = new org.ros.rosjava_geometry.Quaternion(
//                q.getX(), q.getY(), q.getZ(), q.getW());
        final double x = tvec_map_cam.get(0, 0)[0];
        final double y = tvec_map_cam.get(1, 0)[0];
        final double z = tvec_map_cam.get(2, 0)[0];

        return new Point3d(x, y, z, "cam");
    }
}
