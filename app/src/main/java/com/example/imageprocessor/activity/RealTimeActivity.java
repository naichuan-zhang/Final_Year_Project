package com.example.imageprocessor.activity;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.DarkModeSharedPref;
import com.example.imageprocessor.misc.LanguageSharedPref;
import com.example.imageprocessor.misc.OpenCVUtil;
import com.example.imageprocessor.misc.RealTimeCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
    private final static int VIEW_MODE_PENTAGONS = 7;
    private final static int VIEW_MODE_LINES = 8;

    private final static int FLASH_ON = 9;
    private final static int FLASH_OFF = 10;

    private final static List<String> shapes = new ArrayList<>(
            Arrays.asList("Triangle", "Rectangle", "Pentagon", "Circle")
    );

    private static int thresh = 0;

    private RealTimeCameraView javaCameraView;
    private SeekBar zoomSeekBar;
    private SeekBar thresholdSeekBar;
    private ImageButton flashButton;

    private MenuItem itemPreviewRGBA;
    private MenuItem itemPreviewGray;
    private MenuItem itemPreviewCanny;
    private MenuItem itemPreviewContours;
    private MenuItem[] shapeMenuItems;
    private SubMenu itemPreviewShapes;

    private int width;
    private int height;
    private int viewMode;
    private int flashMode;

    private Mat matRgba;
    private Mat matGray;
    private Mat matCanny;
    private Mat matContours;
    private Mat matShapes;
    private Mat matCircles;

    private MatOfPoint2f approxCurve;
    private OpenCVUtil openCVUtil;

    DarkModeSharedPref darkModeSharedPref;
    LanguageSharedPref languageSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // init dark mode settings on RealTimeActivity
        darkModeSharedPref = new DarkModeSharedPref(this);
        if (darkModeSharedPref.loadDarkModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

        // init language settings on RealTimeActivity
        languageSharedPref = new LanguageSharedPref(this);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        if (languageSharedPref.loadLanguageState()
                .equalsIgnoreCase("english")) {
            configuration.locale = Locale.ENGLISH;
        } else {
            configuration.locale = Locale.CHINA;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        resources.updateConfiguration(configuration, metrics);


        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_real_time);
        setTitle(R.string.title_activity_real_time);

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

        flashButton = findViewById(R.id.flashImageButton);
        flashButton.setVisibility(View.VISIBLE);
        // set init flash mode is FLASH_OFF
        flashMode = FLASH_OFF;

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flashMode == FLASH_OFF) {
                    javaCameraView.turnOnFlash();
                    flashButton.setImageDrawable(getDrawable(R.drawable.ic_flash_on));
                    flashMode = FLASH_ON;
                    Toast.makeText(getApplicationContext(),
                            "Flash has been turned on", Toast.LENGTH_SHORT).show();
                } else if (flashMode == FLASH_ON) {
                    javaCameraView.turnOffFlash();
                    flashButton.setImageDrawable(getDrawable(R.drawable.ic_flash_off_black_24dp));
                    flashMode = FLASH_OFF;
                    Toast.makeText(getApplicationContext(),
                            "Flash has been turned off", Toast.LENGTH_SHORT).show();
                }
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
        matCircles = new Mat();

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
        matCircles.release();
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
            case VIEW_MODE_PENTAGONS:
                matRgba = inputFrame.rgba();
                Imgproc.cvtColor(inputFrame.rgba(), matShapes, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.Canny(matShapes, matShapes, thresh, thresh * 2);
                findShapes();
                break;
            case VIEW_MODE_CIRCLES:
                return findCircles(inputFrame);
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
                            && viewMode == VIEW_MODE_PENTAGONS) {
                        Imgproc.drawContours(matRgba, contours, i, new Scalar(255, 0, 255), -1);
                    }
                }
            }
        }
    }

    private Mat findCircles(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat input = inputFrame.gray();
        Mat circles = new Mat();
        Imgproc.blur(input, input, new Size(7, 7), new Point(2, 2));
        Imgproc.HoughCircles(input, circles, Imgproc.CV_HOUGH_GRADIENT,
                2, 100, 200, 100, 0, 200);
        if (circles.cols() > 0) {
            for (int x=0; x < Math.min(circles.cols(), 5); x++ ) {
                double[] vec = circles.get(0, x);
                if (vec == null) break;
                Point center = new Point((int) vec[0], (int) vec[1]);
                int radius = (int) vec[2];

                Imgproc.circle(input, center, 3, new Scalar(255, 255, 255), 5);
                Imgproc.circle(input, center, radius, new Scalar(255, 255, 255), 2);
            }
        }
        circles.release();
        input.release();
        return inputFrame.rgba();
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
            if (shape.equalsIgnoreCase("triangle"))
                viewMode = VIEW_MODE_TRIANGLES;
            else if (shape.equalsIgnoreCase("rectangle"))
                viewMode = VIEW_MODE_RECTANGLES;
            else if (shape.equalsIgnoreCase("pentagon"))
                viewMode = VIEW_MODE_PENTAGONS;
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
