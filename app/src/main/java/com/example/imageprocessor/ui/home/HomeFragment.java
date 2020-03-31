package com.example.imageprocessor.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.imageprocessor.R;
import com.example.imageprocessor.activity.MainActivity;
import com.example.imageprocessor.activity.SettingsActivity;

public class HomeFragment extends Fragment {

    private final static String TAG = "HomeFragment: ";

    private HomeViewModel homeViewModel;
    private View root;

    private CardView cameraCardView;
    private CardView galleryCardView;
    private CardView historyCardView;
    private CardView stitcherCardView;
    private CardView settingsCardView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        cameraCardView = root.findViewById(R.id.cameraCardView);
        galleryCardView = root.findViewById(R.id.galleryCardView);
        historyCardView = root.findViewById(R.id.historyCardView);
        stitcherCardView = root.findViewById(R.id.stitcherCardView);
        settingsCardView = root.findViewById(R.id.settingsCardView);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        cameraCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "camera clicked");
                Navigation.findNavController(root)
                        .navigate(R.id.action_nav_home_to_nav_camera);
            }
        });

        galleryCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "gallery clicked");
                Navigation.findNavController(root)
                        .navigate(R.id.action_nav_home_to_nav_gallery);
            }
        });

        historyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "history clicked");
                Navigation.findNavController(root)
                        .navigate(R.id.action_nav_home_to_nav_history);
            }
        });

        stitcherCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "stitcher clicked");
                Navigation.findNavController(root)
                        .navigate(R.id.action_nav_home_to_nav_stitcher);
            }
        });

        settingsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "settings clicked");
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}