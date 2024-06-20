package com.example.youtubeclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.youtubeclone.Models.PlaylistModel;
import com.example.youtubeclone.R;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    Context context;
    ArrayList<PlaylistModel> list;
    OnItemClickListener listener; // Use custom listener

    public PlaylistAdapter(Context context, ArrayList<PlaylistModel> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlaylistAdapter.ViewHolder holder, int position) {
        holder.bind(list.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_playlist_name, txt_videos_count;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            txt_playlist_name = itemView.findViewById(R.id.txt_playlist_name);
            txt_videos_count = itemView.findViewById(R.id.txt_videos_count);
        }

        public void bind(final PlaylistModel model, final OnItemClickListener listener) {
            txt_playlist_name.setText(model.getPlaylist_name());
            txt_videos_count.setText("Videos " + model.getVideos());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(model);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(PlaylistModel model);
    }
}
