package com.example.imageprocessor.ui.stitcher;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.hardware.Camera.CameraInfo.*;


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

    private final static int OK = 0;
    private final static int ERR_NEED_MORE_IMGS = 1;
    private final static int ERR_HOMOGRAPHY_EST_FAIL = 2;
    private final static int ERR_CAMERA_PARAMS_ADJUST_FAIL = 3;
    private final static int ERR_OTHER = 4;

    private static int num = 1;

    private StitcherViewModel stitcherViewModel;
    private View root;
    private Button captureButton, saveButton;
    private SurfaceView surfaceView;
    private ImageView stitcherImageView;
    private Camera camera;
    private boolean isPreview;
    private boolean safeToTakePicture = true;
    private ProgressDialog progressDialog;

    private List<Mat> images = new ArrayList<>();
    private Uri uri;

    private Mat src;

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
        stitcherImageView = root.findViewById(R.id.stitcherImageView);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stitcherViewModel = ViewModelProviders.of(this).get(StitcherViewModel.class);

        // set stitcher image view invisible by default
        stitcherImageView.setVisibility(View.INVISIBLE);

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
                // Do nothing
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
                        Looper.prepare();

                        stitcherImageView.setVisibility(View.INVISIBLE);

                        if (images.size() == 0) {
                            Toast.makeText(getContext(), "No images captured", Toast.LENGTH_SHORT).show();
                        } else if (images.size() == 1) {
                            Toast.makeText(getContext(), "Only one image captured", Toast.LENGTH_SHORT).show();
                        } else {
                            showProcessingDialog();
                            int status = processImage();
                            closeProcessingDialog();
                            switch (status) {
                                case OK:
                                    Toast.makeText(getContext(), "Stitched image has been saved successfully",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case ERR_NEED_MORE_IMGS:
                                    Toast.makeText(getContext(), "Error: Need more images",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case ERR_HOMOGRAPHY_EST_FAIL:
                                    Toast.makeText(getContext(), "Error: Homography estimation failed",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case ERR_CAMERA_PARAMS_ADJUST_FAIL:
                                    Toast.makeText(getContext(), "Error: Camera parameter adjustment failed",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case ERR_OTHER:
                                    Toast.makeText(getContext(), "Error: An unknown error occurred",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    break;
                            }
                        }
                        Looper.loop();
                    }
                }).start();
            }
        });
    }

    private int processImage() {
        try {
            Mat srcRes = new Mat();
            int status = stitchImages(images.toArray(), images.size(), srcRes.getNativeObjAddr());
            Log.i(TAG, "rows: " + srcRes.rows() + " cols: " + srcRes.cols() + " success: " + status);
            if (status == ERR_NEED_MORE_IMGS) {
                num = 1;
                return ERR_CAMERA_PARAMS_ADJUST_FAIL;
            } else if (status == ERR_HOMOGRAPHY_EST_FAIL) {
                num = 1;
                return ERR_HOMOGRAPHY_EST_FAIL;
            } else if (status == ERR_CAMERA_PARAMS_ADJUST_FAIL) {
                num = 1;
                return ERR_CAMERA_PARAMS_ADJUST_FAIL;
            }
            Imgproc.cvtColor(srcRes, srcRes, Imgproc.COLOR_BGR2RGBA);
            // save image result into database
            Bitmap bitmap = Bitmap.createBitmap(srcRes.cols(), srcRes.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(srcRes, bitmap);
            uri = getImageUri(Objects.requireNonNull(getContext()), bitmap);
            saveImageToDatabase();
            // clear the array
            images.clear();
            num = 1;
            return OK;
        } catch (Exception e) {
            e.printStackTrace();
            num = 1;
            return ERR_OTHER;
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

    // Reference: https://learning.oreilly.com/library/view/opencv-3-blueprints/9781784399757/ch04s02.html
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
            src = new Mat();
            Utils.bitmapToMat(bitmap, src);
            displayCapturedImage(bitmap);
            Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2RGB);
            images.add(src);
            // Start preview the camera again and set the take picture flag to true
            camera.startPreview();
            safeToTakePicture = true;
            num++;
        }
    };

    private void displayCapturedImage(Bitmap bitmap) {
        stitcherImageView.setVisibility(View.VISIBLE);
        stitcherImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_FACING_BACK);
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

    // Reference: https://learning.oreilly.com/library/view/opencv-3-blueprints/9781784399757/ch04s02.html
    @SuppressWarnings("deprecation")
    private Camera.Size getPreviewSize(Camera.Parameters parameters){
        Camera.Size size;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        for (int i = 0; i < sizeList.size(); i++) {
            Log.i(TAG, sizeList.get(i).height + " " + sizeList.get(i).width);
        }
        size = sizeList.get(0);
        for (int i = 1; i < sizeList.size(); i++){
            if ((sizeList.get(i).width * sizeList.get(i).height) >
                    (size.width * size.height)){
                size = sizeList.get(i);
            }
        }
        Log.i(TAG, "Selected Preview Size: " + size.width + " " + size.height);

        return size;
    }

    public Fragment getFragment() {
        return this;
    }

    public native int stitchImages(Object[] images, int size, long addrSrcRes);
}
