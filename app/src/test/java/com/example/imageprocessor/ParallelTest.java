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

    @Test
    public void testParallel3() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point p = new Point(110, 184);
        Point q = new Point(52, 456);
        Point r = new Point(442, 455);
        Point s = new Point(381, 184);
        Assert.assertTrue(fragment.isParallel(p, s, q, r));
        Assert.assertFalse(fragment.isParallel(p, q, r, s));
    }

    @Test
    public void test() {
        double x1 = 110, x2 = 381, x3 = 52, x4 = 442;
        double y1 = 184, y2 = 184, y3 = 456, y4 = 455;
        double slopeRS = (y3 - y4) / (x3 - x4);
        double slopePQ = (y1 - y2) / (x1 - x2);

        System.out.println(Math.abs(slopeRS - slopePQ));
    }
}
