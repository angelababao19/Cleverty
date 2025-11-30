package com.example.cleverty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Account extends AppCompatActivity {

    private TextView userNameText, userEmailText;
    private ImageView profileImage, editNameButton, settingsButton;
    private FloatingActionButton changeProfileFab;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNav;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;
    private FirebaseUser currentUser;

    // --- SharedPreferences for saving the local image path ---
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PREF_IMAGE_PATH = "profile_image_path";

    // --- Modern way to handle getting a result from the image gallery ---
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // User has selected an image, now we save it locally
                    saveImageLocally(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(Account.this, LoginPage.class));
            finish();
            return;
        }

        userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        initializeViews();
        setupClickListeners();
        loadUserData();
        loadLocalProfilePicture(); // Load the locally saved picture
    }

    private void initializeViews() {
        userNameText = findViewById(R.id.user_name_text);
        userEmailText = findViewById(R.id.user_email_text);
        profileImage = findViewById(R.id.profile_image);
        editNameButton = findViewById(R.id.edit_name_button);
        settingsButton = findViewById(R.id.settings_button);
        bottomNav = findViewById(R.id.bottomNav);
        progressBar = findViewById(R.id.account_progress_bar);
        changeProfileFab = findViewById(R.id.change_profile_fab);
    }

    private void setupClickListeners() {
        settingsButton.setOnClickListener(v -> startActivity(new Intent(Account.this, Settings.class)));
        editNameButton.setOnClickListener(v -> showEditNameDialog());
        changeProfileFab.setOnClickListener(v -> openImagePicker());

        bottomNav.setSelectedItemId(R.id.nav_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_account) {
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Homepage.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_timer) {
                startActivity(new Intent(getApplicationContext(), Timer.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        // This method now ONLY loads name and email from Firebase
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    userNameText.setText(name);
                    userEmailText.setText(email);
                } else if (currentUser != null) {
                    // Logic to create a new user profile in Firebase (name/email only)
                    // ... (this part is correct and doesn't need to change)
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(Account.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- NEW: Method to load the saved picture path from SharedPreferences ---
    private void loadLocalProfilePicture() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String imagePath = prefs.getString(PREF_IMAGE_PATH, null);

        if (imagePath != null) {
            Glide.with(this)
                    .load(new File(imagePath)) // Load the image from the local file path
                    .circleCrop()
                    .into(profileImage);
        } else {
            // If no path is saved, show the default
            profileImage.setImageResource(R.drawable.default_profile);
        }
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    // --- NEW: Method to save the chosen image to the app's internal storage ---
    private void saveImageLocally(Uri sourceUri) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        File internalStorageDir = getFilesDir();
        File profilePicFile = new File(internalStorageDir, "profile_pic.jpg");

        try (InputStream in = getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(profilePicFile)) {

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // --- Save the path to SharedPreferences ---
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_IMAGE_PATH, profilePicFile.getAbsolutePath());
            editor.apply();
            // ----------------------------------------

            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();

            // Immediately load the new image
            Glide.with(this).load(profilePicFile).circleCrop().into(profileImage);

        } catch (Exception e) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // --- Other methods (showEditNameDialog, updateUserName) are unchanged ---
    private void showEditNameDialog() {
        // ... (this method is correct)
    }

    private void updateUserName(String newName) {
        // ... (this method is correct)
    }
}
