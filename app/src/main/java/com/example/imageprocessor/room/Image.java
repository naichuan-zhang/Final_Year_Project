package com.example.imageprocessor.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Image {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "imageID")
    private int imageID;

    @ColumnInfo(name = "imageName")
    private String imageName;

    @ColumnInfo(name = "imageDate")
    private String imageDate;

    @ColumnInfo(name = "imageUri")
    private String imageUri;


    public Image(int imageID, String imageName, String imageDate, String imageUri) {
        this.imageID = imageID;
        this.imageName = imageName;
        this.imageDate = imageDate;
        this.imageUri = imageUri;
    }

    @Ignore
    public Image(String imageName, String imageDate, String imageUri) {
        this.imageName = imageName;
        this.imageDate = imageDate;
        this.imageUri = imageUri;
    }

    public int getImageID() {
        return imageID;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageDate() {
        return imageDate;
    }

    public String getImageUri() {
        return imageUri;
    }
}
