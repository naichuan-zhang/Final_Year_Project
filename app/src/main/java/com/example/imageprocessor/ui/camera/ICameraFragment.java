package com.example.imageprocessor.ui.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public interface ICameraFragment {

    Context getContext();
    Activity getActivity();
    void startOtherActivity(Intent intent, int requestCode);
}
