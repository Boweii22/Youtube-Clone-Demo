package com.example.youtubeclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.youtubeclone.Models.ContentModel;
import com.example.youtubeclone.R;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {
    Context context;
    ArrayList<ContentModel> list;
    DatabaseReference databaseReference;

    public ContentAdapter(Context context, ArrayList<ContentModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public ContentAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ContentAdapter.ViewHolder holder, int position) {
        ContentModel model = list.get(position);
        if (model != null) {
            //thumbnail
            Glide.with(context).asBitmap().load(model.getVideo_url()).into(holder.thumbnail);
            holder.video_title.setText(model.getVideo_title());
            holder.views.setText(model.getViews() + "Views");
            holder.date.setText(model.getDate());
            
            setData(model.getPublisher(), holder.channel_logo, holder.channel_name);

        }
    }

    private void setData(String publisher, CircleImageView channel_logo, TextView channel_name) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Channels");
        databaseReference.child(publisher).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String cName = snapshot.child("channel_name").getValue().toString();
                    String cLogo = snapshot.child("channel_logo").getValue().toString();
                    channel_name.setText(cName);
                    Picasso.get().load(cLogo).placeholder(R.drawable.icons8_account_24).into(channel_logo);

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView video_title, channel_name, views, date;
        CircleImageView channel_logo;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            video_title = itemView.findViewById(R.id.video_title);
            channel_name = itemView.findViewById(R.id.channel_name);
            views = itemView.findViewById(R.id.views_count);
            channel_logo = itemView.findViewById(R.id.channel_logo);
            date = itemView.findViewById(R.id.date);

        }
    }
}
