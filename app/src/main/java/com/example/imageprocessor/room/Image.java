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

    // 1 -> gallery image
    // 2 -> camera image
    @ColumnInfo(name = "imageSource")
    private int imageSource;

    public Image(int imageID, String imageName, String imageDate, String imageUri, int imageSource) {
        this.imageID = imageID;
        this.imageName = imageName;
        this.imageDate = imageDate;
        this.imageUri = imageUri;
        this.imageSource = imageSource;
    }

    @Ignore
    public Image(String imageName, String imageDate, String imageUri, int imageSource) {
        this.imageName = imageName;
        this.imageDate = imageDate;
        this.imageUri = imageUri;
        this.imageSource = imageSource;
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

    public int getImageSource() {
        return imageSource;
    }
}
