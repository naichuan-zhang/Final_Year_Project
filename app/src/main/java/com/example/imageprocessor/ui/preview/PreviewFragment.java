package com.example.imageprocessor.ui.preview;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.OpenCVUtil;
import com.example.imageprocessor.misc.Utility;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PreviewFragment extends Fragment
        implements SeekBar.OnSeekBarChangeListener,
        CompoundButton.OnCheckedChangeListener {

    private final static String TAG = "PreviewFragment: ";

    private final static int THRESH_RATIO = 2;
    private final static double EPS = 1E-13;

    private PreviewViewModel previewViewModel;
    private View root;

    private ImageView previewImageView;

    private CheckBox triangleCheckBox;
    private CheckBox quadrangleCheckBox;
    private CheckBox pentagonCheckBox;
    private CheckBox circleCheckBox;

    private SeekBar threshSeekBar;

    // 1 -> gallery, 2 -> camera, 3 -> others
    private int from;
    private Uri uri;

    // to show detect results
    private Bitmap originalBitmap = null;
    private Bitmap bitmap;
    private Bitmap outputBitmap;
    private Mat srcMat;
    private Mat outputMat;

    public static PreviewFragment newInstance() {
        return new PreviewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.preview_fragment, container, false);
        previewImageView = root.findViewById(R.id.previewImageView);

        triangleCheckBox = root.findViewById(R.id.triangleCheckBox);
        quadrangleCheckBox = root.findViewById(R.id.quadrangleCheckBox);
        pentagonCheckBox = root.findViewById(R.id.pentagonCheckBox);
        circleCheckBox = root.findViewById(R.id.circleCheckBox);
        triangleCheckBox.setOnCheckedChangeListener(this);
        quadrangleCheckBox.setOnCheckedChangeListener(this);
        pentagonCheckBox.setOnCheckedChangeListener(this);
        circleCheckBox.setOnCheckedChangeListener(this);

        threshSeekBar = root.findViewById(R.id.threshSeekBar);
        threshSeekBar.setOnSeekBarChangeListener(this);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        previewViewModel =
                ViewModelProviders.of(this).get(PreviewViewModel.class);

        uri = Uri.parse(Objects.requireNonNull(getArguments()).getString("uri"));
        from = getArguments().getInt("from");

        // show original bitmap
        try {
            originalBitmap = Utility.getBitmap(uri, getContext(), from);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bitmap = Objects.requireNonNull(originalBitmap)
                .copy(originalBitmap.getConfig(), true);
        outputBitmap = Objects.requireNonNull(originalBitmap)
                .copy(originalBitmap.getConfig(), true);

        if (originalBitmap != null) {
            previewImageView.setImageBitmap(originalBitmap);
        }

        // set init threshold
        threshSeekBar.setProgress(255 / 2);
        changeThresh(255 / 2);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // preprocessing
        srcMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, srcMat);
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.blur(srcMat, srcMat, new Size(3, 3));

        switch (buttonView.getId()) {
            case R.id.triangleCheckBox:
                if (isChecked)
                    findTriangles();
                else
                    previewImageView.setImageBitmap(bitmap);
            case R.id.quadrangleCheckBox:
                if (isChecked)
                    findQuadrangles();
                else
                    previewImageView.setImageBitmap(bitmap);
            case R.id.pentagonCheckBox:
                if (isChecked)
                    findPentagons();
                else
                    previewImageView.setImageBitmap(bitmap);
            case R.id.circleCheckBox:
                if (isChecked)
                    findCircles();
                else
                    previewImageView.setImageBitmap(bitmap);
            default:
                break;
        }
    }

    private void findTriangles() {
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(srcMat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);
            int vertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(contour);
            if (Math.abs(contourArea) > 500) {
                if (vertices == 3) {
                    Point center = findCenter(contour);
                    Point p1 = approxCurve.toArray()[0];
                    Point p2 = approxCurve.toArray()[1];
                    Point p3 = approxCurve.toArray()[2];
                    double d12 = getDistance(p1, p2);
                    double d13 = getDistance(p1, p3);
                    double d23 = getDistance(p2, p3);
                    Toast.makeText(getContext(), d12 + " " + d13 + " " + d23, Toast.LENGTH_SHORT).show();
                    if (Math.abs(d12 - d13) <= 2 || Math.abs(d12 - d23) <= 2 || Math.abs(d13 - d23) <= 2) {
                        if (Math.abs(d12 - d13) <= 2 && Math.abs(d13 - d23) <= 2)
                            putLabel("Equilateral", center);
                        else
                            putLabel("Isosceles", center);
                    } else
                        putLabel("Triangle", center);
                }
            }
        }
        // TODO: change to outputMat
        Utils.matToBitmap(srcMat, outputBitmap);
        previewImageView.setImageBitmap(outputBitmap);
    }

    private void findQuadrangles() {
    }

    private void findPentagons() {
    }

    private void findCircles() {
    }

    private Point findCenter(MatOfPoint contour) {
        Moments moments = Imgproc.moments(contour);
        Point center = new Point();
        center.x = moments.get_m10() / moments.get_m00();
        center.y = moments.get_m01() / moments.get_m00();
        // TODO: change to outputMat
        Imgproc.circle(srcMat, center, 3, new Scalar(255, 0, 0));
        return center;
    }

    private double getDistance(Point p1, Point p2) {
        double distance = 0.0;
        if (p1 != null && p2 != null) {
            double xDiff = p1.x - p2.x;
            double yDiff = p1.y - p2.y;
            distance = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
        }
        return distance;
    }

    private void putLabel(String text, Point org) {
        // TODO: change to outputMat
        Imgproc.putText(srcMat, text, org, Core.FONT_HERSHEY_COMPLEX, 0.5, new Scalar(255, 0, 0), 2);
    }

    private void changeThresh(int thresh) {
        Mat src = new Mat(originalBitmap.getHeight(), originalBitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(originalBitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.blur(src, src, new Size(3, 3));
        Imgproc.Canny(src, src, thresh, thresh * THRESH_RATIO);
        // write the updated Mat to bitmap
        Utils.matToBitmap(src, bitmap);
//        previewImageView.setImageBitmap(bitmap);
        Log.i(TAG, String.valueOf(thresh));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        changeThresh(progress);

        triangleCheckBox.setChecked(false);
        quadrangleCheckBox.setChecked(false);
        pentagonCheckBox.setChecked(false);
        circleCheckBox.setChecked(false);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Do nothing
    }
}
