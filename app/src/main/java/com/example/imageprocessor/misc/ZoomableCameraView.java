package com.example.imageprocessor.misc;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.SeekBar;

import org.opencv.android.JavaCameraView;

import java.util.List;


/**
 * Reference -> Zoomable:
 *      https://stackoverflow.com/questions/32718941/is-it-possible-to-zoom-and-focus-using-opencv-on-android
 */
public class ZoomableCameraView extends JavaCameraView implements Camera.PictureCallback {

    private final static String TAG = "ZoomableCameraView: ";

    private SeekBar seekBar;

    public ZoomableCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public ZoomableCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setZoomSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
    }

    public void enableZoomSeekBar(Camera.Parameters params) {
        final int max = params.getMaxZoom();
        seekBar.setMax(max);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int val = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                val = progress;
                Camera.Parameters params = mCamera.getParameters();
                params.setZoom(progress);
                mCamera.setParameters(params);
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
    protected boolean initializeCamera(int width, int height) {
        boolean ret = super.initializeCamera(width, height);
        Camera.Parameters params = mCamera.getParameters();
        if (params.isZoomSupported())
            enableZoomSeekBar(params);
        mCamera.setParameters(params);
        return ret;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // TODO: Impl take picture onclick
    }
}
