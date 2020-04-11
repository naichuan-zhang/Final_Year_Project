package com.example.imageprocessor;

import com.example.imageprocessor.ui.preview.PreviewFragment;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Point;

public class DistanceTest {

    @Test
    public void testDistance() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point p1 = new Point(3, 4);
        Point p2 = new Point(7, 7);
        double distance = fragment.getDistance(p1, p2);
        Assert.assertEquals(5, distance, 0);
    }

    @Test
    public void testDistance2() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point p1 = new Point(3, 4);
        Point p2 = new Point(4, 3);
        double distance = fragment.getDistance(p1, p2);
        Assert.assertEquals(1.5, distance, 0.1);
    }

    @Test
    public void testDistance3() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point p1 = new Point(3, 4);
        Point p2 = new Point(7, 1);
        double distance = fragment.getDistance(p1, p2);
        Assert.assertEquals(5, distance, 0.001);
    }
}
