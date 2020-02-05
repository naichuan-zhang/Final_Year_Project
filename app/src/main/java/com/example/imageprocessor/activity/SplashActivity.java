package com.example.imageprocessor.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.imageprocessor.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView imageView;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        animation = AnimationUtils.loadAnimation(this, R.anim.splash_enlarge);
        imageView = findViewById(R.id.splash_image);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    imageView.startAnimation(animation);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                redirect();
            }
        }).start();
    }

    private void redirect() {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}
