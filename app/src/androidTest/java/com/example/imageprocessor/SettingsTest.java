package com.example.imageprocessor;

import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.imageprocessor.activity.SettingsActivity;
import com.example.imageprocessor.ui.settings.SettingsFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.PreferenceMatchers.withKey;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@RunWith(AndroidJUnit4.class)
public class SettingsTest {

    @Rule
    public ActivityTestRule<SettingsActivity> activityTestRule =
            new ActivityTestRule<>(SettingsActivity.class);

    @Test
    public void testSettings() {
        activityTestRule.getActivity().runOnUiThread(() -> {
            SettingsFragment settingsFragment = startSettingsFragment();
        });

        onView(withId(R.id.settings)).check(matches(isCompletelyDisplayed()));
        onData(allOf(is(instanceOf(Preference.class)),
                withKey("dark_mode")))
                .check(matches(isCompletelyDisplayed()));
    }

    private SettingsFragment startSettingsFragment() {
        SettingsActivity activity = activityTestRule.getActivity();
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        SettingsFragment settingsFragment = new SettingsFragment();
        transaction.replace(R.id.settings, settingsFragment, "settingsFragment");
        transaction.commit();
        return settingsFragment;
    }
}
