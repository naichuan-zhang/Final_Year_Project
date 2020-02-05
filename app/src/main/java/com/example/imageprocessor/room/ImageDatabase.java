package com.example.imageprocessor.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Image.class}, version = 1, exportSchema = false)
public abstract class ImageDatabase extends RoomDatabase {

    private static final String DB_NAME = "image_db";
    private static ImageDatabase instance;

    public static synchronized ImageDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    ImageDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public abstract ImageDao imageDao();
}
