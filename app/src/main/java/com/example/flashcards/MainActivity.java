package com.example.flashcards;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;               // 1. added
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcards.model.*;

public class MainActivity extends AppCompatActivity {

    private SubjectAdapter adapter;
    private RecyclerView grid;
    private TextView emptyTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grid     = findViewById(R.id.subjectGrid);
        emptyTxt = findViewById(R.id.emptyTxt);
        ImageButton addBtn = findViewById(R.id.imageButton);

        addBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddCardActivity.class)));

        adapter = new SubjectAdapter(Library.getInstance().getSubjects(), this::openSubject);
        grid.setAdapter(adapter);

        refreshEmptyState();
    }

    private void openSubject(Subject subject) {
        Intent i = new Intent(this, FlashActivity.class);
        i.putExtra("SUBJECT_TITLE", subject.getTitle());
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        refreshEmptyState();
    }

    /* package-visible so SubjectAdapter can call it after delete / pin */
    void refreshEmptyState() {
        boolean has = !Library.getInstance().getSubjects().isEmpty();
        grid.setVisibility(has ? View.VISIBLE : View.GONE);
        emptyTxt.setVisibility(has ? View.GONE : View.VISIBLE);
    }
}