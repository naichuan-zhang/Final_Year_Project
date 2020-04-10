package com.example.imageprocessor;

import android.os.Bundle;
import android.os.Parcel;

import com.example.imageprocessor.room.Image;

import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class ParcelableTest {

    @Test
    public void testImageParcelable() {
        Image image = new Image("Image Name", "2020-01-01",
                "Image Uri", 1);
        Bundle bundle = new Bundle();
        bundle.putParcelable("image", image);

        Parcel parcel = Parcel.obtain();
        bundle.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        Bundle bundle2 = parcel.readBundle();
        Objects.requireNonNull(bundle2)
                .setClassLoader(Image.class.getClassLoader());

        Image image2 = bundle2.getParcelable("image");

        Assert.assertNotSame("Bundle is the same", bundle, bundle2);
        Assert.assertNotSame("Image is the same", image, image2);
        Assert.assertNotEquals(image, image2);
    }
}
