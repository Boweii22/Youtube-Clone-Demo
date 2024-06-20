package com.example.youtubeclone;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView userProfileImage;
    private TextView userName, email;
    private TextView txtYourChannel, txtSettings, txtHelp;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();
        setupFirebase();
        if (user != null) {
            loadUserData();
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            finish(); // End the activity if user is not authenticated
        }

        txtYourChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    checkUserHaveChannel();
                }
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        userProfileImage = findViewById(R.id.user_profile_image);
        userName = findViewById(R.id.user_channel_name);
        email = findViewById(R.id.email);
        txtYourChannel = findViewById(R.id.txt_channel_name);
        txtSettings = findViewById(R.id.settings);
        txtHelp = findViewById(R.id.txt_help);
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void loadUserData() {
        databaseReference.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String userEmail = snapshot.child("email").getValue(String.class);
                    String profileImage = snapshot.child("profile").getValue(String.class);

                    userName.setText(username);
                    email.setText(userEmail);

                    if (profileImage != null && !profileImage.isEmpty()) {
                        Picasso.get().load(profileImage).placeholder(R.drawable.icons8_account_24).into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(AccountActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserHaveChannel() {
        databaseReference.child("Channels").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                    intent.putExtra("type", "channel");
                    startActivity(intent);
                } else {
                    showCreateChannelDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(AccountActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateChannelDialog() {
        Dialog dialog = new Dialog(AccountActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.channel_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        EditText inputChannelName = dialog.findViewById(R.id.input_channel_name);
        EditText inputChannelDescription = dialog.findViewById(R.id.input_description);
        TextView txtCreateChannel = dialog.findViewById(R.id.txt_create_channel);

        txtCreateChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputChannelName.getText().toString();
                String description = inputChannelDescription.getText().toString();

                if (name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(AccountActivity.this, "Fill Required Fields", Toast.LENGTH_SHORT).show();
                } else {
                    createNewChannel(name, description, dialog);
                }
            }
        });

        dialog.show();
    }

    private void createNewChannel(String name, String description, Dialog dialog) {
        ProgressDialog progressDialog = new ProgressDialog(AccountActivity.this);
        progressDialog.setTitle("Creating new channel");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String date = DateFormat.getDateInstance().format(new Date());
        HashMap<String, Object> channelData = new HashMap<>();
        channelData.put("channel_name", name);
        channelData.put("channel_description", description);
        channelData.put("joined", date);
        channelData.put("user_id", user.getUid());
        if (user.getPhotoUrl() != null) {
            channelData.put("channel_logo", user.getPhotoUrl().toString());
        } else {
            channelData.put("channel_logo", "default_logo_url"); // Use a default logo URL if user's photo URL is null
        }

        databaseReference.child("Channels").child(user.getUid()).setValue(channelData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(AccountActivity.this, name + " channel has been created", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AccountActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
