package com.example.imageprocessor.misc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class Utility {

    private final static String TAG = "Utility: ";

    private final static String[] cameraPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final static String[] galleryPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static Bitmap getBitmap(Uri imageUri, Context context, int from) throws IOException {
        // system gallery or others
        if (from == 1 || from == 3) {
            Log.i(TAG, "URI: " + imageUri + ", FROM: " + from);
            String filePath = Utility.getRealPathFromUri(context, imageUri);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            bmOptions.inDither = true;

            BitmapFactory.decodeFile(filePath, bmOptions);

            /* Figure out which way needs to be reduced less */
            int scaleFactor = 1;

            /* Set bitmap options to scale the image decode target */
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            /* Decode the JPEG file into a Bitmap */
            Bitmap rawBitmap = BitmapFactory.decodeFile(filePath, bmOptions);

            // rotate bitmap when necessary

            return Utility.fixImageOrientation(context, rawBitmap, filePath, from);

            // system camera
        } else if (from == 2) {
            Log.i(TAG, "URI: " + imageUri + ", FROM: " + from);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            ContentResolver contentResolver = context.getContentResolver();
            InputStream input;
            InputStream input1;
            try {
                input = contentResolver.openInputStream(imageUri);
                BitmapFactory.decodeStream(input, null, options);
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            int width = options.outWidth;
            int height = options.outHeight;
            Bitmap bitmap = null;
            try {
                input1 = contentResolver.openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(input1);
                bitmap = Utility.fixImageOrientation(context, bitmap, imageUri.toString(), from);
                if (input1 != null) {
                    input1.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }
        return null;
    }

    public static Bitmap fixImageOrientation(Context context, Bitmap bitmap,
                                             String imageUri, int imageFrom) throws IOException {
        if (imageFrom == 1 || imageFrom == 3) {
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

        } else if (imageFrom == 2) {
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
        }
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

    public static boolean checkAndAskCameraPermissions(Context context, Activity activity) {
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : cameraPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(permission);
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    Constants.REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    public static boolean checkAndAskGalleryPermissions(Context context, Activity activity) {
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : galleryPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(permission);
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    Constants.REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    public static String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(calendar.getTime());
    }

    public static AlertDialog showAlertDialog(Context context, String title, String message,
                                       String positiveText, DialogInterface.OnClickListener positiveListener,
                                       String negativeText, DialogInterface.OnClickListener negativeListener,
                                       boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setCancelable(isCancelable)
                .setMessage(message)
                .setPositiveButton(positiveText, positiveListener)
                .setNegativeButton(negativeText,negativeListener);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}
