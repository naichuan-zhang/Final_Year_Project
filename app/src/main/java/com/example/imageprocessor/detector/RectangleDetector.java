package com.example.imageprocessor.detector;

import com.example.imageprocessor.misc.OpenCVUtil;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RectangleDetector {

    public String detect(MatOfPoint2f curve) {
        OpenCVUtil openCVUtil = new OpenCVUtil();
        String shape = null;
        double perimeter = Imgproc.arcLength(curve, true);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        // approx contour with accuracy proportional to the contour perimeter
        Imgproc.approxPolyDP(curve, approxCurve, 0.02 * perimeter, true);

        long total = approxCurve.total();
        if (total == 3) {
            shape = "triangle";

        } else if (total >= 4 && total <= 6) {
            Rect rect = Imgproc.boundingRect(new MatOfPoint(approxCurve.toArray()));
            List<Double> cos = new ArrayList<>();
            Point[] points = approxCurve.toArray();
            for (int i = 2; i < total + 1; i++) {
                cos.add(openCVUtil.getAngle(points[(int)(i % total)], points[i - 2], points[i - 1]));
            }
            Collections.sort(cos);
            Double min = cos.get(0);
            Double max = cos.get(cos.size() - 1);
            boolean isRect = total == 4 && min >= -0.1 && max <= 0.3;
            boolean isPolygon = (total == 5 && min >= -0.34 && max <= -0.27) || (total == 6 && min >= -0.55 && max <= -0.45);
            if (isRect) {
                double ratio = Math.abs(1 - (double) rect.width / rect.height);
                if (ratio <= 0.03)
                    shape = "square";
                else
                    shape = "rectangle";
            }
            if (isPolygon)
                shape = "polygon";
        } else {
            shape = null;
        }
        return shape;
    }
}
