package com.example.cleverty;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import com.google.firebase.auth.FirebaseAuth;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ImageView goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(v -> finish());

        // --- Setup all the list items with their specific actions ---
        setupSettingsItem(R.id.rate_us_button, R.drawable.ic_star, "Rate us on the Play Store", this::openPlayStore);
        setupSettingsItem(R.id.follow_x_button, R.drawable.ic_x_logo, "Follow us on X", () -> openUrl("https://x.com/your_x_handle"));
        setupSettingsItem(R.id.like_fb_button, R.drawable.ic_facebook_logo, "Like us on Facebook", () -> openUrl("https://facebook.com/your_facebook_page"));

        // NOTE: The IDs for help_button, terms_button, etc. must exist in your XML from the <include> tags.
        setupSettingsItem(R.id.help_button, R.drawable.ic_help, "Help", () -> {
            startActivity(new Intent(this, HelpPage.class));
        });

        // Both Terms and Privacy will point to the same TermsPage activity as requested
        setupSettingsItem(R.id.terms_button, R.drawable.ic_document, "Terms and Condition", () -> startActivity(new Intent(this, TermsPage.class)));
        setupSettingsItem(R.id.privacy_button, R.drawable.ic_lock, "Privacy Policy", () -> startActivity(new Intent(this, TermsPage.class)));

        // --- Logout Button Logic ---
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            showConfirmationDialog(
                    "Are you sure you want to log out?",
                    this::performLogout
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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Makes the dialog background transparent

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
        // Check if the view exists before trying to modify it
        if (settingsItem == null) return;

        ImageView icon = settingsItem.findViewById(R.id.item_icon);
        TextView textView = settingsItem.findViewById(R.id.item_text);

        icon.setImageResource(iconResId);
        textView.setText(text);

        // If an action is provided, set a click listener.
        if (action != null) {
            settingsItem.setOnClickListener(v -> action.run());
        }
    }

    // --- Helper Method for the Actual Logout Logic ---
    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        // Redirect to the login page and clear all previous activities from the back stack
        Intent intent = new Intent(Settings.this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(Settings.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // --- NEW HELPER METHODS FOR REDIRECTION ---

    private void openPlayStore() {
        // Use your app's package name here
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            // If the Play Store app is not installed, open it in a web browser
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
