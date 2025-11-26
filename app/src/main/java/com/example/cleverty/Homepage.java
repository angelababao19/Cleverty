package com.example.cleverty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Homepage extends AppCompatActivity {

    // UI Views
    private RecyclerView recentDecksRecyclerView;
    private LinearLayout emptyStateLayout;
    private Button createDeckButton;
    private BottomNavigationView bottomNav;

    // Firebase
    private DatabaseReference userDecksRef;
    private FirebaseAuth firebaseAuth;

    // --- UNCOMMENTED AND INITIALIZED ---
    private DeckAdapter deckAdapter;
    private ArrayList<Deck> deckList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // --- Initialize Views ---
        recentDecksRecyclerView = findViewById(R.id.recent_decks_recyclerview);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        createDeckButton = findViewById(R.id.create_deck_button);
        bottomNav = findViewById(R.id.bottomNav);

        // --- Setup Firebase ---
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            userDecksRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(firebaseAuth.getCurrentUser().getUid()).child("decks");
        }

        // --- Setup Click Listeners ---
        setupClickListeners();

        // --- Setup RecyclerView ---
        setupRecyclerView();

        // --- Load Data ---
        loadDecksFromFirebase();

    }

    private void setupClickListeners() {
        createDeckButton.setOnClickListener(v -> {
            Intent intent = new Intent(Homepage.this, CreateDeckActivity.class);
            startActivity(intent);
        });

        // Setup Bottom Navigation
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_timer) {
                startActivity(new Intent(getApplicationContext(), Timer.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(getApplicationContext(), Account.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        recentDecksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- UNCOMMENTED AND IMPLEMENTED ---
        deckList = new ArrayList<>();
        deckAdapter = new DeckAdapter(this, deckList);
        recentDecksRecyclerView.setAdapter(deckAdapter);
    }

    private void loadDecksFromFirebase() {
        if (userDecksRef == null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recentDecksRecyclerView.setVisibility(View.GONE);
            return;
        }

        userDecksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // --- UNCOMMENTED AND IMPLEMENTED ---
                deckList.clear(); // Clear the old list before adding new data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Deck deck = snapshot.getValue(Deck.class);
                    if (deck != null) {
                        deckList.add(deck);
                    }
                }

                // Check if the list is empty
                if (deckList.isEmpty()) {
                    recentDecksRecyclerView.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    recentDecksRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    // Notify the adapter that the data set has changed so it can update the UI
                    deckAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Homepage.this, "Failed to load decks: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
