package com.example.imageprocessor.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public interface IGalleryFragment {

    Context getContext();
    Activity getActivity();
    void startOtherActivity(Intent intent, int requestCode);
}
