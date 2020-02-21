package com.example.imageprocessor.misc;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OpenCVUtil {

    private final static String TAG = "OpenCVUtil: ";

    public void toGray(Mat from, Mat to) {
        Imgproc.cvtColor(from, to, Imgproc.COLOR_BGR2GRAY);
        Log.i(TAG, "toGray");
    }

    public void reverseColor(Mat mat) {
        Core.bitwise_not(mat, mat);
        Log.i(TAG, "reverseColor");
    }

    public void dilate(Mat mat, double width, double height, int iterations) {
        Imgproc.dilate(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(width, height)), new Point(-1, -1), iterations);
        Log.i(TAG, "dilate");
    }

    public Mat bitmapToMat(Bitmap bitmap) {
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        Mat mat = new Mat(copy.getHeight(), copy.getWidth(), CvType.CV_8UC(3));
        Utils.bitmapToMat(copy, mat);
        Log.i(TAG, "bitmapToMat");
        return mat;
    }

    public Bitmap matToBitmap(Mat mat, Bitmap srcBitmap) {
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(),
                srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public Mat cloneMat(Mat mat) {
        return mat.clone();
    }

    public Mat createMat(int v0, int v1, int v2) {
        return new Mat(360, 640, CvType.CV_8UC3, new Scalar(v0, v1, v2));
    }

    public void gaussianBlur(Mat src, double width, double height, double sigmaX) {
        Imgproc.GaussianBlur(src, src, new Size(width, height), sigmaX);
        Log.i(TAG, "gaussianBlur");
    }

    public Point getCenterPoint(Point[] points) {
        double x = 0.0;
        double y = 0.0;
        double len = points.length;
        for (Point point : points) {
            x += point.x;
            y += point.y;
        }
        Log.i(TAG, "getCenterPoint");
        return new Point((int)(x / len), (int)(y / len));
    }

    public List<MatOfPoint> getContours(Mat mat) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy,
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.i(TAG, "getContours" + contours);
        return contours;
    }

    public void drawContours(Mat image, List<MatOfPoint> contours, int contourIdx, int v0, int v1, int v2, int thickness) {
        Imgproc.drawContours(image, contours, contourIdx, new Scalar(v0, v1, v2, 0), thickness);
        Log.i(TAG, "DrawContours #" + contourIdx);
    }

    public Point[] getCorners(Mat image) {
        final int maxCorners = 50, blockSize = 3;
        final double qualityLevel = 0.01, minDistance = 20.0, k = 0.04;
        final boolean userHarrisDetector = false;
        MatOfPoint corners = new MatOfPoint();
        this.toGray(image, image);
        Imgproc.goodFeaturesToTrack(image, corners, maxCorners,
                qualityLevel, minDistance, new Mat(),
                blockSize, userHarrisDetector, k);
        Log.i(TAG, "getCorners" + corners.toString());
        return corners.toArray();
    }

    public String getShape(Point[] points) {
        int numPoints = points.length;
        if (numPoints < 3) return "ERROR";
        else if (numPoints == 3) return "TRIANGLE";
        else if (numPoints == 5) return "PENTAGON";
        else if (numPoints > 5) return "CIRCLE";
        else {
            // quadrilateral
            double dist1 = getDist(points[0], points[1]);
            double dist2 = getDist(points[0], points[2]);
            double dist3 = getDist(points[0], points[3]);
            Point[] point = new Point[2];
            // get rid of the point with largest dist to the first point
            if (dist1 > dist2 && dist1 > dist3) {
                point[0] = points[2];
                point[1] = points[3];
            } else if (dist2 > dist1 && dist2 > dist3) {
                point[0] = points[1];
                point[1] = points[3];
            } else {
                point[0] = points[1];
                point[1] = points[2];
            }
            double angle1 = getAngle(points[0], point[0]);
            double angle2 = getAngle(points[0], point[1]);
            double angle = Math.abs(angle1 - angle2);
            if (angle > 80 && angle < 100) {
                double d1 = getDist(points[0], point[0]);
                double d2 = getDist(points[0], point[1]);
                double ratio = d1 / d2;
                if (ratio > 0.80 && ratio < 1.20) return "SQUARE";
                else return "RECTANGLE";
            } else return "DIAMOND";
        }
    }

    public void drawCircleByCenter(Mat image, Point center) {
        final int radius = 2;
        final Scalar color = new Scalar(255, 255, 0);
        Imgproc.circle(image, center, radius, color, 1);
        Log.i(TAG, "DrawCircleByCenter");
    }

    private double getDist(Point point1, Point point2) {
        return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y + point2.y, 2));
    }

    private double getAngle(Point point1, Point point2) {
        if (point1.y == point2.y) {
            point1.y += 0.01;
        }
        return Math.toDegrees(Math.atan((point1.x - point2.x) / (point1.y - point2.y)));
    }

    public void toBinary(Mat mat, int threshold, int maxVal) {
        Imgproc.threshold(mat, mat, threshold, maxVal, Imgproc.THRESH_BINARY);
        Log.i(TAG, "toBinary");
    }

    public Point[] checkPoint(Point[] points) {
        int lastLength = -1;
        int thisLength = 0;
        Point[] lp = points;
        Point[] np;
        while (true) {
            np = checkPointOnce(lp);
            thisLength = np.length;
            if (thisLength == lastLength) {
                break;
            }
            lastLength = thisLength;
            lp = np;
        }
        return np;
    }

    /**
     * 合并直线上的点[分步]
     */
    private Point[] checkPointOnce(Point[] points) {
        int length = points.length;
        boolean flag = false;// 是否找到可删除点
        if (length < 4) {
            return points;// 如果小于四个点 免了判断
        }
        label: for (int i = 0; i < length; i++) {// 得到点1
            for (int j = 0; j < length; j++) {// 得到点2
                if (j == i) {
                    continue;
                }
                for (int k = 0; k < length; k++) {// 得到点3
                    if (k == j || k == i) {
                        continue;
                    }
                    // int slope = 0;//斜率
                    double d1 = getAngle(points[i], points[j]);// i,j直线角度
                    double d2 = getAngle(points[i], points[k]);// i,k直线角度
                    double angelMin = d1 - d2;
                    if (Math.abs(angelMin) < 10) {// 如果倾角非常接近，删除中间的点
                        int needDelete = deleteMiddlePointToNull(points[i],
                                points[j], points[k]);
                        if (needDelete == 1) {
                            points[i] = null;
                        } else if (needDelete == 2) {
                            points[j] = null;
                        } else if (needDelete == 3) {
                            points[k] = null;
                        }
                        flag = true;
                        break label;
                    }
                }
            }
        }
        if (flag) {
            Point[] newPoints = new Point[length - 1];
            int index = 0;
            for (Point p : points) {// 准备一个没有空值的新数组
                if (null != p) {
                    newPoints[index] = p;
                    index++;
                }
            }
            return newPoints;
        } else {
            return points;
        }
    }

    private int deleteMiddlePointToNull(Point p1, Point p2, Point p3) {
        double a = p1.x + p1.y;
        double b = p2.x + p2.y;
        double c = p3.x + p3.y;
        if ((a > b && b > c) || (a < b && b < c)) {// b在中间
            return 2;
        } else if ((c > a && a > b) || (c < a && a < b)) {// a在中间
            return 1;
        } else {
            return 3;
        }
    }
    /**
     * 横向填充杂色
     */
    public void coverBackGroundToBlack(Mat mat) {
        final double blackPixle[] = { 0.0 };
        for (int y = 0; y < mat.height(); y++) {
            for (int x = 0; x < mat.width(); x++) {
                double pixle[] = mat.get(y, x);
                if (pixle[0] == 255.0) {// 如果是白色
                    mat.put(y, x, blackPixle);
                } else {// 遇到黑色
                    break;
                }
            }
            for (int x = mat.width() - 1; x > 0; x--) {
                double pixle[] = mat.get(y, x);
                if (pixle[0] == 255.0) {// 如果是白色
                    mat.put(y, x, blackPixle);
                } else {// 遇到黑色
                    break;
                }
            }
        }
        Log.d(TAG, "背景涂黑完成");
    }
}
