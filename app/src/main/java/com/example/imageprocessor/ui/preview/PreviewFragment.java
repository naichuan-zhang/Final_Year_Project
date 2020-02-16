package com.example.imageprocessor.ui.preview;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.Utility;

import java.io.IOException;
import java.util.Objects;

public class PreviewFragment extends Fragment {

    private final static String TAG = "PreviewFragment: ";

    private PreviewViewModel previewViewModel;
    private View root;

    private ImageView previewImageView;
    private Button buttonEdit;
    private Spinner shapeSpinner;
    private Button buttonDetect;

    // 1 -> gallery, 2 -> camera
    private int from;
    private Uri uri;

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
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        previewViewModel =
                ViewModelProviders.of(this).get(PreviewViewModel.class);

        uri = Uri.parse(Objects.requireNonNull(getArguments()).getString("uri"));
        from = getArguments().getInt("from");

        Bitmap imageBitmap = null;
        try {
            imageBitmap = Utility.getBitmap(uri, getContext(), from);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageBitmap != null) {
            // TODO: do something
        }
        previewImageView.setImageBitmap(imageBitmap);

        // init spinner
        ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()), R.array.shape_array,
                        android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shapeSpinner.setAdapter(spinnerAdapter);


        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.action_previewFragment_to_detectFragment);
            }
        });
    }
}
