package com.example.imageprocessor.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.LanguagesDialog;
import com.example.imageprocessor.misc.SettingsConfig;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public final static String KEY_DARK_MODE = "dark_mode";
    public final static String KEY_LANGUAGE = "language";
    private final static String TAG = "SettingsFragment: ";

    public static final String LANGUAGE_SETTING = "lang_setting";
    public static final int LANGUAGE_CHANGED = 1000;

    private SwitchPreferenceCompat darkModeSwitchPreference;
    private ListPreference languageListPreference;
    private Preference languagePreference;

    private SettingsConfig settingsConfig;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate...");

        settingsConfig = new SettingsConfig(getContext());
        darkModeSwitchPreference = findPreference(KEY_DARK_MODE);
//            languageListPreference = findPreference(KEY_LANGUAGE);
        languagePreference = findPreference(KEY_LANGUAGE);
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
//                    settingsConfig.changeLocale(languageListPreference, getActivity());
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume...");
        Objects.requireNonNull(getActivity()).setTitle(R.string.title_activity_settings);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
//        Objects.requireNonNull(findPreference(KEY_LANGUAGE)).setOnPreferenceClickListener(this);
        findPreference(KEY_LANGUAGE).setOnPreferenceClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause...");
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference != null ? preference.getKey() : "";
        if (key.equals(KEY_LANGUAGE)) {
            LanguagesDialog languagesDialog = new LanguagesDialog();
            languagesDialog.show(Objects.requireNonNull(getFragmentManager()), "LanguagesDialogFragment");
            return true;
        }
        return false;
    }
}
