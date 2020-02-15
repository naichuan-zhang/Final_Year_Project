package com.example.imageprocessor.ui.history;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.FootViewStatus;
import com.example.imageprocessor.room.Image;

import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final static String TAG = "History Adapter: ";
    private final static int NORMAL_VIEW_TYPE = 0;
    private final static int FOOTER_VIEW_TYPE = 1;
    private FootViewStatus footerViewStatus = FootViewStatus.CAN_LOAD_MORE;

    private Context context;
    private View itemView;
    private View view;
    private HistoryFragment fragment;
    private HistoryViewHolder holder;
    private List<Image> images = new ArrayList<>();

    public HistoryAdapter(Context context, HistoryFragment fragment, View view) {
        this.context = context;
        this.fragment = fragment;
        this.view = view;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (getItemCount() == 0) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.no_history, parent, false);
        } else {
            if (viewType == FOOTER_VIEW_TYPE) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.history_footer, parent, false);
                footerViewStatus = FootViewStatus.NO_MORE;
            } else {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.history_cell, parent, false);
                footerViewStatus = FootViewStatus.CAN_LOAD_MORE;
            }
        }
        holder = new HistoryViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder holder, int position) {
        if (position == getItemCount()) {
            Log.i(TAG, "onBindViewHolder -> footer view type");
            switch (footerViewStatus) {
                case CAN_LOAD_MORE:
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.textViewFooter.setText(R.string.loading);
                    break;
                case NO_MORE:
                    holder.progressBar.setVisibility(View.GONE);
                    holder.textViewFooter.setText(R.string.no_loading);
                    break;
                default:
                    break;
            }
            return;
        }
        Image image = images.get(position);
        holder.textViewImageDate.setText(image.getImageDate());
        holder.imageButtonShowDetails.setVisibility(View.VISIBLE);
        holder.imageButtonShowDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "show popup menu");
                showPopupMenu(holder.imageButtonShowDetails);
            }
        });
        holder.shimmerLayoutHistory.setShimmerColor(0x55FFFFFF);
        holder.shimmerLayoutHistory.setShimmerAngle(0);
        holder.shimmerLayoutHistory.startShimmerAnimation();
        Glide.with(holder.itemView)
                .load(image.getImageUri())
                .placeholder(R.drawable.image_placeholder)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.shimmerLayoutHistory.stopShimmerAnimation();
                        return false;
                    }
                })
                .into(holder.imageViewHistory);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == getItemCount()) ? FOOTER_VIEW_TYPE : NORMAL_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setImages(List<Image> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public Image getImageAt(int position) {
        return images.get(position);
    }

    public boolean isEmpty() {
        return images.isEmpty();
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_history, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new HistoryMenuItemClickListener());
        popupMenu.show();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        // ------------ No history -----------
        private TextView textViewNoHistory;
        // ------------ No history -----------

        // ------------ Normal ----------------
        private TextView textViewImageDate;
        private ImageView imageViewHistory;
        private ImageButton imageButtonShowDetails;
        private ShimmerLayout shimmerLayoutHistory;
        // ------------ Normal ----------------

        // --------- Footer ----------------
        private ProgressBar progressBar;
        private TextView textViewFooter;
        // --------- Footer ----------------

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewNoHistory = itemView.findViewById(R.id.textViewNoHistory);

            textViewImageDate = itemView.findViewById(R.id.textViewImageDate);
            imageViewHistory = itemView.findViewById(R.id.imageViewHistory);
            imageButtonShowDetails = itemView.findViewById(R.id.imageButtonShowDetails);
            shimmerLayoutHistory = itemView.findViewById(R.id.shimmerLayoutHistory);

            progressBar = itemView.findViewById(R.id.progressBarHistoryFooter);
            textViewFooter = itemView.findViewById(R.id.textViewFooter);
        }
    }

    // TODO: ERROR -> get position !!!!!!!!!!!!!!!!!!!!!!!!!!!!
    class HistoryMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_history_view:
                    Log.i(TAG, "history view clicked");
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("photo_list", (ArrayList<? extends Parcelable>) images);
                    bundle.putInt("photo_position", holder.getAdapterPosition());
                    Navigation.findNavController(itemView).navigate(R.id.action_nav_history_to_photoViewFragment, bundle);
                    return true;
                case R.id.action_history_share:
                    Log.i(TAG, "history share clicked");
                    return true;
                case R.id.action_history_delete:
                    Log.i(TAG, "history delete clicked");
                    fragment.deleteImage(getImageAt(holder.getAdapterPosition()));
                    return true;
            }
            return false;
        }
    }
}
