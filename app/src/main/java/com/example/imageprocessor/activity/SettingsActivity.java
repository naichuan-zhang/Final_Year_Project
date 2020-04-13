package com.example.imageprocessor.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.DarkModeSharedPref;
import com.example.imageprocessor.misc.LanguageSharedPref;
import com.example.imageprocessor.ui.settings.SettingsFragment;

import java.util.Locale;


public class SettingsActivity extends AppCompatActivity {

    private final static String TAG = "SettingsActivity: ";

    DarkModeSharedPref darkModeSharedPref;
    LanguageSharedPref languageSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // init dark mode settings on SettingsActivity
        darkModeSharedPref = new DarkModeSharedPref(this);
        if (darkModeSharedPref.loadDarkModeState()) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        // init language settings on SettingsActivity
        languageSharedPref = new LanguageSharedPref(this);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        if (languageSharedPref.loadLanguageState()
                .equalsIgnoreCase("english")) {
            configuration.locale = Locale.ENGLISH;
        } else {
            configuration.locale = Locale.CHINA;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        resources.updateConfiguration(configuration, metrics);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setTitle(R.string.title_activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // A back arrow shown on ActionBar
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged...");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}