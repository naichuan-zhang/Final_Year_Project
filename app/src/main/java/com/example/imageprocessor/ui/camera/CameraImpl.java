package com.example.imageprocessor.ui.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.imageprocessor.misc.Constants;
import com.example.imageprocessor.misc.Utility;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraImpl implements ICamera {

    private Uri uri;
    private String currentPhotoPath;
    private ICameraFragment view;
    private Activity activity;

    public CameraImpl(ICameraFragment view, Activity activity) {
        this.view = view;
        this.activity = activity;
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    @Override
    public void openCamera() {
        // check and ask permissions needed for camera
        if (Utility.checkAndAskCameraPermissions(getContext(), activity)) {
            Toast.makeText(activity, "System camera opened ...", Toast.LENGTH_SHORT).show();
            // open camera
            try {
                File file = createImageFile(getContext());
                if (Build.VERSION.SDK_INT >= 24 && file != null)
                    uri = FileProvider.getUriForFile(getContext(), "com.example.imageprocessor.fileprovider", file);
                else
                    uri = Uri.fromFile(file);
                // intent for invoke camera to capture image
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                view.startOtherActivity(intent, Constants.CAMERA_REQUEST_CODE);

            } catch (IOException e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFilename = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFilename,             /* prefix */
                ".jpg",              /* suffix */
                storageDir                 /* directory */
        );
        currentPhotoPath = "file://" + image.getAbsolutePath();
        return image;
    }

    private Context getContext() {
        return view.getContext();
    }
}
