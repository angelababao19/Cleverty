package com.example.cleverty;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HelpPage extends AppCompatActivity {

    private ImageView goBackButton;
    private Button contactSupportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_page);

        // --- Initialize Views ---
        goBackButton = findViewById(R.id.goBack);
        contactSupportButton = findViewById(R.id.contact_support_button);

        // --- Setup Click Listeners ---

        // Set the listener for the back button at the top of the page
        goBackButton.setOnClickListener(v -> {
            // finish() simply closes the current activity and goes back to the previous one (Settings)
            finish();
        });

        // Set the listener for the "Contact Support" button
        contactSupportButton.setOnClickListener(v -> {
            // Create an Intent to open an email client
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            // Use "mailto:" to specify it's an email action
            emailIntent.setData(Uri.parse("mailto:"));

            // Set the recipient's email address (REPLACE WITH YOUR EMAIL)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"your.support.email@example.com"});

            // Set a default subject line for the email
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Cleverty App Support Request");

            // Check if there is an email app available to handle the intent
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                // If no email app is found, show a toast message
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
}
