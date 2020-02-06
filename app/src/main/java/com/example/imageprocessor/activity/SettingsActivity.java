package com.example.imageprocessor.activity;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.SettingsConfig;


public class SettingsActivity extends AppCompatActivity {

    private final static String TAG = "SettingsActivity: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final static String KEY_DARK_MODE = "dark_mode";
        private final static String KEY_LANGUAGE = "language";

        private SwitchPreferenceCompat darkModeSwitchPreference;
        private ListPreference languageListPreference;

        private SettingsConfig settingsConfig;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.i(TAG, "onCreate...");

            settingsConfig = new SettingsConfig(getContext());
            darkModeSwitchPreference = findPreference(KEY_DARK_MODE);
            languageListPreference = findPreference(KEY_LANGUAGE);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i(TAG, "onSharedPreferenceChanged -> key: " + key);

            // Actions to take when settings preferences changed
            switch (key) {
                // dark mode
                case KEY_DARK_MODE:
                    boolean on = sharedPreferences.getBoolean(key, false);
                    if (on) {
                        // when dark mode is on
                        settingsConfig.darkModeOn();
                    } else {
                        // when dark mode is off
                        settingsConfig.darkModeOff();
                    }
                // language
                case KEY_LANGUAGE:
                    settingsConfig.changeLocale(languageListPreference, getActivity());
                default:
                    break;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.i(TAG, "onResume...");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.i(TAG, "onPause...");
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}