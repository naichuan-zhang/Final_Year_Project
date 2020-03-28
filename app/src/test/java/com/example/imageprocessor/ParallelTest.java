package com.example.imageprocessor;

import com.example.imageprocessor.ui.preview.PreviewFragment;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Point;

public class ParallelTest {
    @Test
    public void testParallel() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point p = new Point(2, 5);
        Point q = new Point(6, 4);
        Point r = new Point(8, 3);
        Point s = new Point(9, 7);
        Assert.assertFalse(fragment.isParallel(p, q, r, s));
    }

    @Test
    public void testParallel2() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point p = new Point(5, 6);
        Point q = new Point(4, 2);
        Point r = new Point(5, 3);
        Point s = new Point(6, 7);
        Assert.assertTrue(fragment.isParallel(p, q, r, s));
    }
}
