package com.example.imageprocessor.ui.photoview;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imageprocessor.R;
import com.example.imageprocessor.room.Image;

import uk.co.senab.photoview.PhotoView;

public class PhotoViewAdapter extends ListAdapter<Image, PhotoViewAdapter.PhotoViewViewHolder> {

    private PhotoViewViewHolder holder;

    public PhotoViewAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public PhotoViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_view, parent, false);
        return new PhotoViewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(getItem(position).getImageUri())
                .placeholder(R.drawable.image_placeholder)
                .into(holder.photoView);
        // TODO: ERROR -> Sometimes crashes !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        setHolder(holder);
    }

    private static DiffUtil.ItemCallback<Image> DIFF_CALLBACK = new DiffUtil.ItemCallback<Image>() {
        @Override
        public boolean areItemsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
            return oldItem.getImageID() == newItem.getImageID();
        }
    };
    // TODO: ERROR -> Sometimes crashes !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public PhotoViewViewHolder getHolder() {
        return holder;
    }

    public void setHolder(PhotoViewViewHolder holder) {
        this.holder = holder;
    }

    class PhotoViewViewHolder extends RecyclerView.ViewHolder {

        PhotoView photoView;

        PhotoViewViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }
    }
}
