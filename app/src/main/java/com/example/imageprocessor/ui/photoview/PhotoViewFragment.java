package com.example.imageprocessor.ui.photoview;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.Utility;
import com.example.imageprocessor.room.Image;

import java.util.List;
import java.util.Objects;

public class PhotoViewFragment extends Fragment {

    private final static String TAG = "PhotoViewFragment: ";

    private View root;

    private PhotoViewViewModel photoViewViewModel;
    private ViewPager2 viewPager2;
    private PhotoViewAdapter photoViewAdapter;
    private TextView photoTag;
    private ImageView saveButton;
    private List<Image> imageList;
    private int currentPosition;

    public static PhotoViewFragment newInstance() {
        return new PhotoViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.photo_view_fragment, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        photoViewViewModel = ViewModelProviders.of(this).get(PhotoViewViewModel.class);

        // init imageList
        imageList = Objects.requireNonNull(getArguments()).getParcelableArrayList("photo_list");
        Log.i(TAG, Objects.requireNonNull(imageList).toString());
        // get current position of imageList
        currentPosition = getArguments().getInt("photo_position");

        photoViewAdapter = new PhotoViewAdapter();
        photoViewAdapter.submitList(imageList);

        viewPager2 = root.findViewById(R.id.viewPager2);
        viewPager2.setAdapter(photoViewAdapter);
        photoTag = root.findViewById(R.id.photoTag);
        saveButton = root.findViewById(R.id.saveButton);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                photoTag.setText(getString(R.string.photo_tag, position + 1, imageList.size()));
            }
        });
        viewPager2.setCurrentItem(currentPosition, false);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "saveButton -> onClick");
                // save picture
                addPictureToGallery();
            }
        });
    }

    private void addPictureToGallery() {
        // check and ask permissions needed to save a picture
        if (Utility.checkAndAskGalleryPermissions(getContext(), getActivity())) {
            PhotoViewAdapter.PhotoViewViewHolder holder = photoViewAdapter.getHolder();
            BitmapDrawable drawable = (BitmapDrawable) Objects.requireNonNull(holder).photoView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            if (MediaStore.Images.Media.insertImage(requireContext().getContentResolver(),
                    bitmap, "", "") != null) {
                Toast.makeText(requireContext(), getString(R.string.save_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
