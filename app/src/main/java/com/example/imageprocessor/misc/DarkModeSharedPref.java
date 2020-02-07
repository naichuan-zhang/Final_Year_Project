package com.example.imageprocessor.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DarkModeSharedPref {

    private SharedPreferences sharedPreferences;

    public DarkModeSharedPref(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Boolean loadDarkModeState() {
        return sharedPreferences.getBoolean("dark_mode", false);
    }

    @Deprecated
    public void setDarkModeState(Boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("dark_mode", state);
        editor.apply();
    }
}
