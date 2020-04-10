package com.example.imageprocessor;

import android.os.Bundle;
import android.os.Parcel;

import com.example.imageprocessor.room.Image;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class ImageTest {

    @Mock
    Image image;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsNotNull() {
        Assert.assertNotNull(image);
    }

    @Test
    public void testImageReturn() {
        when(image.getImageName()).thenReturn("Image Name");
        when(image.getImageUri()).thenReturn("Image Uri");
        System.out.println(image.getImageName());
        System.out.println(image.getImageUri());
    }
}
