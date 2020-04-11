package com.example.imageprocessor.ui.preview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.imageprocessor.misc.Utility;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.opencv.imgproc.Imgproc.CV_CONTOURS_MATCH_I1;
import static org.opencv.imgproc.Imgproc.matchShapes;

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
    private CheckBox hexagonCheckBox;
    private CheckBox circleCheckBox;
    private CheckBox ellipseCheckBox;

    private SeekBar threshSeekBar;

    private Button logButton;

    // 1 -> gallery, 2 -> camera, 3 -> others
    private int from;
    private Uri uri;

    // to show detect results
    private Bitmap originalBitmap = null;
    private Bitmap initBitmap = null;
    private Bitmap bitmap;
    private Bitmap outputBitmap;

    private Mat srcMat;
    private Mat outputMat;

    private StringBuilder detectResult = new StringBuilder();

    public PreviewFragment() {
    }


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
        hexagonCheckBox = root.findViewById(R.id.hexagonCheckBox);
        circleCheckBox = root.findViewById(R.id.circleCheckBox);
        ellipseCheckBox = root.findViewById(R.id.ellipseCheckBox);

        triangleCheckBox.setOnCheckedChangeListener(this);
        quadrangleCheckBox.setOnCheckedChangeListener(this);
        pentagonCheckBox.setOnCheckedChangeListener(this);
        hexagonCheckBox.setOnCheckedChangeListener(this);
        circleCheckBox.setOnCheckedChangeListener(this);
        ellipseCheckBox.setOnCheckedChangeListener(this);

        threshSeekBar = root.findViewById(R.id.threshSeekBar);
        threshSeekBar.setOnSeekBarChangeListener(this);

        logButton = root.findViewById(R.id.logButton);
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
            initBitmap = Utility.getBitmap(uri, getContext(), from);
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

        // init outputMat to show detect results
        outputMat = new Mat();
        Utils.bitmapToMat(originalBitmap, outputMat);

        // set init threshold
        threshSeekBar.setProgress(255 / 2);
        changeThresh(255 / 2);

        // show log
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message;
                if (detectResult == null || detectResult.equals("")) {
                    message = "No output log";
                } else {
                    message = detectResult.toString();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.output_log))
                        .setMessage(message)
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // show original bitmap when a checkbox is unchecked
        previewImageView.setImageBitmap(originalBitmap);

        // preprocessing
        srcMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, srcMat);
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.blur(srcMat, srcMat, new Size(3, 3));

        int id = buttonView.getId();
        switch (id) {
            case R.id.triangleCheckBox:
                if (isChecked) {
                    clearOutputs();
                    triangleCheckBox.setChecked(true);
                    quadrangleCheckBox.setChecked(false);
                    pentagonCheckBox.setChecked(false);
                    hexagonCheckBox.setChecked(false);
                    circleCheckBox.setChecked(false);
                    ellipseCheckBox.setChecked(false);
                    findTriangles();
                } break;
            case R.id.quadrangleCheckBox:
                if (isChecked) {
                    clearOutputs();
                    triangleCheckBox.setChecked(false);
                    quadrangleCheckBox.setChecked(true);
                    pentagonCheckBox.setChecked(false);
                    hexagonCheckBox.setChecked(false);
                    circleCheckBox.setChecked(false);
                    ellipseCheckBox.setChecked(false);
                    findQuadrangles();
                } break;
            case R.id.pentagonCheckBox:
                if (isChecked) {
                    clearOutputs();
                    triangleCheckBox.setChecked(false);
                    quadrangleCheckBox.setChecked(false);
                    pentagonCheckBox.setChecked(true);
                    hexagonCheckBox.setChecked(false);
                    circleCheckBox.setChecked(false);
                    ellipseCheckBox.setChecked(false);
                    findPentagons();
                } break;
            case R.id.hexagonCheckBox:
                if (isChecked) {
                    clearOutputs();
                    triangleCheckBox.setChecked(false);
                    quadrangleCheckBox.setChecked(false);
                    pentagonCheckBox.setChecked(false);
                    hexagonCheckBox.setChecked(true);
                    circleCheckBox.setChecked(false);
                    ellipseCheckBox.setChecked(false);
                    findHexagons();
                } break;
            case R.id.circleCheckBox:
                if (isChecked) {
                    clearOutputs();
                    triangleCheckBox.setChecked(false);
                    quadrangleCheckBox.setChecked(false);
                    pentagonCheckBox.setChecked(false);
                    hexagonCheckBox.setChecked(false);
                    circleCheckBox.setChecked(true);
                    ellipseCheckBox.setChecked(false);
                    findCircles();
                } break;
            case R.id.ellipseCheckBox:
                if (isChecked) {
                    clearOutputs();
                    triangleCheckBox.setChecked(false);
                    quadrangleCheckBox.setChecked(false);
                    pentagonCheckBox.setChecked(false);
                    hexagonCheckBox.setChecked(false);
                    circleCheckBox.setChecked(false);
                    ellipseCheckBox.setChecked(true);
                    findEllipses();
                }
            default:
                break;
        }
    }

    private void clearOutputs() {
        Utils.bitmapToMat(originalBitmap, outputMat);
        // clear up detect results
        detectResult.delete(0, detectResult.length());
    }

    private void findTriangles() {
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(srcMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
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
                    if (Math.abs(d12 - d13) <= 10 || Math.abs(d12 - d23) <= 10 || Math.abs(d13 - d23) <= 10) {
                        if (Math.abs(d12 - d13) <= 10 && Math.abs(d13 - d23) <= 10) {
                            putLabel("Equilateral", center);
                            Log.i(TAG, "Equilateral Triangle detected");
                            detectResult.append("Equilateral Triangle detected\n");
                        } else {
                            putLabel("Isosceles", center);
                            Log.i(TAG, "Isosceles Triangle detected");
                            detectResult.append("Isosceles Triangle detected\n");
                        }
                    } else {
                        putLabel("Scalene", center);
                        Log.i(TAG, "Scalene Triangle detected");
                        detectResult.append("Scalene Triangle detected\n");
                    }
                }
            }
        }
        Utils.matToBitmap(outputMat, outputBitmap);
        previewImageView.setImageBitmap(outputBitmap);
    }

    /**
     * Order:
     *     Square -> Rectangle -> Rhombus -> Parallelogram -> Trapezoid -> General
     */
    private void findQuadrangles() {
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(srcMat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            Rect rect = Imgproc.boundingRect(contour);
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);
            int vertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(contour);
            if (Math.abs(contourArea) > 500) {
                if (vertices == 4) {
                    Point center = findCenter(contour);
                    List<Double> angles = new ArrayList<>();
                    List<Double> distances = new ArrayList<>();

                    angles.add(getAngle(approxCurve.toArray()[2], approxCurve.toArray()[0], approxCurve.toArray()[1]));
                    angles.add(getAngle(approxCurve.toArray()[3], approxCurve.toArray()[1], approxCurve.toArray()[2]));
                    angles.add(getAngle(approxCurve.toArray()[0], approxCurve.toArray()[2], approxCurve.toArray()[3]));
                    angles.add(getAngle(approxCurve.toArray()[1], approxCurve.toArray()[3], approxCurve.toArray()[0]));

                    distances.add(getDistance(approxCurve.toArray()[0], approxCurve.toArray()[1]));
                    distances.add(getDistance(approxCurve.toArray()[1], approxCurve.toArray()[2]));
                    distances.add(getDistance(approxCurve.toArray()[2], approxCurve.toArray()[3]));
                    distances.add(getDistance(approxCurve.toArray()[3], approxCurve.toArray()[0]));
                    Collections.sort(angles);
                    Collections.sort(distances);
                    double minAngle = angles.get(0);
                    double maxAngle = angles.get(angles.size() - 1);
                    // four angles = 90
                    if (minAngle >= 85 && maxAngle <= 95) {
                        /*
                          判定正方形和长方形
                         */
                        // check if four edges are equal
                        if (Math.abs(distances.get(0) - distances.get(1)) <= 10
                            && Math.abs(distances.get(1) - distances.get(2)) <= 10
                            && Math.abs(distances.get(2) - distances.get(3)) <= 10
                            && Math.abs(distances.get(3) - distances.get(0)) <= 10) {
                            putLabel("Square", center);
                            Log.i(TAG, "Square detected");
                            detectResult.append("Square detected\n");
                        } else {
                            putLabel("Rectangle", center);
                            Log.i(TAG, "Rectangle detected");
                            detectResult.append("Rectangle detected\n");
                        }

                    // 两个锐角，两个钝角
                    } else if (minAngle < 90 && maxAngle > 90) {
                        /*
                          判定平行四边形，等腰梯形，菱形，Kite
                         */
                        // 两对对角或邻角都相等 - 至少有两对！
                        if ((Math.abs(angles.get(0) - angles.get(1)) <= 5 && Math.abs(angles.get(2) - angles.get(3)) <= 5)
                                || (Math.abs(angles.get(0) - angles.get(2)) <= 5 && Math.abs(angles.get(1) - angles.get(3)) <= 5)
                                || (Math.abs(angles.get(0) - angles.get(3)) <= 5 && Math.abs(angles.get(1) - angles.get(2)) <= 5)) {
                            /*
                              判定菱形，平行四边形，等腰梯形
                             */
                            // check if four edges are equal
                            if (Math.abs(distances.get(0) - distances.get(1)) <= 5
                                    && Math.abs(distances.get(1) - distances.get(2)) <= 5
                                    && Math.abs(distances.get(2) - distances.get(3)) <= 5
                                    && Math.abs(distances.get(3) - distances.get(0)) <= 5) {
                                putLabel("Rhombus", center);
                                Log.i(TAG, "Rhombus detected");
                                detectResult.append("Rhombus detected\n");
                            } else {
                                // 有两对对边都平行
                                if ((isParallel(approxCurve.toArray()[0], approxCurve.toArray()[1], approxCurve.toArray()[2], approxCurve.toArray()[3])
                                        && isParallel(approxCurve.toArray()[0], approxCurve.toArray()[3], approxCurve.toArray()[2], approxCurve.toArray()[1]))
                                    /*|| (isParallel(approxCurve.toArray()[0], approxCurve.toArray()[3], approxCurve.toArray()[2], approxCurve.toArray()[1])
                                        && isParallel(approxCurve.toArray()[0], approxCurve.toArray()[2], approxCurve.toArray()[1], approxCurve.toArray()[3]))*/) {
                                    putLabel("Parallelogram", center);
                                    Log.i(TAG, "Parallelogram detected");
                                    detectResult.append("Parallelogram detected\n");

                                // 只有一对对边平行
                                } else if ((!isParallel(approxCurve.toArray()[0], approxCurve.toArray()[1], approxCurve.toArray()[2], approxCurve.toArray()[3])
                                        && isParallel(approxCurve.toArray()[0], approxCurve.toArray()[3], approxCurve.toArray()[2], approxCurve.toArray()[1]))
                                      || (isParallel(approxCurve.toArray()[0], approxCurve.toArray()[1], approxCurve.toArray()[2], approxCurve.toArray()[3])
                                        && !isParallel(approxCurve.toArray()[0], approxCurve.toArray()[3], approxCurve.toArray()[2], approxCurve.toArray()[1]))) {
                                    putLabel("Isosceles Trapezoid", center);
                                    Log.i(TAG, "Isosceles Trapezoid detected");
                                    detectResult.append("Isosceles Trapezoid detected\n");

                                // 没有对边平行
                                } else {
                                    putLabel("Irregular", center);
                                    Log.i(TAG, "Irregular Quadrangle 1 detected");
                                    detectResult.append("Irregular Quadrangle1 detected\n");
                                }
                            }

                        // 只有其中一对对角相等
                        } else if (Math.abs(angles.get(0) - angles.get(1)) <= 10 || Math.abs(angles.get(0) - angles.get(2)) <= 10 ||
                                Math.abs(angles.get(0) - angles.get(3)) <= 10 || Math.abs(angles.get(1) - angles.get(2)) <= 10 ||
                                Math.abs(angles.get(1) - angles.get(3)) <= 10 || Math.abs(angles.get(2) - angles.get(3)) <= 10) {
                            // 两对临边都相等
                            if ((Math.abs(distances.get(0) - distances.get(1)) <= 10 && Math.abs(distances.get(2) - distances.get(3)) <= 10)
                                || (Math.abs(distances.get(0) - distances.get(2)) <= 10 && Math.abs(distances.get(1) - distances.get(3)) <= 10)
                                || (Math.abs(distances.get(0) - distances.get(3)) <= 10 && Math.abs(distances.get(1) - distances.get(2)) <= 10)) {
                                putLabel("Kite", center);
                                Log.i(TAG, "Kite detected");
                                detectResult.append("Kite detected\n");
                            // 有一对直角，并且有一对对边相互平行
                            } else if (isParallel(approxCurve.toArray()[0], approxCurve.toArray()[1], approxCurve.toArray()[2], approxCurve.toArray()[3])
                                    || isParallel(approxCurve.toArray()[1], approxCurve.toArray()[2], approxCurve.toArray()[0], approxCurve.toArray()[3])
                                    || isParallel(approxCurve.toArray()[1], approxCurve.toArray()[3], approxCurve.toArray()[0], approxCurve.toArray()[2])) {
                                putLabel("Trapezoid", center);
                                Log.i(TAG, "Trapezoid detected");
                                detectResult.append("Trapezoid detected\n");
                            } else {
                                putLabel("Irregular", center);
                                Log.i(TAG, "Irregular Quadrangle 2 detected");
                                detectResult.append("Irregular Quadrangle2 detected\n");
                            }

                        // 没有对角相等
                        } else {
                            // 有一对对边平行
                            if (isParallel(approxCurve.toArray()[0], approxCurve.toArray()[1], approxCurve.toArray()[2], approxCurve.toArray()[3])
                                    || isParallel(approxCurve.toArray()[1], approxCurve.toArray()[2], approxCurve.toArray()[0], approxCurve.toArray()[3])
                                    || isParallel(approxCurve.toArray()[1], approxCurve.toArray()[3], approxCurve.toArray()[0], approxCurve.toArray()[2])) {
                                putLabel("Trapezoid", center);
                                Log.i(TAG, "Trapezoid detected");
                                detectResult.append("Trapezoid detected\n");
                            } else {
                                putLabel("Irregular", center);
                                Log.i(TAG, "Irregular Quadrangle 3 detected");
                                detectResult.append("Irregular Quadrangle3 detected\n");
                            }
                        }

                    } else {
                        putLabel("Irregular", center);
                        Log.i(TAG, "Irregular Quadrangle 4 detected");
                        detectResult.append("Irregular Quadrangle4 detected\n");
                    }
                }
            }
        }
        Utils.matToBitmap(outputMat, outputBitmap);
        previewImageView.setImageBitmap(outputBitmap);
    }

    private void findPentagons() {
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(srcMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);
            int vertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(contour);
            if (Math.abs(contourArea) > 500) {
                if (vertices == 5) {
                    Point center = findCenter(contour);
                    putLabel("Pentagon", center);
                    Log.i(TAG, "Pentagon detected");
                    detectResult.append("Pentagon detected\n");
                }
            }
        }
        Utils.matToBitmap(outputMat, outputBitmap);
        previewImageView.setImageBitmap(outputBitmap);
    }

    private void findHexagons() {
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(srcMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);
            int vertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(contour);
            if (Math.abs(contourArea) > 500) {
                if (vertices == 6) {
                    Point center = findCenter(contour);
                    putLabel("Hexagon", center);
                    Log.i(TAG, "Hexagon detected");
                    detectResult.append("Hexagon detected\n");
                }
            }
        }
        Utils.matToBitmap(outputMat, outputBitmap);
        previewImageView.setImageBitmap(outputBitmap);
    }

    private void findCircles() {
        List<MatOfPoint> contours = new ArrayList<>();
        List<Double> rates = new ArrayList<>();
        Imgproc.findContours(srcMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int idx = 0; idx < contours.size(); idx++) {
            int count = 0;
            MatOfPoint contour = contours.get(idx);
            Point center = findCenter(contour);
            Point[] points = contour.toArray();

            // randomly pick a distance as a reference
            double dist0 = getDistance(points[0], center);
            for (Point point : points) {
                double dist = getDistance(point, center);
                double rate = dist / dist0;
                rates.add(rate);
            }
            for (int i = 0; i < rates.size(); i++) {
                if (rates.get(i) >= 0.9 && rates.get(i) <= 1.1)
                    count++;
            }
            double percentage = count * 1.0 / rates.size() * 100;
            if (percentage >= 60.0) {
                putLabel("Circle", center, percentage);
                Log.i(TAG, "The percentage is " + percentage + "%");
                detectResult.append("Circle " + percentage + "% detected\n");
            }
        }
        Utils.matToBitmap(outputMat, outputBitmap);
        previewImageView.setImageBitmap(outputBitmap);
    }

    private void findEllipses() {
        List<MatOfPoint> contours = new ArrayList<>();
        List<Double> distances = new ArrayList<>();
        double maxDist = 0, minDist = 0;
        int max_n = 0, min_n = 0;
        Imgproc.findContours(srcMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int idx = 0; idx < contours.size(); idx++) {
            MatOfPoint contour = contours.get(idx);
            Point center = findCenter(contour);
            Point[] points = contour.toArray();
            // get distances of each point to center point
            for (Point point : points) {
                double dist = getDistance(point, center);
                distances.add(dist);
            }
            // find the max dist and min dist
            maxDist = distances.get(0);
            minDist = distances.get(0);
            for (int i = 0; i < distances.size(); i++) {
                if (distances.get(i) >= maxDist) {
                    maxDist = distances.get(i);
                    max_n = i;
                }
                if (distances.get(i) <= minDist) {
                    minDist = distances.get(i);
                    min_n = i;
                }
            }
            Log.i(TAG, "Max Dist: " + maxDist + ", Min Dist: " + minDist);

            // find the focal point (焦点)
            double long_ax, short_ax, focus_dist;
            long_ax = maxDist;
            short_ax = minDist;
            focus_dist = sqrt(pow(maxDist, 2) - pow(minDist, 2));
            Point F1 = new Point();
            Point F2 = new Point();
            Point vec = new Point();
            vec.x = points[max_n].x - center.x;
            vec.y = points[max_n].y - center.y;
            double pro = focus_dist / long_ax;
            vec.x = vec.x * pro;
            vec.y = vec.y * pro;
            F1.x = vec.x + center.x;
            F1.y = vec.y + center.y;
            F2.x = center.x - vec.x;
            F2.y = center.y - vec.y;

            Log.i(TAG, "F1: " + F1.x + " " + F1.y);
            Log.i(TAG, "F2: " + F2.x + " " + F2.y);
            Log.i(TAG, "vec: " + vec.x + " " + vec.y);

            // get sum of all distances
            List<Double> sumOfDistances = new ArrayList<>();
            List<Double> rates = new ArrayList<>();
            for (int i = 0; i < distances.size(); i++) {
                double sumDist = sqrt(pow(F1.x - points[i].x, 2) + pow(F1.y - points[i].y, 2))
                        + sqrt(pow(F2.x - points[i].x, 2) + pow(F2.y - points[i].y, 2));
                sumOfDistances.add(sumDist);
            }
            for (int i = 0; i < distances.size(); i++) {
                double rate;
                if (2 * long_ax >= sumOfDistances.get(i))
                    rate = 2 * long_ax / sumOfDistances.get(i);
                else
                    rate = sumOfDistances.get(i) / (2 * long_ax);
                rates.add(rate);
            }
            int count = 0;
            double percentage;
            for (int i = 0; i < distances.size(); i++) {
                if (rates.get(i) <= 1.035)
                    count++;
            }
            percentage = count * 1.0 / rates.size() * 100;
            Log.i(TAG, "The percentage is " + percentage + "%");
            if (percentage >= 0.70 && (long_ax / short_ax) > 1.1) {
                putLabel("Ellipse", center, percentage);
                detectResult.append("Ellipse " + percentage + "% detected\n");
            }
        }
        Utils.matToBitmap(outputMat, outputBitmap);
        previewImageView.setImageBitmap(outputBitmap);
    }

    private Point findCenter(MatOfPoint contour) {
        Moments moments = Imgproc.moments(contour);
        Point center = new Point();
        center.x = moments.get_m10() / moments.get_m00();
        center.y = moments.get_m01() / moments.get_m00();
        Imgproc.circle(outputMat, center, 3, new Scalar(255, 0, 0));
        return center;
    }

    public boolean isParallel(Point P, Point Q, Point R, Point S) {
        double x1 = P.x, x2 = Q.x, x3 = R.x, x4 = S.x;
        double y1 = P.y, y2 = Q.y, y3 = R.y, y4 = S.y;
        double slopeRS = (y3 - y4) / (x3 - x4);
        double slopePQ = (y1 - y2) / (x1 - x2);

        // check if lines PQ and RS are parallel
        return Math.abs(slopeRS - slopePQ) < 0.1;
    }

//    @Deprecated
//    public boolean isParallel(Point P, Point Q, Point R, Point S) {
//        double x1 = P.x, x2 = Q.x, x3 = R.x, x4 = S.x;
//        double y1 = P.y, y2 = Q.y, y3 = R.y, y4 = S.y;
//
//        // check if lines PQ and RS are parallel
//        return Math.abs((x2 - x1) * (y4 - y3) - (x4 - x3) * (y2 - y1)) < 10;
//    }

    public double getDistance(Point p1, Point p2) {
        double distance = 0.0;
        if (p1 != null && p2 != null) {
            double xDiff = p1.x - p2.x;
            double yDiff = p1.y - p2.y;
            distance = sqrt(pow(xDiff, 2) + pow(yDiff, 2));
        }
        return distance;
    }

    public double getAngle(Point pt1, Point pt2, Point pt0) {
        double vertexPointX = pt0.x;
        double vertexPointY = pt0.y;
        double point0X = pt1.x;
        double point0Y = pt1.y;
        double point1X = pt2.x;
        double point1Y = pt2.y;
        // 向量的点乘
        int vector = (int) ((point0X - vertexPointX) * (point1X - vertexPointX)
                + (point0Y - vertexPointY) * (point1Y - vertexPointY));
        // 向量的模乘
        double sqrt = sqrt(
                (Math.abs((point0X - vertexPointX) * (point0X - vertexPointX))
                        + Math.abs((point0Y - vertexPointY) * (point0Y - vertexPointY)))
                        * (Math.abs((point1X - vertexPointX) * (point1X - vertexPointX))
                        + Math.abs((point1Y - vertexPointY) * (point1Y - vertexPointY)))
        );
        // 反余弦计算弧度
        double radian = Math.acos(vector / sqrt);
        // 弧度转角度制
        return (int) (180 * radian / Math.PI);
    }

    private void putLabel(String text, Point org) {
        Imgproc.putText(outputMat, text, org, Core.FONT_HERSHEY_COMPLEX,
                0.5, new Scalar(255, 0, 0));
    }

    private void putLabel(String text, Point org, double percentage) {
        Imgproc.putText(outputMat, text + ": " + percentage + "%", org, Core.FONT_HERSHEY_COMPLEX,
                0.5, new Scalar(255, 0, 0));
    }

    private void drawContour(List<MatOfPoint> contours, int contourIdx) {
        Imgproc.drawContours(outputMat, contours, contourIdx, new Scalar(255, 0, 0), 2);
    }

    private void changeThresh(int thresh) {
        Mat src = new Mat(originalBitmap.getHeight(), originalBitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(originalBitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.blur(src, src, new Size(3, 3));
        Imgproc.Canny(src, src, thresh, thresh * THRESH_RATIO);
        // write the updated Mat to bitmap
        Utils.matToBitmap(src, bitmap);
        previewImageView.setImageBitmap(bitmap);
        Log.i(TAG, String.valueOf(thresh));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        changeThresh(progress);

        triangleCheckBox.setChecked(false);
        quadrangleCheckBox.setChecked(false);
        pentagonCheckBox.setChecked(false);
        hexagonCheckBox.setChecked(false);
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
