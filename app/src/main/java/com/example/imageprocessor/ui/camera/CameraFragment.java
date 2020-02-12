package com.example.imageprocessor.ui.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.Constants;
import com.example.imageprocessor.misc.Utility;
import com.example.imageprocessor.room.Image;
import com.example.imageprocessor.room.ImageViewModel;

import java.io.File;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class CameraFragment extends Fragment implements ICameraFragment {

    private CameraViewModel cameraViewModel;
    private ConstraintLayout constraintLayoutSystemCamera;
    private ICamera cameraImpl;

    private View root;

    private final static String TAG = "CameraFragment: ";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cameraViewModel =
                ViewModelProviders.of(this).get(CameraViewModel.class);
        cameraImpl = new CameraImpl(this, getActivity());

        root = inflater.inflate(R.layout.fragment_camera, container, false);

        constraintLayoutSystemCamera = root.findViewById(R.id.constraintLayoutSystemCamera);
        constraintLayoutSystemCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "constraintLayoutSystemCamera clicked...");
                cameraImpl.openCamera();
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // when the request was successful
        if (resultCode == RESULT_OK) {
            // check which request we are responding to
            if (requestCode == Constants.CAMERA_REQUEST_CODE) {
                final Uri uri = cameraImpl.getUri();
                Log.i(TAG, uri.toString());

                // add picture to gallery
                addPictureToGallery(uri);
                saveImageToDatabase(uri.toString());

                // TODO: Create bundle
                // redirect to PreviewFragment
                Navigation.findNavController(root).navigate(R.id.action_nav_camera_to_previewFragment/*, bundle */ );
            }
        }
    }

    private void saveImageToDatabase(String imageUri) {
        String imageDate = Utility.getCurrentDateTime();
        String imageName = new File(imageUri).getName();
        int imageSource = 2;        // from camera
        Image image = new Image(imageName, imageDate, imageUri, imageSource);
        ImageViewModel imageViewModel = ViewModelProviders.of(this).get(ImageViewModel.class);
        imageViewModel.insertImages(image);
    }

    @Override
    public void startOtherActivity(Intent intent, int requestCode) {
        if (intent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null)
            startActivityForResult(intent, requestCode);
    }

    private void addPictureToGallery(Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        Objects.requireNonNull(getContext()).sendBroadcast(mediaScanIntent);
    }
}
