package com.example.imageprocessor.ui.history;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imageprocessor.R;
import com.example.imageprocessor.room.Image;

import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final static String TAG = "History Adapter: ";

    private Context context;
    private List<Image> images = new ArrayList<>();

    public HistoryAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_cell, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder holder, int position) {
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
        // TODO: Add ShimmerLayout features here !!!!!!!!!!!!!!!
//        holder.imageViewHistory.setImageURI(Uri.parse(image.getImageUri()));
//        Glide.with(holder.itemView).load(Uri.parse(image.getImageUri())).into(holder.imageViewHistory);
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
        private TextView textViewImageDate;
        private ImageView imageViewHistory;
        private ImageButton imageButtonShowDetails;
        private ShimmerLayout shimmerLayoutHistory;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewImageDate = itemView.findViewById(R.id.textViewImageDate);
            imageViewHistory = itemView.findViewById(R.id.imageViewHistory);
            imageButtonShowDetails = itemView.findViewById(R.id.imageButtonShowDetails);
            shimmerLayoutHistory = itemView.findViewById(R.id.shimmerLayoutHistory);
        }
    }

    class HistoryMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_history_details:
                    Log.i(TAG, "history details clicked");
                    return true;
                case R.id.action_history_delete:
                    Log.i(TAG, "history delete clicked");
                    return true;
            }
            return false;
        }
    }
}
