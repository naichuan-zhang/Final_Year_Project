package com.example.imageprocessor.misc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.imageprocessor.R;
import com.example.imageprocessor.ui.settings.SettingsFragment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Reference:
 *      @link https://www.javarticles.com/2015/08/android-change-locale-dynamically.html
 */
public class LanguagesDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Map<String, String> localeMap = new HashMap<>();
        String[] availableLanguages = getResources().getStringArray(R.array.languages);
        final String[] values = new String[availableLanguages.length + 1];

        for (int i = 0; i < availableLanguages.length; i++) {
            String localeString = availableLanguages[i];
            if (localeString.contains("-"))
                localeString = localeString.substring(0, localeString.indexOf("-"));
            Locale locale = new Locale(localeString);
            values[i + 1] = locale.getDisplayLanguage() + " ("
                    + availableLanguages[i] + ")";
            localeMap.put(values[i + 1], availableLanguages[i]);
        }
        values[0] = Objects.requireNonNull(getActivity()).getString(R.string.device) + " ("
                + Locale.getDefault().getLanguage() + ")";
        localeMap.put(values[0], Locale.getDefault().getLanguage());
        Arrays.sort(values, 1, values.length);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.language_title));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        ListView listView = new ListView(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Resources resources = getResources();
                DisplayMetrics metrics = resources.getDisplayMetrics();
                Configuration configuration = resources.getConfiguration();
                String localeString = localeMap.get(values[position]);
                if (Objects.requireNonNull(localeString).contains("-")) {
                    configuration.locale = new Locale(localeString.substring(0, localeString.indexOf("-")),
                            localeString.substring(localeString.indexOf("-") + 1, localeString.length()));
                } else {
                    configuration.locale = new Locale(localeString);
                }
                resources.updateConfiguration(configuration, metrics);
                settings.edit().putString(SettingsFragment.LANGUAGE_SETTING, localeString).apply();

                Intent refresh = new Intent(getActivity(), Objects.requireNonNull(getActivity()).getClass());
                // For smooth transition
                getActivity().overridePendingTransition(0, 0);
                startActivity(refresh);
                getActivity().setResult(SettingsFragment.LANGUAGE_CHANGED);
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
            }
        });

        builder.setView(listView);
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
