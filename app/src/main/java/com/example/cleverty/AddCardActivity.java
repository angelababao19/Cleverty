package com.example.cleverty;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleverty.model.*;

public class AddCardActivity extends AppCompatActivity {

    private EditText subjectInput;
    private QaAdapter adapter;
    private int editingPos = -1;   // -1 = create new, >=0 = edit existing

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

        /* ---------- EDIT MODE ? ---------- */
        editingPos = getIntent().getIntExtra("SUBJECT_POSITION", -1);
        if (editingPos != -1) {                 // we are editing
            Subject subj = Library.getInstance().getSubjects().get(editingPos);
            subjectInput.setText(subj.getTitle());
            for (FlashCard card : subj.getCards()) {
                adapter.addRow(card.getQuestion(), card.getAnswer());
            }
            adapter.setEditMode(true);          // flag adapter
            adapter.addRow("", "");             // ONE blank at bottom
        } else {
            /* new deck – normal behaviour */
            if (adapter.getItemCount() == 0) adapter.addRow("", "");
        }

        saveBtn.setOnClickListener(v -> saveAndExit());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void saveAndExit() {
        String title = subjectInput.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Subject required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (adapter.getCompletedCards().isEmpty()) {
            Toast.makeText(this, "Add at least one Q/A pair", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject updated = new Subject(title);
        for (QaAdapter.Row row : adapter.getCompletedCards()) {
            updated.addCard(new FlashCard(row.q, row.a));
        }

        if (editingPos == -1) {
            Library.getInstance().addSubject(updated);
        } else {
            Library.getInstance().getSubjects().set(editingPos, updated);
        }
        finish();
    }
}