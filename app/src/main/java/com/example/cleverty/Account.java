package com.example.cleverty;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Account extends AppCompatActivity {

    private TextView userNameText, userEmailText;
    private ImageView profileImage, editNameButton, settingsButton;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNav;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;
    private FirebaseUser currentUser;

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
    }

    private void initializeViews() {
        userNameText = findViewById(R.id.user_name_text);
        userEmailText = findViewById(R.id.user_email_text);
        profileImage = findViewById(R.id.profile_image);
        editNameButton = findViewById(R.id.edit_name_button);
        settingsButton = findViewById(R.id.settings_button);
        progressBar = findViewById(R.id.account_progress_bar);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupClickListeners() {
        settingsButton.setOnClickListener(v -> startActivity(new Intent(Account.this, Settings.class)));
        editNameButton.setOnClickListener(v -> showEditNameDialog());

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
        progressBar.setVisibility(View.VISIBLE);

        userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User data exists, load it
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    userNameText.setText(name);
                    userEmailText.setText(email);
                    progressBar.setVisibility(View.GONE);
                } else if (currentUser != null) {
                    // User data does not exist, this is a new user, so create it.
                    // Declare variables as 'final' so they are accessible in the inner OnCompleteListener
                    final String finalNameFromAuth = (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty()) ? "New User" : currentUser.getDisplayName();
                    final String finalEmailFromAuth = currentUser.getEmail();

                    // Use YOUR User class, not the Firestore one
                    User newUser = new User(finalNameFromAuth, finalEmailFromAuth, "");

                    userDatabaseReference.setValue(newUser).addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Now that data is saved, set it to the UI
                            userNameText.setText(finalNameFromAuth);
                            userEmailText.setText(finalEmailFromAuth);
                            Toast.makeText(Account.this, "Profile created!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Account.this, "Failed to create profile.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Account.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_name, null);
        builder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.edit_text_name);
        final Button buttonSave = dialogView.findViewById(R.id.button_save);
        final Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        editTextName.setText(userNameText.getText().toString());

        final AlertDialog dialog = builder.create();

        buttonSave.setOnClickListener(v -> {
            String newName = editTextName.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateUserName(newName);
                dialog.dismiss();
            } else {
                editTextName.setError("Name cannot be empty");
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateUserName(String newName) {
        progressBar.setVisibility(View.VISIBLE);
        userDatabaseReference.child("name").setValue(newName)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(Account.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                        userNameText.setText(newName);

                        if (currentUser != null) {
                            com.google.firebase.auth.UserProfileChangeRequest profileUpdates = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(newName)
                                    .build();
                            currentUser.updateProfile(profileUpdates);
                        }
                    } else {
                        Toast.makeText(Account.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
