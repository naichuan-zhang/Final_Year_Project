package com.example.imageprocessor.room;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ImageRepository {

    private ImageDao imageDao;
    private LiveData<List<Image>> allImages;

    public ImageRepository(Application application) {
        ImageDatabase database = ImageDatabase.getInstance(application);
        imageDao = database.imageDao();
        allImages = imageDao.getAllImages();
    }

    public void insertImages(Image... images) {
        new InsertImageAsyncTask(imageDao).execute(images);
    }

    public void updateImages(Image... images) {
        new UpdateImageAsyncTask(imageDao).execute(images);
    }

    public void deleteImages(Image... images) {
        new DeleteImageAsyncTask(imageDao).execute(images);
    }

    // TODO: NOT SURE IF IT WORKS!!!
    public LiveData<List<Image>> findImageByUri(String imageUri) {
        return imageDao.findImageByUri(imageUri);
    }

    public LiveData<List<Image>> getAllImages() {
        return allImages;
    }

    public void deleteAllImages() {
        new DeleteAllImagesAsyncTask(imageDao).execute();
    }

    private static class InsertImageAsyncTask extends AsyncTask<Image, Void, Void> {
        private ImageDao imageDao;

        private InsertImageAsyncTask(ImageDao imageDao) {
            this.imageDao = imageDao;
        }

        @Override
        protected Void doInBackground(Image... images) {
            imageDao.insertImages(images);
            return null;
        }
    }

    private static class UpdateImageAsyncTask extends AsyncTask<Image, Void, Void> {
        private ImageDao imageDao;

        private UpdateImageAsyncTask(ImageDao imageDao) {
            this.imageDao = imageDao;
        }

        @Override
        protected Void doInBackground(Image... images) {
            imageDao.updateImages(images);
            return null;
        }
    }

    private static class DeleteImageAsyncTask extends AsyncTask<Image, Void, Void> {
        private ImageDao imageDao;

        private DeleteImageAsyncTask(ImageDao imageDao) {
            this.imageDao = imageDao;
        }

        @Override
        protected Void doInBackground(Image... images) {
            imageDao.deleteImages(images);
            return null;
        }
    }

    private static class DeleteAllImagesAsyncTask extends AsyncTask<Void, Void, Void> {
        private ImageDao imageDao;

        private DeleteAllImagesAsyncTask(ImageDao imageDao) {
            this.imageDao = imageDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            imageDao.deleteAllImages();
            return null;
        }
    }
}
