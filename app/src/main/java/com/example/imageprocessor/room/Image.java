package com.example.imageprocessor.room;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Image implements Parcelable {

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
    // 3 -> others
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

    protected Image(Parcel in) {
        imageID = in.readInt();
        imageName = in.readString();
        imageDate = in.readString();
        imageUri = in.readString();
        imageSource = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageID);
        dest.writeString(imageName);
        dest.writeString(imageDate);
        dest.writeString(imageUri);
        dest.writeInt(imageSource);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

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
