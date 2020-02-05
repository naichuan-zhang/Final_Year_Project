package com.example.imageprocessor;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.imageprocessor.room.ImageDao;
import com.example.imageprocessor.room.ImageDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private ImageDao imageDao;
    private ImageDatabase database;

    @Before
    public void initDatabase() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context,
                ImageDatabase.class)
                .build();
        imageDao = database.imageDao();
    }

    @After
    public void closeDatabase() throws Exception {
        database.close();
    }

    @Test
    public void insertAnImageAndReadInList() throws Exception {
        // insert an image
    }
}
