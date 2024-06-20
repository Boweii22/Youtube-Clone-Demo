package com.example.youtubeclone;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import com.example.youtubeclone.Adapter.PlaylistAdapter;
import com.example.youtubeclone.Models.PlaylistModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PublishContentActivity extends AppCompatActivity {

    private static final String TAG = "PublishContentActivity";

    EditText input_video_title, input_video_description;
    LinearLayout progressLyt;
    ProgressBar progressBar;
    TextView progress_text;

    VideoView videoView;
    Uri videoUri;

    MediaController mediaController;
    NachoTextView nachoTextView;
    TextView txt_upload;

    TextView txt_choose_playlist;

    Dialog dialog;

    FirebaseUser user;
    DatabaseReference reference;
    StorageReference storageReference;

    String selectedPlaylist;
    int videosCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_content);

        // Initialize UI components
        nachoTextView = findViewById(R.id.input_video_tag);
        txt_choose_playlist = findViewById(R.id.choose_playlist);
        txt_upload = findViewById(R.id.txt_upload);
        videoView = findViewById(R.id.videoView);
        input_video_title = findViewById(R.id.input_video_title);
        input_video_description = findViewById(R.id.input_video_description);
        progressLyt = findViewById(R.id.progressLyt);
        progress_text = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar);
        mediaController = new MediaController(PublishContentActivity.this);

        // Handle Intent
        Intent intent = getIntent();
        if (intent != null) {
            videoUri = intent.getData();
            if (videoUri != null) {
                videoView.setVideoURI(videoUri);
                videoView.setMediaController(mediaController);
                videoView.start();
            } else {
                Toast.makeText(this, "Invalid video URI", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Invalid video URI");
            }
        }

        // Initialize Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Videos");
        storageReference = FirebaseStorage.getInstance().getReference().child("Videos");

        // Setup NachoTextView for tags
        nachoTextView.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);

        // Set listeners
        txt_upload.setOnClickListener(view -> {
            String title = input_video_title.getText().toString().trim();
            String description = input_video_description.getText().toString().trim();
            String tags = nachoTextView.getAllChips().toString().replace(",", "").trim();

            if (title.isEmpty() || description.isEmpty() || tags.isEmpty()) {
                Toast.makeText(PublishContentActivity.this, "Fill all Fields...", Toast.LENGTH_SHORT).show();
            } else if (txt_choose_playlist.getText().toString().equals("Choose Playlist")) {
                Toast.makeText(PublishContentActivity.this, "Please select playlist", Toast.LENGTH_SHORT).show();
            } else {
                uploadVideoToStorage(title, description, tags);
            }
        });

        txt_choose_playlist.setOnClickListener(view -> showPlaylistDialog());
    }

    private void uploadVideoToStorage(String title, String description, String tags) {
        if (videoUri == null) {
            Toast.makeText(this, "Video URI is null", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Video URI is null");
            return;
        }

        progressLyt.setVisibility(View.VISIBLE);
        String fileExtension = getFileExtensions(videoUri);
        if (fileExtension == null) {
            Toast.makeText(this, "Invalid file extension", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid file extension");
            return;
        }

        final StorageReference storageReference1 = storageReference.child(user.getUid()).child(System.currentTimeMillis() + "." + fileExtension);
        storageReference1.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> storageReference1.getDownloadUrl().addOnSuccessListener(uri -> {
                    String videoUrl = uri.toString();
                    saveDataToFirebase(title, description, tags, videoUrl);
                }))
                .addOnProgressListener(snapshot -> {
                    double progress = 100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount();
                    progressBar.setProgress((int) progress);
                    progress_text.setText("Uploading " + (int) progress + "%");
                })
                .addOnFailureListener(e -> {
                    progressLyt.setVisibility(View.GONE);
                    Toast.makeText(PublishContentActivity.this, "Failed to upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to upload", e);
                });
    }

    private void saveDataToFirebase(String title, String description, String tags, String videoUrl) {
        String currentDate = DateFormat.getDateInstance().format(new Date());
        String videoId = reference.push().getKey();

        HashMap<String, Object> map = new HashMap<>();
        map.put("videoid", videoId);
        map.put("video_title", title);
        map.put("video_description", description);
        map.put("video_tag", tags);
        map.put("playlist", selectedPlaylist);
        map.put("video_url", videoUrl);
        map.put("publisher", user.getUid());
        map.put("type","video");
        map.put("views",0);
        map.put("date", currentDate);

        reference.child(videoId).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressLyt.setVisibility(View.GONE);
                Toast.makeText(PublishContentActivity.this, "Video Uploaded", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PublishContentActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                updateVideoCount();
            } else {
                progressLyt.setVisibility(View.GONE);
                Toast.makeText(PublishContentActivity.this, "Failed to upload: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to upload", task.getException());
            }
        });
    }

    private void updateVideoCount() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        int update = videosCount + 1;
        HashMap<String, Object> map = new HashMap<>();
        map.put("videos", update);

        databaseReference.child(user.getUid()).child(selectedPlaylist).updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to update video count", task.getException());
                    }
                });
    }

    private String getFileExtensions(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void showPlaylistDialog() {
        dialog = new Dialog(PublishContentActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.playlist_dialog);
        dialog.setCancelable(true);

        EditText input_playlist_name = dialog.findViewById(R.id.input_playlist_name);
        TextView txt_add = dialog.findViewById(R.id.txt_add);

        ArrayList<PlaylistModel> list = new ArrayList<>();
        PlaylistAdapter adapter = new PlaylistAdapter(PublishContentActivity.this, list, model -> {
            dialog.dismiss();
            selectedPlaylist = model.getPlaylist_name();
            videosCount = model.getVideos();
            txt_choose_playlist.setText("Playlist : " + model.getPlaylist_name());
        });

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        checkUserAlreadyHavePlaylist(recyclerView);
        showAllPlaylists(adapter, list);

        txt_add.setOnClickListener(view -> {
            String value = input_playlist_name.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(PublishContentActivity.this, "Enter Playlist Name", Toast.LENGTH_SHORT).show();
            } else {
                createNewPlaylist(value);
            }
        });

        dialog.show();
    }

    private void showAllPlaylists(PlaylistAdapter adapter, ArrayList<PlaylistModel> list) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        PlaylistModel model = dataSnapshot.getValue(PlaylistModel.class);
                        list.add(model);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PublishContentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to fetch playlists", error.toException());
            }
        });
    }

    private void createNewPlaylist(String value) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Playlists");

        HashMap<String, Object> map = new HashMap<>();
        map.put("playlist_name", value);
        map.put("videos", 0);
        map.put("uid", user.getUid());

        databaseReference.child(user.getUid()).child(value).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(PublishContentActivity.this, "New Playlist Created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PublishContentActivity.this, "Failed to create playlist: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to create playlist", task.getException());
            }
        });
    }

    private void checkUserAlreadyHavePlaylist(RecyclerView recyclerView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(PublishContentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to check user playlists", error.toException());
            }
        });
    }
}
