package com.example.imageprocessor.misc;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class Utility {

    public static Bitmap fixImageOrientation(Context context, Bitmap bitmap,
                                             String imageUri, String imageFrom) throws IOException {
        if (imageFrom.equalsIgnoreCase("GALLERY")) {
            Bitmap rotatedImage;
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int orientation = Objects.requireNonNull(exif).getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {

                case ExifInterface.ORIENTATION_NORMAL:

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedImage = rotateBitmap(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedImage = rotateBitmap(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedImage = rotateBitmap(bitmap, 270);
                    break;

                default:
                    rotatedImage = bitmap;
                    break;
            }
            return rotatedImage;

        } else if (imageFrom.equalsIgnoreCase("CAMERA")) {
            Uri uri = Uri.parse(imageUri);
            InputStream input = context.getContentResolver().openInputStream(uri);
            ExifInterface exif;
            if (Build.VERSION.SDK_INT > 23)
                exif = new ExifInterface(input);
            else
                exif = new ExifInterface(uri.getPath());

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return Utility.rotateBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return Utility.rotateBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return Utility.rotateBitmap(bitmap, 270);
                default:
                    return bitmap;
            }

        } else
            return null;
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, true);
    }

    public static String getRealPathFromUri(Context context, Uri imageUri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(imageUri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
}
