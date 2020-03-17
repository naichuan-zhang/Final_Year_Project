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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
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

    private static int num = 1;

    private StitcherViewModel stitcherViewModel;
    private View root;
    private Button captureButton, saveButton;
    private SurfaceView surfaceView;
    private Camera camera;
    private boolean isPreview;
    private boolean safeToTakePicture = true;
    private ProgressDialog progressDialog;

    private List<Mat> images = new ArrayList<>();
    private Uri uri;

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
        captureButton = root.findViewById(R.id.captureButton);
        saveButton = root.findViewById(R.id.saveStitcherButton);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stitcherViewModel = ViewModelProviders.of(this).get(StitcherViewModel.class);

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

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null && safeToTakePicture) {
                    safeToTakePicture = false;
                    camera.takePicture(null, null, jpegCallback);
                    Toast.makeText(getContext(), "You have taken " + num + " photos", Toast.LENGTH_SHORT).show();
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
                        processImage();
                        closeProcessingDialog();
                    }
                }).start();
            }
        });
    }

    private void processImage() {
        try {
            int size = images.size();
            long[] objAddrs = new long[size];
            for (int i = 0; i < size; i++)
                objAddrs[i] = images.get(i).getNativeObjAddr();
            Mat result = new Mat();
            stitchImages(objAddrs, result.getNativeObjAddr());
            Log.i(TAG, Arrays.toString(objAddrs));
            // Save the image to Room DB
            Bitmap bitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(result, bitmap);
            uri = getImageUri(Objects.requireNonNull(getContext()), bitmap);
            saveImageToDatabase();
            Toast.makeText(getContext(), "Stitched Image has been saved successfully...", Toast.LENGTH_SHORT).show();
            // Clear the array
            images.clear();
            num = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "", null);
        return Uri.parse(path);
    }

    private void saveImageToDatabase() {
        String imageDate = Utility.getCurrentDateTime();
        String imageUri = uri.toString();
        String imageName = new File(imageUri).getName();
        int imageSource = 3;        // from others
        Image image = new Image(imageName, imageDate, imageUri, imageSource);
        ImageViewModel imageViewModel = ViewModelProviders.of(getFragment()).get(ImageViewModel.class);
        imageViewModel.insertImages(image);
    }

    private void showProcessingDialog() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                camera.stopPreview();
                progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading), true);
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

    @SuppressWarnings("deprecation")
    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // Decode the byte array to a bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            // Rotate bitmap to fit the camera
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            // Convert bitmap to mat and add it to array
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            images.add(mat);
            // Start preview the camera again and set the take picture flag to true
            camera.startPreview();
            safeToTakePicture = true;
            num++;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open(0);
        num = 1;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isPreview)
            camera.stopPreview();
        camera.release();
        camera = null;
        isPreview = false;
        num = 1;
    }

    @SuppressWarnings("deprecation")
    private Camera.Size getPreviewSize(Camera.Parameters parameters){
        Camera.Size size;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        size = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (size.width * size.height)){
                size = sizeList.get(i);
            }
        }
        return size;
    }

    public Fragment getFragment() {
        return this;
    }

    public native void stitchImages(long[] imageAddressArray, long outputAddress);
}
