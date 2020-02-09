package com.example.imageprocessor.ui.camera;

import android.net.Uri;

public interface ICamera {

    Uri getUri();
    String getCurrentPhotoPath();
    void openCamera();
}
