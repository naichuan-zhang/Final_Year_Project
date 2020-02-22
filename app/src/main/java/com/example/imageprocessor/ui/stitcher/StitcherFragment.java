package com.example.imageprocessor.ui.stitcher;

import androidx.lifecycle.ViewModelProviders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.imageprocessor.R;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class StitcherFragment extends Fragment {

    private final static String TAG = "StitchingActivity: ";

    private StitcherViewModel mViewModel;
    private View root;

    private final int CLICK_PHOTO = 1;
    private Uri fileUri;
    private ImageView imageViewStitcher;
    private Button bClickImage, bDone;
    Mat src;
    ArrayList<Mat> clickedImages;
    static int ACTION_MODE = 0, MODE_NONE = 0;
    private static final String FILE_LOCATION = Environment.getExternalStorageDirectory() + "/Download/PacktBook/Chapter6/";

    public static StitcherFragment newInstance() {
        return new StitcherFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_stitcher, container, false);
        clickedImages = new ArrayList<>();
        imageViewStitcher = root.findViewById(R.id.imageViewStitcher);
        bClickImage = root.findViewById(R.id.bClickImage);
        bDone = root.findViewById(R.id.bDone);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(StitcherViewModel.class);
    }

    public native int stitch(Object images[], int size, long addrSrcRes);
}
