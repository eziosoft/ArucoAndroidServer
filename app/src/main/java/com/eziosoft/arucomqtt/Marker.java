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


import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import static org.opencv.imgproc.Imgproc.*;

class Marker {
    private Point center;
    private double heading;
    private int ID;
    private int size;
    private Point[] markerCorners = new Point[4];

    transient private final Scalar c1 = new Scalar(255, 100, 0);
    transient private final Scalar c2 = new Scalar(255, 0, 255);

    Marker(Mat corners, int ID) {
        this.ID = ID;
        center = getMarkerCenter(corners);
        heading = getMarkerHeading(corners);
        for (int i = 0; i < 4; i++) {
            this.markerCorners[i] = new Point(corners.get(0, i)[0], corners.get(0, i)[1]);
        }
        this.size = (int) Math.sqrt(Math.pow(markerCorners[0].x - markerCorners[1].x, 2) + Math.pow(markerCorners[0].y - markerCorners[1].y, 2));
    }

    private double getMarkerHeading(Mat corners) {
        Point up = getMarkerUp(corners);
        return Math.atan2((up.x - center.x), (up.y - center.y));
    }

    private Point getMarkerCenter(Mat corners) {
        double[] c = corners.get(0, 0);
        double x = 0;
        double y = 0;
        for (int i = 0; i < 4; i++) {
            x += corners.get(0, i)[0];
            y += corners.get(0, i)[1];
        }
        return new Point(x / 4, y / 4);
    }

    private Point getMarkerUp(Mat corners) {
        double[] c = corners.get(0, 0);
        double x = 0;
        double y = 0;
        for (int i = 0; i < 2; i++) {
            x += corners.get(0, i)[0];
            y += corners.get(0, i)[1];
        }
        return new Point(x / 2, y / 2);
    }


    void draw(Mat frame) {
        line(frame, markerCorners[0], markerCorners[1], c1, 3);
        line(frame, markerCorners[1], markerCorners[2], c1, 3);
        line(frame, markerCorners[2], markerCorners[3], c1, 3);
        line(frame, markerCorners[3], markerCorners[0], c1, 3);
        line(frame, center, new Point(center.x + size / 2f * Math.sin(heading), center.y + size / 2f * Math.cos(heading)), c2, 5);
        putText(frame, String.valueOf(ID), center, 1, 1, c1);
    }
}
