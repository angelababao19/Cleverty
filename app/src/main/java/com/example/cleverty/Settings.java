package com.example.cleverty;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ImageView goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(v -> finish());

        // --- Setup all the list items ---
        setupSettingsItem(R.id.rate_us_button, R.drawable.ic_star, "Rate us on the Play Store", null);
        setupSettingsItem(R.id.follow_x_button, R.drawable.ic_x_logo, "Follow us on X", null);
        setupSettingsItem(R.id.like_fb_button, R.drawable.ic_facebook_logo, "Like us on Facebook", null);
        setupSettingsItem(R.id.help_button, R.drawable.ic_help, "Help", null);
        setupSettingsItem(R.id.terms_button, R.drawable.ic_document, "Terms and Condition", null);
        setupSettingsItem(R.id.privacy_button, R.drawable.ic_lock, "Privacy Policy", null);

        // --- Danger Zone Section with custom click logic ---
        setupSettingsItem(R.id.delete_account_button, R.drawable.ic_delete, "Delete Account", () -> {
            showConfirmationDialog(
                    "Are you sure you want to delete this account?",
                    () -> {
                        // This is the action for "YES" on DELETE
                        Toast.makeText(this, "Account deletion logic would go here.", Toast.LENGTH_SHORT).show();
                        // Example: Call API to delete account, then perform logout
                        performLogout();
                    }
            );
        });

        // Tint the delete icon red
        View deleteAccountItem = findViewById(R.id.delete_account_button);
        ImageView deleteIcon = deleteAccountItem.findViewById(R.id.item_icon);
        deleteIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));

        // --- Logout Button Logic ---
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            showConfirmationDialog(
                    "Are you sure you want to log out?",
                    this::performLogout // This is the action for "YES" on LOGOUT
            );
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // --- Helper Method for Showing the Custom Dialog ---
    private void showConfirmationDialog(String message, Runnable onYesAction) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirmation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Makes the dialog background transparent to show our custom shape

        TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
        Button buttonYes = dialog.findViewById(R.id.button_yes);
        Button buttonNo = dialog.findViewById(R.id.button_no);

        dialogMessage.setText(message);

        buttonYes.setOnClickListener(v -> {
            dialog.dismiss();
            onYesAction.run(); // Execute the action passed to the method
        });

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // --- Helper Method for Setting up List Items ---
    private void setupSettingsItem(int viewId, int iconResId, String text, Runnable action) {
        View settingsItem = findViewById(viewId);
        ImageView icon = settingsItem.findViewById(R.id.item_icon);
        TextView textView = settingsItem.findViewById(R.id.item_text);

        icon.setImageResource(iconResId);
        textView.setText(text);

        // If an action is provided, set a click listener. Otherwise, do nothing.
        if (action != null) {
            settingsItem.setOnClickListener(v -> action.run());
        } else {
            // Default action if none is provided
            settingsItem.setOnClickListener(v -> {
                Toast.makeText(Settings.this, text + " clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // --- Helper Method for the Actual Logout Logic ---
    private void performLogout() {
        Intent intent = new Intent(Settings.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(Settings.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
