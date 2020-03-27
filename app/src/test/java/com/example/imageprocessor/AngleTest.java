package com.example.imageprocessor;

import com.example.imageprocessor.ui.preview.PreviewFragment;

import org.junit.Test;
import org.opencv.core.Point;

public class AngleTest {

    @Test
    public void testAngle() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point pt1 = new Point(10, 10);
        Point pt2 = new Point(8, 3);
        Point pt0 = new Point(5, 5);
        System.out.println(fragment.getAngle(pt1, pt2, pt0));
    }

    @Test
    public void testAngle2() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point pt1 = new Point(1, 1);
        Point pt2 = new Point(11, 3);
        Point pt0 = new Point(4.5, 5);
        System.out.println(fragment.getAngle(pt1, pt2, pt0));
    }

    @Test
    public void testAngle3() {
        PreviewFragment fragment = PreviewFragment.newInstance();
        Point pt1 = new Point(6, 6);
        Point pt2 = new Point(6, 0);
        Point pt0 = new Point(0, 0);
        System.out.println(fragment.getAngle(pt1, pt2, pt0));
    }
}
