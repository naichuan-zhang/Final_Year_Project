package com.example.imageprocessor.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.OpenCVUtil;
import com.example.imageprocessor.misc.ZoomableCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RealTimeActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private final static String TAG = "RealTimeActivity: ";

    static {
        if (!OpenCVLoader.initDebug()) {
            // Do something which
        } else {
            // Close the application
        }
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private final static int VIEW_MODE_RGBA = 0;
    private final static int VIEW_MODE_GRAY = 1;
    private final static int VIEW_MODE_CANNY = 2;
    private final static int VIEW_MODE_CONTOURS = 3;
    private final static int VIEW_MODE_CIRCLES = 4;
    private final static int VIEW_MODE_TRIANGLES = 5;
    private final static int VIEW_MODE_RECTANGLES = 6;
    private final static int VIEW_MODE_PENTAGON = 7;

    private final static List<String> shapes = new ArrayList<>(
            Arrays.asList("Line", "Triangle", "Rectangle", "Pentagon", "Circle")
    );

    private static int thresh = 0;

    private ZoomableCameraView javaCameraView;
    private SeekBar zoomSeekBar;
    private SeekBar thresholdSeekBar;
    private MenuItem itemPreviewRGBA;
    private MenuItem itemPreviewGray;
    private MenuItem itemPreviewCanny;
    private MenuItem itemPreviewContours;
    private MenuItem[] shapeMenuItems;
    private SubMenu itemPreviewShapes;

    private int width;
    private int height;
    private int viewMode;

    private Mat matRgba;
    private Mat matGray;
    private Mat matCanny;
    private Mat matContours;
    private Mat matShapes;

    private MatOfPoint2f approxCurve;
    private OpenCVUtil openCVUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Hide status bar
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_real_time);

        javaCameraView = findViewById(R.id.javaCameraView);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        zoomSeekBar = findViewById(R.id.zoomSeekBar);
        javaCameraView.setZoomSeekBar(zoomSeekBar);
        zoomSeekBar.setVisibility(View.VISIBLE);

        thresholdSeekBar = findViewById(R.id.threshSeekBar);
        thresholdSeekBar.setVisibility(View.VISIBLE);
        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setProgress(progress);
                thresh = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.width = width;
        this.height = height;
        matRgba = new Mat(height, width, CvType.CV_8UC4);
        matGray = new Mat(height, width, CvType.CV_8UC1);
        matCanny = new Mat(height, width, CvType.CV_8UC1);
        matContours = new Mat(height, width, CvType.CV_8UC4);
        matShapes = new Mat();

        approxCurve = new MatOfPoint2f();
        openCVUtil = new OpenCVUtil();
    }

    @Override
    public void onCameraViewStopped() {
        matRgba.release();
        matGray.release();
        matCanny.release();
        matContours.release();
        matShapes.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final int mode = viewMode;
        switch (mode) {
            case VIEW_MODE_RGBA:
                matRgba = inputFrame.rgba();
                break;
            case VIEW_MODE_GRAY:
                Imgproc.cvtColor(inputFrame.gray(), matRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_CANNY:
                matRgba = inputFrame.rgba();
                Imgproc.Canny(inputFrame.gray(), matCanny, thresh, thresh * 2);
                Imgproc.cvtColor(matCanny, matRgba, Imgproc.COLOR_GRAY2BGRA, 4);
                break;
            case VIEW_MODE_CONTOURS:
                matRgba = inputFrame.rgba();
                Imgproc.cvtColor(inputFrame.rgba(), matContours, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.Canny(matContours, matContours, thresh, thresh * 2);
                findContours();
                break;
            case VIEW_MODE_TRIANGLES:
            case VIEW_MODE_RECTANGLES:
            case VIEW_MODE_CIRCLES:
            case VIEW_MODE_PENTAGON:
                matRgba = inputFrame.rgba();
                Imgproc.cvtColor(inputFrame.rgba(), matShapes, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.Canny(matShapes, matShapes, thresh, thresh * 2);
                findShapes();
                break;
            default:
                break;
        }
        return matRgba;
    }

    private void findContours() {
        Imgproc.dilate(matContours, matContours, new Mat(), new Point(-1, -1), 1);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(matContours, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double contourArea = Imgproc.contourArea(contour);
            if (Math.abs(contourArea) > 1000) {
                Imgproc.drawContours(matRgba, contours, i, new Scalar(0, 255, 0), 1);
            }
        }
    }

    private void findShapes() {
        Log.i(TAG, "findShapes ...");
        Imgproc.dilate(matShapes, matShapes, new Mat(), new Point(-1, -1), 1);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(matShapes, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);
            int vertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(contour);
            if (Math.abs(contourArea) > 1000) {
                if (vertices == 3 && viewMode == VIEW_MODE_TRIANGLES) {
                    Imgproc.drawContours(matRgba, contours, i, new Scalar(255, 255, 0), -1);

                } else if (vertices >= 4 && vertices <= 5) {
                    List<Double> cos = new ArrayList<>();
                    for (int j = 2; j < vertices + 1; j++) {
                        cos.add(openCVUtil.getAngle(
                                approxCurve.toArray()[j % vertices],
                                approxCurve.toArray()[j - 2],
                                approxCurve.toArray()[j - 1]));
                    }
                    Collections.sort(cos);
                    double mincos = cos.get(0);
                    double maxcos = cos.get(cos.size() - 1);
                    if (vertices == 4 && mincos >= -0.1 && maxcos <= 0.3
                            && viewMode == VIEW_MODE_RECTANGLES) {
                        Imgproc.drawContours(matRgba, contours, i, new Scalar(0, 255, 0), -1);

                    } else if (vertices == 5 && mincos > -0.34 && maxcos <= -0.27
                            && viewMode == VIEW_MODE_PENTAGON) {
                        Imgproc.drawContours(matRgba, contours, i, new Scalar(255, 0, 255), -1);
                    }

                } else if (vertices >= 10) {
                    // TODO: Not accurate for circle !!!!!!!!!!!
                    Rect rect = Imgproc.boundingRect(contour);
                    int radius = rect.width / 2;
                    if (Math.abs(1 - (rect.width / rect.height)) <= 0.2
                            && Math.abs(1 - (contourArea / (Math.PI * radius * radius))) <= 0.2
                            && viewMode == VIEW_MODE_CIRCLES) {
                        Imgproc.drawContours(matRgba, contours, i, new Scalar(0, 255, 255), -1);
                    }
                }
            }
        }
    }

//    // TODO: NOT ACCURATE !!!!!!!!!!!!!!!!!!!!!
//    private void findCircles() {
//        circles = new Mat();
//        Imgproc.blur(matGray, matGray, new Size(7, 7), new Point(2, 2));
//        Imgproc.HoughCircles(matGray, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 90, 500);
//        if (circles.cols() > 0) {
//            for (int i = 0; i < Math.min(circles.cols(), 5); i++) {
//                double[] vec = circles.get(0, i);
//                if (vec == null) break;
//                Point center = new Point((int) vec[0], (int) vec[1]);
//                int radius = (int) vec[2];
//                Imgproc.line(matRgba, center, center, new Scalar(255, 0, 0), 3);
//                Imgproc.circle(matRgba, center, radius, new Scalar(0, 255, 0), 1);
//            }
//        }
//        circles.release();
//        circles = null;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        itemPreviewRGBA = menu.add(getString(R.string.preview_rgba));
        itemPreviewGray = menu.add(getString(R.string.preview_gray));
        itemPreviewCanny = menu.add(getString(R.string.preview_canny));
        itemPreviewContours = menu.add(getString(R.string.preview_contours));
        itemPreviewShapes = menu.addSubMenu(getString(R.string.shapes));
        shapeMenuItems = new MenuItem[shapes.size()];
        int idx = 0;
        for (String shape : shapes) {
            shapeMenuItems[idx] = itemPreviewShapes.add(1, idx, Menu.NONE, shape);
            idx++;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == itemPreviewRGBA)
            viewMode = VIEW_MODE_RGBA;
        else if (item == itemPreviewGray)
            viewMode = VIEW_MODE_GRAY;
        else if (item == itemPreviewCanny)
            viewMode = VIEW_MODE_CANNY;
        else if (item == itemPreviewContours)
            viewMode = VIEW_MODE_CONTOURS;
        else if (item.getGroupId() == 1) {
            String shape = shapes.get(item.getItemId());
            if (shape.equalsIgnoreCase("triangle"))
                viewMode = VIEW_MODE_TRIANGLES;
            else if (shape.equalsIgnoreCase("rectangle"))
                viewMode = VIEW_MODE_RECTANGLES;
            else if (shape.equalsIgnoreCase("pentagon"))
                viewMode = VIEW_MODE_PENTAGON;
            else if (shape.equalsIgnoreCase("circle"))
                viewMode = VIEW_MODE_CIRCLES;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume...");
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    Log.i(TAG, "Load OpenCV successfully...");
                    javaCameraView.enableView();
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
