package com.example.imageprocessor.ui.detect;

import androidx.lifecycle.ViewModelProviders;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.imageprocessor.R;

import java.util.Objects;

public class DetectFragment extends Fragment {

    private View root;
    private DetectViewModel mViewModel;
    private ImageView detectImageView;

    public static DetectFragment newInstance() {
        return new DetectFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.detect_fragment, container, false);
        detectImageView = root.findViewById(R.id.detectImageView);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(DetectViewModel.class);
        Bitmap resultBitmap = Objects.requireNonNull(getArguments()).getParcelable("resultBitmap");
        if (resultBitmap != null) {
            detectImageView.setImageBitmap(resultBitmap);
        }
    }

}
