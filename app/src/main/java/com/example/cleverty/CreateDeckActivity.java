package com.example.cleverty;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateDeckActivity extends AppCompatActivity {

    private TextInputEditText deckNameInput;
    private Button saveDeckButton;
    private ImageView backButton;
    private ProgressBar progressBar;

    private DatabaseReference userDecksRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_deck);

        // --- Initialize Views ---
        deckNameInput = findViewById(R.id.deck_name_input);
        saveDeckButton = findViewById(R.id.save_deck_button);
        backButton = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.create_deck_progress);

        // --- Setup Firebase ---
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            userDecksRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(firebaseAuth.getCurrentUser().getUid()).child("decks");
        } else {
            // No user is logged in, they shouldn't be here. Finish the activity.
            Toast.makeText(this, "You must be logged in to create a deck.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Setup Click Listeners ---
        backButton.setOnClickListener(v -> finish());
        saveDeckButton.setOnClickListener(v -> saveDeckToFirebase());
    }

    private void saveDeckToFirebase() {
        String deckName = deckNameInput.getText().toString().trim();

        // Validate that the deck name is not empty
        if (TextUtils.isEmpty(deckName)) {
            deckNameInput.setError("Deck name cannot be empty");
            deckNameInput.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveDeckButton.setEnabled(false);

        // Create a new Deck object. We initialize the card count to 0.
        Deck newDeck = new Deck(deckName, 0);

        // Push the new deck to Firebase. .push() creates a unique key for this new deck.
        userDecksRef.push().setValue(newDeck).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            saveDeckButton.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(CreateDeckActivity.this, "Deck created successfully!", Toast.LENGTH_SHORT).show();

                // TODO: In the future, you would navigate to a new activity to add cards to this deck.
                // For now, just finish this activity and go back to the homepage.
                finish();
            } else {
                Toast.makeText(CreateDeckActivity.this, "Failed to create deck: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
