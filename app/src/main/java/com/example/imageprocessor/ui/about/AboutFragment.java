package com.example.imageprocessor.ui.about;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.imageprocessor.R;

import java.util.Objects;

public class AboutFragment extends Fragment {

    // TODO: ERROR occurred when quits AboutFragment !!!!!!!!!!!!
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        Toolbar toolbar = root.findViewById(R.id.aboutToolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity()))
                .setSupportActionBar(toolbar);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof AppCompatActivity) {
            Objects.requireNonNull(((AppCompatActivity) getActivity())
                    .getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
    }
}
