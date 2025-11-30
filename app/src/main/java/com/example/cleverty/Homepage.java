package com.example.cleverty;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleverty.model.Library;
import com.example.cleverty.model.Subject;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Implement the listener interface from the adapter
public class Homepage extends AppCompatActivity implements SubjectAdapter.OnSubjectInteractionListener {

    // UI Views
    private RecyclerView grid;
    private TextView emptyTxt;
    private BottomNavigationView bottomNav;
    private TextInputEditText searchInput;

    // Data
    private SubjectAdapter adapter;
    private List<Subject> subjectList; // This list will hold the currently displayed subjects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        initializeViews();
        setupBottomNavigation();
        setupSearchListener();

        ImageButton addBtn = findViewById(R.id.imageButton);
        addBtn.setOnClickListener(v ->
                startActivity(new Intent(Homepage.this, AddCardActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When the screen is shown, get the current search text and refresh the list
        String currentQuery = searchInput.getText() != null ? searchInput.getText().toString() : "";
        loadAndDisplaySubjects(currentQuery);
    }

    private void initializeViews() {
        grid = findViewById(R.id.subjectGrid);
        emptyTxt = findViewById(R.id.emptyTxt);
        bottomNav = findViewById(R.id.bottomNav);
        searchInput = findViewById(R.id.search_input);

        View rootLayout = findViewById(R.id.main);

        // Set the touch listener on the root layout instead of the RecyclerView
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboardAndClearFocus();
                return false;
            }
        });
    }

    // --- NEW: Helper method to hide the keyboard and clear focus ---
    private void hideKeyboardAndClearFocus() {
        // Clear focus from the search input field
        searchInput.clearFocus();

        // Get the Input Method Manager service
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Find the currently focused view
        View currentFocus = getCurrentFocus();

        // If a view has focus, hide the keyboard from it
        if (currentFocus != null && imm != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
    // ---------------------------------------------------------------

    // Method to set up the search listener
    private void setupSearchListener() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Do nothing */ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // As the user types, immediately filter the list
                loadAndDisplaySubjects(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { /* Do nothing */ }
        });
    }

    // This method handles filtering, sorting, and displaying the list
    private void loadAndDisplaySubjects(String query) {
        // 1. Get the full, raw list from the Library
        List<Subject> rawList = Library.getInstance().getSubjects();

        // 2. Filter the list based on the search query
        List<Subject> filteredList;
        if (query == null || query.trim().isEmpty()) {
            filteredList = new ArrayList<>(rawList); // No query, so we use the full list
        } else {
            String lowerCaseQuery = query.toLowerCase();
            // Use a stream to filter the list
            filteredList = rawList.stream()
                    .filter(subject -> subject.getTitle().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        // 3. Sort the filtered list (pinned items always come first)
        Collections.sort(filteredList, (s1, s2) -> {
            if (s1.isPinned() && !s2.isPinned()) return -1;
            if (!s1.isPinned() && s2.isPinned()) return 1;
            return s1.getTitle().compareTo(s2.getTitle());
        });

        // 4. Update the adapter with the new filtered and sorted list
        if (adapter == null) {
            this.subjectList = filteredList;
            adapter = new SubjectAdapter(this.subjectList, this);
            grid.setAdapter(adapter);
        } else {
            this.subjectList.clear();
            this.subjectList.addAll(filteredList);
            adapter.notifyDataSetChanged();
        }

        // 5. Refresh the "empty" text view based on the filtered list
        refreshEmptyState(filteredList);
    }

    // --- IMPLEMENTATION OF THE ADAPTER'S LISTENER INTERFACE ---
    @Override
    public void onSubjectClicked(Subject s) {
        Intent i = new Intent(this, FlashActivity.class);
        i.putExtra("SUBJECT_TITLE", s.getTitle());
        startActivity(i);
    }

    @Override
    public void onPinClicked(Subject s) {
        s.setPinned(true);
        loadAndDisplaySubjects(searchInput.getText().toString());
    }

    @Override
    public void onUnpinClicked(Subject s) {
        s.setPinned(false);
        loadAndDisplaySubjects(searchInput.getText().toString());
    }

    @Override
    public void onEditClicked(Subject s) {
        Intent intent = new Intent(this, AddCardActivity.class);
        intent.putExtra("SUBJECT_POSITION", Library.getInstance().getSubjects().indexOf(s));
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(Subject s) {
        showDeleteConfirmationDialog(s);
    }

    // This method now updates the empty text message based on the search
    public void refreshEmptyState(List<Subject> currentList) {
        boolean hasSubjects = currentList != null && !currentList.isEmpty();
        grid.setVisibility(hasSubjects ? View.VISIBLE : View.GONE);
        emptyTxt.setVisibility(hasSubjects ? View.GONE : View.VISIBLE);

        String currentQuery = searchInput.getText() != null ? searchInput.getText().toString() : "";
        if (currentQuery.isEmpty()) {
            emptyTxt.setText("No decks yet!\nTap ＋ to create one.");
        } else {
            emptyTxt.setText("No decks found for your search.");
        }
    }

    // --- Helper methods for dialogs and navigation ---
    public void showDeleteConfirmationDialog(Subject subjectToDelete) {
        showConfirmationDialog(
                "Are you sure you want to delete this deck?",
                () -> performDeckDeletion(subjectToDelete)
        );
    }

    private void performDeckDeletion(Subject subjectToDelete) {
        Library.getInstance().getSubjects().remove(subjectToDelete);
        String currentQuery = searchInput.getText() != null ? searchInput.getText().toString() : "";
        loadAndDisplaySubjects(currentQuery);
        Toast.makeText(this, "Deck deleted", Toast.LENGTH_SHORT).show();
    }

    private void showConfirmationDialog(String message, Runnable onYesAction) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirmation);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
        Button buttonYes = dialog.findViewById(R.id.button_yes);
        Button buttonNo = dialog.findViewById(R.id.button_no);

        dialogMessage.setText(message);

        buttonYes.setOnClickListener(v -> {
            dialog.dismiss();
            onYesAction.run();
        });

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupBottomNavigation() {
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
}
