package com.example.imageprocessor.ui.stitcher;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.Utility;
import com.example.imageprocessor.room.Image;
import com.example.imageprocessor.room.ImageViewModel;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/*
 Reference:
    https://learning.oreilly.com/library/view/opencv-3-blueprints/9781784399757/ch04s02.html
*/
public class StitcherFragment extends Fragment {

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private final static String TAG = "StitchingActivity: ";

    private StitcherViewModel mViewModel;
    private View root;
    private Button captureButton, saveButton;
    private SurfaceView surfaceView, surfaceViewOnTop;
    private Camera camera;
    private boolean isPreview;
    private boolean safeToTakePicture = true;
    private ProgressDialog progressDialog;

    private List<Mat> listImages = new ArrayList<>();

    public StitcherFragment() {
    }

    public static StitcherFragment newInstance() {
        return new StitcherFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_stitcher, container, false);
        isPreview = false;
        surfaceView = root.findViewById(R.id.surfaceView);
        surfaceViewOnTop = root.findViewById(R.id.surfaceViewOnTop);
        captureButton = root.findViewById(R.id.captureButton);
        saveButton = root.findViewById(R.id.saveStitcherButton);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(StitcherViewModel.class);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Camera.Parameters params = camera.getParameters();
                Camera.Size size = getPreviewSize(params);
                if (size != null) {
                    params.setPreviewSize(size.width, size.height);
                    camera.setParameters(params);
                    camera.setDisplayOrientation(90);
                    camera.startPreview();
                    isPreview = true;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        surfaceViewOnTop.setZOrderOnTop(true);
        surfaceViewOnTop.getHolder().setFormat(PixelFormat.TRANSPARENT);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null && safeToTakePicture) {
                    safeToTakePicture = false;
                    camera.takePicture(null, null, jpegCallback);
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showProcessingDialog();
                        try {
                            int elems = listImages.size();
                            long[] tempobjaddr = new long[elems];
                            for (int i = 0; i < elems; i++)
                                tempobjaddr[i] = listImages.get(i).getNativeObjAddr();
                            Mat result = new Mat();
                            processPanorama(tempobjaddr, result.getNativeObjAddr());
                            Log.i(TAG, Arrays.toString(tempobjaddr));
                            // Save the image to external storage
                            File sdcard = Environment.getExternalStorageDirectory();
                            final String fileName = sdcard.getAbsolutePath() + "/stitcher_" + System.currentTimeMillis() + ".png";
                            Imgcodecs.imwrite(fileName, result);
                            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "File saved at: " + fileName, Toast.LENGTH_LONG).show();
                                    Log.i(TAG, "File saved at: " + fileName);
                                    // TODO: Save image to db !!!!!!!
//                                    String imageDate = Utility.getCurrentDateTime();
//                                    String imageUri = ;
//                                    ImageViewModel imageViewModel = ViewModelProviders.of(getFragment()).get(ImageViewModel.class);
//                                    Image image = new Image(fileName, imageDate, imageUri, 3);
                                }
                            });

                            listImages.clear();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        closeProcessingDialog();
                    }
                }).start();
            }
        });
    }

    private void showProcessingDialog() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                camera.stopPreview();
                progressDialog = ProgressDialog.show(getActivity(), "", "Panorama", true);
                progressDialog.setCancelable(false);
            }
        });
    }

    private void closeProcessingDialog() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                camera.startPreview();
                progressDialog.dismiss();
            }
        });
    }

    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // decode the byte array to a bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            // Rotate the picture to fit portrait mode
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            listImages.add(mat);

            Canvas canvas = null;
            try {
                canvas = surfaceViewOnTop.getHolder().lockCanvas(null);
                synchronized (surfaceView.getHolder()) {
                    // Clear canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    // Scale the image to fit the SurfaceView
                    float scale = 1.0f * surfaceView.getHeight() / bitmap.getHeight();
                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap, (int)(scale * bitmap.getWidth()), surfaceView.getHeight() , false);
                    Paint paint = new Paint();
                    // Set the opacity of the image
                    paint.setAlpha(200);
                    // Draw the image with an offset so we only see one third of image.
                    canvas.drawBitmap(scaleImage, -scaleImage.getWidth() * 2 / 3, 0, paint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    surfaceViewOnTop.getHolder().unlockCanvasAndPost(canvas);
                }
            }
            // Start preview the camera again and set the take picture flag to true
            camera.startPreview();
            safeToTakePicture = true;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isPreview)
            camera.stopPreview();
        camera.release();
        camera = null;
        isPreview = false;
    }

    private Camera.Size getPreviewSize(Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        return bestSize;
    }

    public Fragment getFragment() {
        return this;
    }

    public native void processPanorama(long[] imageAddressArray, long outputAddress);
}
