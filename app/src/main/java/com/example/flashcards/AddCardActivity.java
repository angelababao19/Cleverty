package com.example.flashcards;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcards.model.*;

public class AddCardActivity extends AppCompatActivity {

    private EditText subjectInput;
    private QaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        subjectInput = findViewById(R.id.subjectInput);
        RecyclerView qaList = findViewById(R.id.qaList);
        Button saveBtn = findViewById(R.id.saveButton);

        adapter = new QaAdapter();
        qaList.setLayoutManager(new LinearLayoutManager(this));
        qaList.setAdapter(adapter);

        saveBtn.setOnClickListener(v -> saveAndExit());

        /* white back arrow - close screen */
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void saveAndExit() {
        // 1. Get the text from the input field and remove any leading/trailing whitespace.
        String title = subjectInput.getText().toString().trim();

        // 2. Check if the resulting title is empty.
        if (title.isEmpty()) {
            // 3. If it's empty, show a message to the user and stop executing the method.
            Toast.makeText(this, "Subject required", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the title is valid, proceed to create the subject and add cards.
        Subject subject = new Subject(title);
        for (QaAdapter.Row row : adapter.getCompletedCards()) {
            subject.addCard(new FlashCard(row.q, row.a));
        }
        if (subject.getCards().isEmpty()) {
            Toast.makeText(this, "Add at least one Q/A pair", Toast.LENGTH_SHORT).show();
            return;
        }
        Library.getInstance().addSubject(subject);
        finish(); // Close the activity and return to the main screen.
    }
}
