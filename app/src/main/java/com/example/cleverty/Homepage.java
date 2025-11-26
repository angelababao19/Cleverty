package com.example.cleverty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// It looks like you might have moved these model classes into the main package.
// If Subject, Library, etc. are still in a 'model' sub-package, you'll need to adjust this import.
// For example: import com.example.cleverty.model.*;
import com.example.cleverty.model.Subject;
import com.example.cleverty.model.Library;
import com.example.cleverty.SubjectAdapter;
import com.example.cleverty.AddCardActivity;
import com.example.cleverty.FlashActivity;


public class Homepage extends AppCompatActivity {

    // UI Views from both projects
    private BottomNavigationView bottomNav;
    private RecyclerView grid;
    private TextView emptyTxt;
    private ImageButton addBtn;
    private SubjectAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // --- Initialize ALL views from your layout ---
        grid     = findViewById(R.id.subjectGrid);
        emptyTxt = findViewById(R.id.emptyTxt);
        // Make sure the ID in your XML is 'add_deck_button' as we created in the merged layout
        addBtn   = findViewById(R.id.imageButton);
        bottomNav = findViewById(R.id.bottomNav);


        // --- Setup Listeners and Adapters ---

        // This is from your teammate's code
        addBtn.setOnClickListener(v ->
                startActivity(new Intent(Homepage.this, AddCardActivity.class)));

        // This is also from your teammate's code
        // It initializes the adapter to show the decks
        adapter = new SubjectAdapter(Library.getInstance().getSubjects(), this::openSubject);
        grid.setAdapter(adapter);

        // --- This is your code, now correctly called ---
        setupBottomNavigation();

        // Initial check to see if the list is empty
        refreshEmptyState();
    }

    // This method is called when the activity becomes visible again
    @Override
    protected void onResume() {
        super.onResume();
        // This is from your teammate's code. It refreshes the list when you come back to this screen.
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        refreshEmptyState();
    }

    // Method from your teammate's code to open a deck
    private void openSubject(Subject subject) {
        Intent i = new Intent(this, FlashActivity.class);
        i.putExtra("SUBJECT_TITLE", subject.getTitle());
        startActivity(i);
    }

    // Method from your teammate's code to show/hide the "empty" text.
    // It is now correctly part of the Homepage class.
    void refreshEmptyState() {
        boolean has = !Library.getInstance().getSubjects().isEmpty();
        grid.setVisibility(has ? View.VISIBLE : View.GONE);
        emptyTxt.setVisibility(has ? View.GONE : View.VISIBLE);
    }

    // Your method for setting up the bottom navigation bar
    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on the home screen, do nothing
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
}
