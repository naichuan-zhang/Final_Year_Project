package com.example.imageprocessor.misc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;

import com.example.imageprocessor.R;

import java.util.Locale;
import java.util.Objects;

public class SettingsConfig {

    private Context context;
    private static CurrentMode currentMode;

    public SettingsConfig(Context context) {
        this.context = context;
    }

    private Context getContext() {
        return context;
    }

    public CurrentMode getCurrentMode() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO)
            currentMode = CurrentMode.OFF;
        else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            currentMode = CurrentMode.ON;
        else
            currentMode = CurrentMode.UNKNOWN;
        return currentMode;
    }

    public void darkModeOn() {
        Toast.makeText(getContext(), R.string.dark_mode_on, Toast.LENGTH_SHORT).show();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public void darkModeOff() {
        Toast.makeText(getContext(), R.string.dark_mode_off, Toast.LENGTH_SHORT).show();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void changeLanguage(ListPreference languageListPreference, Activity activity) {
        Resources resources = getContext().getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        int index = languageListPreference.findIndexOfValue(languageListPreference.getValue());
        if (index != -1) {
            Toast.makeText(getContext(), languageListPreference.getEntries()[index], Toast.LENGTH_SHORT).show();
            if (languageListPreference.getValue().equals("english")) {
                configuration.setLocale(Locale.ENGLISH);
            } else if (languageListPreference.getValue().equals("chinese")) {
                configuration.setLocale(Locale.CHINESE);
            }
            resources.updateConfiguration(configuration, displayMetrics);
            refreshSettings(activity);
        }
    }

    private void refreshSettings(Activity activity) {
        // refresh the current activity without a blink
        Objects.requireNonNull(activity).finish();
        activity.overridePendingTransition(0, 0);
        getContext().startActivity(activity.getIntent());
        activity.overridePendingTransition(0, 0);
    }
}
