package com.example.imageprocessor.ui.preview;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.bumptech.glide.util.Util;
import com.example.imageprocessor.R;
import com.example.imageprocessor.detector.ShapeDetector;
import com.example.imageprocessor.misc.OpenCVUtil;
import com.example.imageprocessor.misc.Utility;

import org.opencv.android.Utils;
import org.opencv.core.Core;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PreviewFragment extends Fragment {

    private final static String TAG = "PreviewFragment: ";

    private PreviewViewModel previewViewModel;
    private View root;

    private ImageView previewImageView;
    private Button buttonEdit;
    private Spinner shapeSpinner;
    private Button buttonDetect;
    // default detect shape is "Auto"
    private String selectedShape = "Auto";

    // 1 -> gallery, 2 -> camera
    private int from;
    private Uri uri;

    private Bitmap imageBitmap = null;
    private OpenCVUtil openCVUtil;

    private static Map<String, Integer> shapeResult = new HashMap<>();

    public static PreviewFragment newInstance() {
        return new PreviewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.preview_fragment, container, false);
        previewImageView = root.findViewById(R.id.previewImageView);
        shapeSpinner = root.findViewById(R.id.shapeSpinner);
        buttonDetect = root.findViewById(R.id.buttonDetect);
        buttonEdit = root.findViewById(R.id.buttonEdit);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        previewViewModel =
                ViewModelProviders.of(this).get(PreviewViewModel.class);

        uri = Uri.parse(Objects.requireNonNull(getArguments()).getString("uri"));
        from = getArguments().getInt("from");

        try {
            imageBitmap = Utility.getBitmap(uri, getContext(), from);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageBitmap != null) {
            previewImageView.setImageBitmap(imageBitmap);
        }

        // init spinner
        final ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()), R.array.shape_array,
                        android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shapeSpinner.setAdapter(spinnerAdapter);
        shapeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedShape = shapeSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap resultBitmap = detect(selectedShape, imageBitmap);
                Bundle bundle = new Bundle();
                bundle.putParcelable("resultBitmap", resultBitmap);
                Navigation.findNavController(root).navigate(R.id.action_previewFragment_to_detectFragment, bundle);
            }
        });
    }

    private Bitmap detect(String shape, final Bitmap srcBitmap) {
        openCVUtil = new OpenCVUtil();
        Bitmap resultBitmap = null;
        switch (shape.toLowerCase()) {
            case "auto":
                Log.i(TAG, "shape -> auto");
                resultBitmap = detectShapes(srcBitmap);
                break;
            case "line":
                Log.i(TAG, "shape -> line");
                break;
            case "circle":
                Log.i(TAG, "shape -> circle");
//                resultBitmap = detectCircles(srcBitmap);
                break;
            case "rectangle":
                Log.i(TAG, "shape -> rectangle");
                break;
            case "ellipse":
                Log.i(TAG, "shape -> ellipse");
                break;
            default:
                break;
        }
        return resultBitmap;
    }

    private Bitmap detectShapes(Bitmap srcBitmap) {
        Mat src = openCVUtil.bitmapToMat(srcBitmap);
        Mat dst = openCVUtil.cloneMat(src);
        openCVUtil.toGray(dst, dst);
        Imgproc.GaussianBlur(dst, dst, new Size(5, 5), 0);
//        Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
//                Imgproc.THRESH_BINARY_INV, 3, 4);
        Imgproc.threshold(dst, dst, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dst, contours, hierarchy,
                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        ShapeDetector detector = new ShapeDetector();
        for (int i = 0; i < contours.size(); i++) {
            Moments m = Imgproc.moments(contours.get(i));
            // get center point for each contour
            int cX = (int) (m.m10 / m.m00);
            int cY = (int) (m.m01 / m.m00);
            Imgproc.drawContours(src, contours, i, new Scalar(255, 0, 0, 0), 1);
            Imgproc.circle(src, new Point(cX, cY), 3, new Scalar(255, 0, 0), -1);

            String shape = detector.detect(new MatOfPoint2f(contours.get(i).toArray()));
            Imgproc.putText(src, shape, new Point(cX, cY), Core.FONT_HERSHEY_COMPLEX, 0.5, new Scalar(255, 0, 0), 1);
        }
        return openCVUtil.matToBitmap(src, srcBitmap);
    }

    // TODO: Not Accurate
//    private Bitmap detectLines(Bitmap srcBitmap) {
//        Mat srcImage = openCVUtil.bitmapToMat(srcBitmap);
//        Mat dstImage = openCVUtil.cloneMat(srcImage);
//        Imgproc.Canny(srcImage, dstImage, 400, 500, 5, false);
//        Mat mat = new Mat();
//        Imgproc.HoughLines(dstImage, mat, 1, Math.PI / 180, 200, 0, 0, 0, 10);
//        for (int i = 0; i < mat.rows(); i++) {
//            double[] vec = mat.get(i, 0);
//            double rho = vec[0];
//            double theta = vec[1];
//            Point point1 = new Point();
//            Point point2 = new Point();
//            double a = Math.cos(theta);
//            double b = Math.sin(theta);
//            double x0 = a * rho;
//            double y0 = b * rho;
//            point1.x = Math.round(x0 + 1000 * (-b));
//            point1.y = Math.round(y0 + 1000 * a);
//            point2.x = Math.round(x0 - 1000 * (-b));
//            point2.y = Math.round(y0 - 1000 * a);
//            if (theta >= 0)
//                Imgproc.line(dstImage, point1, point2, new Scalar(255, 255, 255, 255), 1, Imgproc.LINE_4, 0);
//        }
//        return openCVUtil.matToBitmap(dstImage, srcBitmap);
//    }

//    private void detectShapes(Bitmap srcBitmap) {
//        // convert bitmap to mat
//        Mat image = openCVUtil.bitmapToMat(srcBitmap);
//        Mat m1 = openCVUtil.cloneMat(image);
//        Mat declareMat = openCVUtil.cloneMat(image);
//        openCVUtil.reverseColor(image);
//        openCVUtil.gaussianBlur(declareMat, 9, 9, 9);
//        Mat m2 = openCVUtil.cloneMat(declareMat);
//        Mat srcWhite = openCVUtil.cloneMat(declareMat);
//        openCVUtil.dilate(image, 1, 1, 1);
//        openCVUtil.toGray(image, image);
//        Mat binary = openCVUtil.cloneMat(image);
//        openCVUtil.toBinary(binary, 130, 255);
////        openCVUtil.coverBackGroundToBlack(binary);
//        List<MatOfPoint> contours = openCVUtil.getContours(binary);
//        for (int i = 0; i < contours.size(); i++) {
//            Mat mat = openCVUtil.createMat(0, 0, 0);
//            openCVUtil.drawContours(mat, contours, i, 0, 255, 0, 1);
//            Point[] points = openCVUtil.getCorners(mat);
//            Point[] checkedPoints = openCVUtil.checkPoint(points);
//            for (Point checkedPoint : checkedPoints) {
//                openCVUtil.drawCircleByCenter(mat, checkedPoint);
//            }
//        }
//        Bitmap bitmap = openCVUtil.matToBitmap(binary, srcBitmap);
//        previewImageView.setImageBitmap(bitmap);
//    }
}
