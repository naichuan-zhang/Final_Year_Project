package com.example.imageprocessor.ui.history;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.imageprocessor.R;
import com.example.imageprocessor.room.Image;
import com.example.imageprocessor.room.ImageViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends Fragment {

    private final static String TAG = "History Fragment: ";

    private HistoryViewModel historyViewModel;
    private View root;

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private ImageViewModel imageViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel =
                ViewModelProviders.of(this).get(HistoryViewModel.class);
        imageViewModel =
                ViewModelProviders.of(this).get(ImageViewModel.class);
        root = inflater.inflate(R.layout.fragment_history, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = root.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(historyAdapter);

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayoutHistory);
        swipeRefreshLayout.setRefreshing(true);

        imageViewModel.getAllImages().observe(getViewLifecycleOwner(), new Observer<List<Image>>() {
            @Override
            public void onChanged(List<Image> images) {
                // update RecyclerView
                historyAdapter.setImages(images);
            }
        });

        swipeRefreshLayout.setRefreshing(false);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "swipeRefreshLayout -> onRefresh");
                // TODO: Refresh ...
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        historyAdapter = new HistoryAdapter(context, this, getView());
    }

    void viewImage(int position, List<Image> images) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("photo_list", (ArrayList<? extends Parcelable>) images);
        bundle.putInt("photo_position", position);
        Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.action_nav_history_to_photoViewFragment, bundle);
    }

    void shareImage(Image image) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(image.getImageUri()));
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));
    }

    void deleteImage(Image deletedImage) {
        imageViewModel.deleteImages(deletedImage);
        showUndoSnackbar(deletedImage);
    }

    private void showUndoSnackbar(final Image deletedImage) {
        Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getView()), R.string.snack_bar_text, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.snack_bar_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewModel.insertImages(deletedImage);
            }
        }).show();
    }
}
