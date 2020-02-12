package com.example.imageprocessor.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.imageprocessor.misc.Constants;
import com.example.imageprocessor.misc.Utility;

public class GalleryImpl implements IGallery {

    private Uri uri;
    private Activity activity;
    private IGalleryFragment view;

    public GalleryImpl(IGalleryFragment view, Activity activity) {
        this.view = view;
        this.activity = activity;
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void openGallery() {
        Context context = view.getContext();
        // check and ask permissions needed for system gallery
        if (Utility.checkAndAskGalleryPermissions(context, activity)) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            view.startOtherActivity(intent, Constants.GALLERY_REQUEST_CODE);
        }
    }
}
