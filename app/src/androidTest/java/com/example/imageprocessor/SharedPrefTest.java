package com.example.imageprocessor;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.imageprocessor.misc.DarkModeSharedPref;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SharedPrefTest {

    DarkModeSharedPref sharedPref;

    @Before
    public void setUp() {
        sharedPref = new DarkModeSharedPref(
                ApplicationProvider.getApplicationContext());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSharedPref() {
        sharedPref.setDarkModeState(true);
        Assert.assertTrue(sharedPref.loadDarkModeState());
    }
}
