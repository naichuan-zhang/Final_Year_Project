package com.example.imageprocessor.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.ZoomableCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

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
    private final static int VIEW_MODE_ZOOM = 3;

    private ZoomableCameraView javaCameraView;
    private LinearLayout zoomLinearLayout;
    private SeekBar zoomSeekBar;
    private MenuItem itemPreviewRGBA;
    private MenuItem itemPreviewGray;
    private MenuItem itemPreviewCanny;
    private MenuItem itemZoom;

    private int width;
    private int height;
    private int viewMode;

    private Mat matRgba;
    private Mat matGray;
    private Mat matCanny;
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
//        zoomSeekBar.setVisibility(View.INVISIBLE);
        javaCameraView.setZoomSeekBar(zoomSeekBar);
        zoomLinearLayout = findViewById(R.id.linearLayoutZoom);
        zoomLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.width = width;
        this.height = height;

        matRgba = new Mat(height, width, CvType.CV_8UC4);
        matGray = new Mat(height, width, CvType.CV_8UC1);
        matCanny = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        matRgba.release();
        matGray.release();
        matCanny.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final int mode = viewMode;
        switch (mode) {
            case VIEW_MODE_RGBA:
                setZoomLinearLayoutVisibility(View.GONE);
                matRgba = inputFrame.rgba();
                break;
            case VIEW_MODE_GRAY:
                setZoomLinearLayoutVisibility(View.GONE);
                Imgproc.cvtColor(inputFrame.gray(), matRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_CANNY:
                setZoomLinearLayoutVisibility(View.GONE);
                matRgba = inputFrame.rgba();
                Imgproc.Canny(inputFrame.gray(), matCanny, 80, 100);
                Imgproc.cvtColor(matCanny, matRgba, Imgproc.COLOR_GRAY2BGRA, 4);
                break;
            case VIEW_MODE_ZOOM:
                setZoomLinearLayoutVisibility(View.VISIBLE);
                break;
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
        itemZoom = menu.add(getString(R.string.zoom));

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
        else if (item == itemZoom)
            viewMode = VIEW_MODE_ZOOM;

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

    public void setZoomLinearLayoutVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                zoomLinearLayout.setVisibility(visibility);
            }
        });
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
