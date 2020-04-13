package com.example.imageprocessor.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LanguageSharedPref {

    private SharedPreferences sharedPreferences;

    public LanguageSharedPref(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String loadLanguageState() {
        return sharedPreferences.getString("language", "english");
    }

    public void setLanguageState(String state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", state);
        editor.apply();
    }
}
