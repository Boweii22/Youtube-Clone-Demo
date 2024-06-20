package com.example.youtubeclone;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.youtubeclone.fragment.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    private ImageView userProfileImage;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100;
    private static final int PERMISSION = 101;
    private static final int PICK_VIDEO = 102;
    private AppBarLayout appBarLayout;
    private Fragment fragment = null;
    Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();
        setupFirebase();
        setupGoogleSignIn();
        setupBottomNavigationView();
        getProfileImage();
        showFragment();
        checkPermission();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        frameLayout = findViewById(R.id.frame_layout);
        userProfileImage = findViewById(R.id.user_profile_image);
        appBarLayout = findViewById(R.id.appBar);

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    startActivity(new Intent(MainActivity.this, AccountActivity.class));
                } else {
                    userProfileImage.setImageResource(R.drawable.icons8_account_24);
                    showSignInDialog();
                }
            }
        });
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                if (item.getItemId() == R.id.home){
                    selectFragment(new HomeFragment());
                    return true;
                }else if (item.getItemId() == R.id.explore) {
                    selectFragment(new ExploreFragment());
                    return true;
                } else if (item.getItemId() == R.id.publish) {
                    showPublishContentDialog();
                    return true;
                } else if (item.getItemId() == R.id.subscription) {
                    selectFragment(new SubscriptionFragment());
                    return true;
                } else if (item.getItemId() == R.id.library) {
                    selectFragment(new LibraryFragment());
                    return true;
                }else {
                    return false;
                }
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    private void showPublishContentDialog() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upload_dialog);
        dialog.setCanceledOnTouchOutside(true);

        TextView txt_upload_video = dialog.findViewById(R.id.txt_upload_video);
        TextView txt_make_post = dialog.findViewById(R.id.txt_publish_post);
        TextView txt_poll = dialog.findViewById(R.id.txt_release_poll);

        txt_upload_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, PublishContentActivity.class);
//                intent.putExtra("type","video");
//                startActivity(intent);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(Intent.createChooser(intent, "Select video"), PICK_VIDEO);
            }
        });

        dialog.show();
    }

    private void selectFragment(Fragment fragment) {
        setStatusBarColor("#FFFFFF");
        appBarLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }


    private void showSignInDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_signin_dialog, findViewById(android.R.id.content), false);
        builder.setView(view);
        TextView txtGoogleSignIn = view.findViewById(R.id.text_google_signin);
        txtGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        builder.create().show();
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == PICK_VIDEO) {
            if (resultCode == RESULT_OK && data != null) {
                videoUri = data.getData();
                Intent intent = new Intent(MainActivity.this, PublishContentActivity.class);
                intent.putExtra("type","video");
                intent.setData(videoUri);
                startActivity(intent);
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        saveUserToDatabase(account);
                    } else {
                        Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (ApiException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserToDatabase(GoogleSignInAccount account) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", account.getDisplayName());
        userMap.put("email", account.getEmail());
        userMap.put("profile", String.valueOf(account.getPhotoUrl()));
        userMap.put("uid", Objects.requireNonNull(firebaseUser).getUid());
        userMap.put("search", account.getDisplayName().toLowerCase());

        databaseReference.child(firebaseUser.getUid()).setValue(userMap);
    }

    private void getProfileImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(Objects.requireNonNull(user).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profileString = Objects.requireNonNull(snapshot.child("profile").getValue()).toString();
                    Picasso.get().load(profileString).placeholder(R.drawable.icons8_account_24).into(userProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFragment() {
        String type = getIntent().getStringExtra("type");
        if (type != null && type.equals("channel")) {
            setStatusBarColor("#99FF0000");
            appBarLayout.setVisibility(View.GONE);
            fragment = ChannelDashboardFragment.newInstance();
            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit();
            } else {
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setStatusBarColor(String color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(color));
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION);
        }else {
            Log.d("tag","checkPermission: Permission Granted");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.notification) {
            Toast.makeText(this, "Notification", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.search) {
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }
}
