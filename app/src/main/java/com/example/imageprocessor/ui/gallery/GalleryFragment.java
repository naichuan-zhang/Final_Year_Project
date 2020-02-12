package com.example.imageprocessor.ui.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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

public class GalleryFragment extends Fragment implements IGalleryFragment {

    private GalleryViewModel galleryViewModel;
    private ConstraintLayout constraintLayoutSystemGallery;
    private IGallery galleryImpl;

    private View root;

    private final static String TAG = "GalleryFragment: ";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        galleryImpl = new GalleryImpl(this, getActivity());

        root = inflater.inflate(R.layout.fragment_gallery, container, false);

        constraintLayoutSystemGallery = root.findViewById(R.id.constraintLayoutSystemGallery);
        constraintLayoutSystemGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "constraintLayoutSystemGallery clicked...");
                galleryImpl.openGallery();
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
            if (requestCode == Constants.GALLERY_REQUEST_CODE) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();

                    // save image data to Room db
                    saveImageToDatabase(uri.toString());

                    // TODO: create bundle
                    // redirect to PreviewFragment
                    Navigation.findNavController(root).navigate(R.id.action_nav_gallery_to_previewFragment);
                }
            }
        }
    }

    private void saveImageToDatabase(String imageUri) {
        String imageDate = Utility.getCurrentDateTime();
        String imageName = new File(imageUri).getName();
        int imageSource = 1;        // from gallery
        Image image = new Image(imageName, imageDate, imageUri, imageSource);
        ImageViewModel imageViewModel = ViewModelProviders.of(this).get(ImageViewModel.class);
        imageViewModel.insertImages(image);
    }

    @Override
    public void startOtherActivity(Intent intent, int requestCode) {
        if (intent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }
}