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
import com.example.imageprocessor.misc.ZoomableCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
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

    private final static List<String> shapes = new ArrayList<>(
            Arrays.asList("Line", "Circle")
    );

    private ZoomableCameraView javaCameraView;
    private SeekBar zoomSeekBar;
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
    private Mat circles;

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
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.width = width;
        this.height = height;
        matRgba = new Mat(height, width, CvType.CV_8UC4);
        matGray = new Mat(height, width, CvType.CV_8UC1);
        matCanny = new Mat(height, width, CvType.CV_8UC1);
        matContours = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        matRgba.release();
        matGray.release();
        matCanny.release();
        matContours.release();
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
                Imgproc.Canny(inputFrame.gray(), matCanny, 80, 100);
                Imgproc.cvtColor(matCanny, matRgba, Imgproc.COLOR_GRAY2BGRA, 4);
                break;
            case VIEW_MODE_CONTOURS:
                matRgba = inputFrame.rgba();
                Imgproc.cvtColor(inputFrame.rgba(), matContours, Imgproc.COLOR_RGBA2GRAY);
                findContours();
                break;
            case VIEW_MODE_CIRCLES:
                matRgba = inputFrame.rgba();
                matGray = inputFrame.gray();
                findCircles();
            default:
                break;
        }
        return matRgba;
    }

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
            if (shape.equalsIgnoreCase("circle"))
                viewMode = VIEW_MODE_CIRCLES;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findContours() {
        Core.inRange(matContours, new Scalar(22, 0, 0), new Scalar(241, 255, 255), matContours);
        Imgproc.erode(matContours, matContours,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
        Imgproc.dilate(matContours, matContours,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8)));
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(matContours, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = -1;
        int maxAreaIdx = -1;
        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea > maxArea) {
                maxArea = contourArea;
                maxAreaIdx = i;
            }
        }
        Imgproc.cvtColor(matContours, matContours, Imgproc.COLOR_BayerBG2BGR);
        Imgproc.drawContours(matRgba, contours, maxAreaIdx, new Scalar(0, 255, 0), 1);
    }

    // TODO: NOT ACCURATE !!!!!!!!!!!!!!!!!!!!!
    private void findCircles() {
        circles = new Mat();
        Imgproc.blur(matGray, matGray, new Size(7, 7), new Point(2, 2));
        Imgproc.HoughCircles(matGray, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 90, 500);
        if (circles.cols() > 0) {
            for (int i = 0; i < Math.min(circles.cols(), 5); i++) {
                double[] vec = circles.get(0, i);
                if (vec == null) break;
                Point center = new Point((int) vec[0], (int) vec[1]);
                int radius = (int) vec[2];
                Imgproc.line(matRgba, center, center, new Scalar(255, 0, 0), 3);
                Imgproc.circle(matRgba, center, radius, new Scalar(0, 255, 0), 1);
            }
        }
        circles.release();
        circles = null;
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
