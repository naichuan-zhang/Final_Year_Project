package com.example.imageprocessor;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.imageprocessor.room.Image;
import com.example.imageprocessor.room.ImageDao;
import com.example.imageprocessor.room.ImageDatabase;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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
        Image image = new Image("test", "2019/12/12", "image_uri", 1);
        database.imageDao().insertImages(image);
        LiveData<List<Image>> images = database.imageDao().getAllImages();
        Image dbImage = images.getValue().get(0);
        Assert.assertEquals(dbImage.getImageID(), image.getImageID());
        Assert.assertEquals(dbImage.getImageName(), image.getImageName());
    }
}
