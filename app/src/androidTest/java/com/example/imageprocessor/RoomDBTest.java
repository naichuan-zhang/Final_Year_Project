package com.example.imageprocessor;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.imageprocessor.room.Image;
import com.example.imageprocessor.room.ImageDao;
import com.example.imageprocessor.room.ImageDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RoomDBTest {

    private ImageDao imageDao;
    private ImageDatabase database;
    private Observer<List<Image>> observer;

    @Before
    public void createDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, ImageDatabase.class)
                        .allowMainThreadQueries().build();
        imageDao = database.imageDao();
    }

    @After
    public void closeDatabase() {
        database.close();
    }

    @Test
    public void testInsertImage() {
        String uri = "Image Uri";
        Image image = new Image("Image Name", "2020-01-01 01:00:00", uri, 1);
//        imageDao.getAllImages().observeForever(observer);
        imageDao.insertImages(image);
        LiveData<List<Image>> imageByUri = imageDao.findImageByUri(uri);
        List<Image> images = imageByUri.getValue();
        assertThat(images.get(0), equalTo(image));
    }
}
