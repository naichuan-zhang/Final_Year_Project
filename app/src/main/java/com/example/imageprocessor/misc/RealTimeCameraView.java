package com.example.imageprocessor.misc;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

import org.opencv.android.JavaCameraView;

import java.util.List;


/**
 * Reference -> Zoomable:
 *      https://stackoverflow.com/questions/32718941/is-it-possible-to-zoom-and-focus-using-opencv-on-android
 */
public class RealTimeCameraView extends JavaCameraView
        implements Camera.PictureCallback, Flash {

    private final static String TAG = "ZoomableCameraView: ";

    private SeekBar seekBar;

    public RealTimeCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public RealTimeCameraView(Context context, AttributeSet attrs) {
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

    @SuppressWarnings("deprecation")
    @Override
    public void turnOnFlash() {
        if (mCamera == null) return;

        Camera.Parameters params = mCamera.getParameters();
        if (params == null) return;

        List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes == null) return;
        String flashMode = params.getFlashMode();

        if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(params);
                Log.i(TAG, "turn on the flash");
            } else {
                Log.i(TAG, "FLASH_MODE_TORCH not supported");
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void turnOffFlash() {
        if (mCamera == null) return;

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) return;

        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) return;
        String flashMode = parameters.getFlashMode();

        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                Log.i(TAG, "turn off the flash");
            } else {
                Log.i(TAG, "FLASH_MODE_OFF not supported");
            }
        }
    }
}
