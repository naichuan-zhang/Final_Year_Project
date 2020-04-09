package com.example.imageprocessor.ui.about;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.imageprocessor.R;

public class AboutFragment extends Fragment {

    private final static String TAG = "AboutFragment: ";

    private final static String PHONE = "0833955546";
    private final static String EMAIL = "18111521@studentmail.ul.ie";
    private final static String GITHUB_LINK = "https://github.com/naichuan-zhang";
    private final static String PROJECT_LINK = "https://github.com/naichuan-zhang/Final_Year_Project";

    private View root;

    private RelativeLayout phone;
    private RelativeLayout email;
    private RelativeLayout github;
    private Button viewProjectDetailsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_about, container, false);
        phone = root.findViewById(R.id.phone);
        email = root.findViewById(R.id.email);
        github = root.findViewById(R.id.github);
        viewProjectDetailsButton = root.findViewById(R.id.viewProjectDetailsButton);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "phone ...");
                callPhone();
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "email ...");
                sendEmail();
            }
        });

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "github ...");
                openGithub();
            }
        });

        viewProjectDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "viewProjectDetailsButton ...");
                viewProjectDetails();
            }
        });
    }

    private void callPhone() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + PHONE);
        intent.setData(data);
        startActivity(intent);
    }

    private void sendEmail() {
        String[] email = {EMAIL};
        Uri uri = Uri.parse("mailto:" + EMAIL);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_CC, email);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Image Process Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, "Hello!");
        startActivity(Intent.createChooser(intent, "Please choose an app to send email: "));
    }

    private void openGithub() {
        Uri uri = Uri.parse(GITHUB_LINK);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void viewProjectDetails() {
        Uri uri = Uri.parse(PROJECT_LINK);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
