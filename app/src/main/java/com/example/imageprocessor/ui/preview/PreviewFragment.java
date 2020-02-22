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
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.imageprocessor.R;
import com.example.imageprocessor.detector.DefaultDetector;
import com.example.imageprocessor.detector.RectangleDetector;
import com.example.imageprocessor.misc.OpenCVUtil;
import com.example.imageprocessor.misc.Utility;

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

    private ProgressBar progressBarPreview;
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
        progressBarPreview = root.findViewById(R.id.progressBarPreview);
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

        progressBarPreview.setVisibility(View.GONE);
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object[] objs = detect(selectedShape, imageBitmap);
                Bundle bundle = new Bundle();
                bundle.putParcelable("resultBitmap", (Bitmap)objs[0]);
                bundle.putString("resultText", (String)objs[1]);
                Navigation.findNavController(root).navigate(R.id.action_previewFragment_to_detectFragment, bundle);
            }
        });
    }

    private Object[] detect(String shape, final Bitmap srcBitmap) {
        openCVUtil = new OpenCVUtil();
        Object[] result = null;
        switch (shape.toLowerCase()) {
            case "line":
                Log.i(TAG, "shape -> line");
                result = detectLines(srcBitmap);
                break;
            case "circle":
                Log.i(TAG, "shape -> circle");
                result = detectCircles(srcBitmap);
                break;
            case "rectangle":
                Log.i(TAG, "shape -> rectangle");
                result = detectRectangles(srcBitmap);
                break;
            case "ellipse":
                Log.i(TAG, "shape -> ellipse");
                break;
            default:
                break;
        }
        return result;
    }

    private Object[] detectLines(Bitmap srcBitmap) {
        Mat src = openCVUtil.bitmapToMat(srcBitmap);
        Mat dst = openCVUtil.cloneMat(src);
        return null;
    }

    private Object[] detectCircles(Bitmap srcBitmap) {
        Mat src = openCVUtil.bitmapToMat(srcBitmap);
        Mat dst = openCVUtil.cloneMat(src);
        return null;
    }

    private Object[] detectRectangles(Bitmap srcBitmap) {
        Mat src = openCVUtil.bitmapToMat(srcBitmap);
        Mat dst = openCVUtil.cloneMat(src);
        // convert image to grayscale
        openCVUtil.toGray(dst, dst);
        // down-scale and up-scale the image to remove small noises
        Mat pyr = new Mat();
        Imgproc.pyrDown(dst, pyr, new Size(src.cols()/2, src.rows()/2));
        Imgproc.pyrUp(pyr, dst, src.size());
        // blur the image to reduce noise
        Imgproc.GaussianBlur(dst, dst, new Size(3, 3), 0);
        // apply canny
        Imgproc.Canny(dst, dst, 20, 50, 3, false);
        // dilate canny to remove potential holes between edge segments
        Imgproc.dilate(dst, dst, new Mat(), new Point(-1, -1));
        // find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dst, contours, hierarchy,
                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        RectangleDetector detector = new RectangleDetector();
        int triangle = 0, rectangle = 0, square = 0, polygon = 0;
        for (int i = 0; i < contours.size(); i++) {
            Moments m = Imgproc.moments(contours.get(i));
            // get center point for each contour
            int cX = (int) (m.m10 / m.m00);
            int cY = (int) (m.m01 / m.m00);
            String shape = detector.detect(new MatOfPoint2f(contours.get(i).toArray()));
            if (shape != null) {
                Imgproc.drawContours(src, contours, i, new Scalar(255, 0, 0, 0), 1);
                Imgproc.circle(src, new Point(cX, cY), 3, new Scalar(255, 0, 0), -1);
                Imgproc.putText(src, shape, new Point(cX, cY), Core.FONT_HERSHEY_COMPLEX, 0.5, new Scalar(255, 0, 0), 1);
                if (shape.equalsIgnoreCase("triangle"))
                    triangle++;
                else if (shape.equalsIgnoreCase("rectangle"))
                    rectangle++;
                else if (shape.equalsIgnoreCase("square"))
                    square++;
                else if (shape.equalsIgnoreCase("polygon"))
                    polygon++;
            }
        }
        String resultText = "Triangle: " + triangle + "\nRectangle: " + rectangle + "\nSquare: " + square + "\nPolygon: " + polygon;
        Bitmap resultBitmap = openCVUtil.matToBitmap(dst, srcBitmap);
        return new Object[] {resultBitmap, resultText};
    }

    private Bitmap detectDefault(Bitmap srcBitmap) {
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
        DefaultDetector detector = new DefaultDetector();
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
}
