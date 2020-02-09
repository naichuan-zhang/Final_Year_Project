package com.example.imageprocessor.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImages(Image... images);

    @Update
    void updateImages(Image... images);

    @Delete
    void deleteImages(Image... images);

    @Query("SELECT * FROM image WHERE imageUri = :imageUri LIMIT 1")
    LiveData<List<Image>> findImageByUri(String imageUri);

//    @Query("SELECT * FROM image WHERE imageName LIKE :imageName LIMIT 1")
//    LiveData<List<Image>> findImageByName(String imageName);

    // Allow the app to update UI automatically when the data changes
    @Query("SELECT * FROM image")
    LiveData<List<Image>> getAllImages();

    @Query("DELETE FROM image")
    void deleteAllImages();
}
