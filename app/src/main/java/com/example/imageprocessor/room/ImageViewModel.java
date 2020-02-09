package com.example.imageprocessor.room;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ImageViewModel extends AndroidViewModel {

    private ImageRepository repository;
    private LiveData<List<Image>> allImages;

    public ImageViewModel(@NonNull Application application) {
        super(application);
        repository = new ImageRepository(application);
        allImages = repository.getAllImages();
    }

    public void insertImages(Image... images) {
        repository.insertImages(images);
    }

    public void updateImages(Image... images) {
        repository.updateImages(images);
    }

    public void deleteImages(Image... images) {
        repository.deleteImages(images);
    }

    public void deleteAllImages() {
        repository.deleteAllImages();
    }

    public LiveData<List<Image>> getAllImages() {
        return allImages;
    }
}
