package com.example.imageprocessor.ui.history;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.imageprocessor.R;
import com.example.imageprocessor.room.Image;
import com.example.imageprocessor.room.ImageViewModel;

import java.util.List;

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private View root;

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private Context context;
    private ImageViewModel imageViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel =
                ViewModelProviders.of(this).get(HistoryViewModel.class);
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

        // TODO: Change here ..............................................................
        imageViewModel =
                ViewModelProviders.of(this).get(ImageViewModel.class);
        imageViewModel.getAllImages().observe(getViewLifecycleOwner(), new Observer<List<Image>>() {
            @Override
            public void onChanged(List<Image> images) {
                // update RecyclerView ...
                historyAdapter.setImages(images);
            }
        });
        // ................................................................................
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        historyAdapter = new HistoryAdapter(context);
    }
}
