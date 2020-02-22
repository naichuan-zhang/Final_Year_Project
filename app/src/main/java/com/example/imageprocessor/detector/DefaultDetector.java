package com.example.imageprocessor.detector;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class DefaultDetector {

    public String detect(MatOfPoint2f curve) {
        String shape;
        // calc perimeter of contour
        double perimeter = Imgproc.arcLength(curve, true);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        // get an approx contour
        Imgproc.approxPolyDP(curve, approxCurve, 0.01 * perimeter, true);
        if (approxCurve.toList().size() == 3) {
            shape = "triangle";

        } else if (approxCurve.toList().size() == 4) {
            Rect rect = Imgproc.boundingRect(new MatOfPoint(approxCurve.toArray()));
            float ratio = rect.width / (float)rect.height;
            if (ratio >= 0.9 && ratio <= 1.1)
                shape = "square";
            else
                shape = "rectangle";

        } else if (approxCurve.toList().size() == 5) {
            shape = "pentagon";

        } else if (approxCurve.toList().size() >= 6 && approxCurve.toList().size() <= 15){
            shape = "polygon";
        } else {
            shape = "circle";
        }
        return shape;
    }
}
