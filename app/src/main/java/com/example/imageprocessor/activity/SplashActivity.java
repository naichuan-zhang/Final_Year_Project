package com.example.imageprocessor.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.example.imageprocessor.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        handler.sendMessageDelayed(new Message(), 2000);
    }

    private Handler handler = new Handler() {

        public void handleMessage(Message message) {
            // TODO: Change Start to Main later ...
            Intent intent = new Intent(SplashActivity.this, StartActivity.class);
            SplashActivity.this.startActivity(intent);
            finish();
        }
    };
}
