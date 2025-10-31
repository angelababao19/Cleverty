package com.example.flashcards;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;        // 1. correct dialog
import android.widget.LinearLayout;               // 2. explicit import
import com.example.flashcards.model.FlashCard;
import com.example.flashcards.model.Library;
import com.example.flashcards.model.Subject;
import java.util.List;

public class FlashActivity extends AppCompatActivity {

    private TextView cardText;
    private List<FlashCard> cards;
    private int idx = 0;
    private boolean showAnswer = false;
    private GestureDetector gd;
    private Subject currentSubject;          // keep subject so we can append

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        cardText = findViewById(R.id.cardText);
        ImageButton del   = findViewById(R.id.imageButton2);
        ImageButton addBtn = findViewById(R.id.addButton);   // NEW

        /* which subject? */
        String title = getIntent().getStringExtra("SUBJECT_TITLE");
        for (Subject s : Library.getInstance().getSubjects()) {
            if (s.getTitle().equals(title)) {
                currentSubject = s;
                cards = s.getCards();
                break;
            }
        }

        /* NEW:  always initialise currentSubject even if list empty */
        if (currentSubject == null) {
            /* edge-case: subject was removed while screen open */
            Toast.makeText(this, "Subject not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (cards == null || cards.isEmpty()) {
            cardText.setText("No cards in this subject");
            del.setEnabled(false);
            del.setAlpha(0.5f);
        } else {
            del.setOnClickListener(v -> deleteCard());
        }

        addBtn.setOnClickListener(v -> showAddCardDialog());   // NEW
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        /* swipe + tap detector */
        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) prev(); else next();
                    return true;
                }
                return false;
            }
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                flip(); return true;
            }
        });
        findViewById(R.id.rootLayout).setOnTouchListener((v, e) -> gd.onTouchEvent(e));

        show();
    }

    /* ---------- mini add-card dialog ---------- */
    private void showAddCardDialog() {
        EditText qEt = new EditText(this), aEt = new EditText(this);
        qEt.setHint("Question");
        aEt.setHint("Answer");

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(50, 20, 50, 20);
        lay.addView(qEt);
        lay.addView(aEt);

        new AlertDialog.Builder(this)
                .setTitle("Add new card")
                .setView(lay)
                .setPositiveButton("Save", (d, i) -> {
                    String q = qEt.getText().toString().trim();
                    String a = aEt.getText().toString().trim();
                    if (!q.isEmpty() && !a.isEmpty()) {
                        currentSubject.addCard(new FlashCard(q, a));
                        cards = currentSubject.getCards();        // refresh list
                        idx = cards.size() - 1;                   // jump to new card
                        showAnswer = false;
                        show();
                    } else {
                        Toast.makeText(this, "Both fields required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ---------- original helpers ---------- */
    private void show() {
        if (cards.isEmpty()) {
            cardText.setText("No cards left");
            findViewById(R.id.imageButton2).setEnabled(false);
            findViewById(R.id.imageButton2).setAlpha(0.5f);
            return;
        }
        FlashCard c = cards.get(idx);
        cardText.setText(showAnswer ? c.getAnswer() : c.getQuestion());
    }
    private void flip() {
        if (cards.isEmpty()) return;
        showAnswer = !showAnswer; show();
    }
    private void next() {
        if (cards.isEmpty()) return;
        idx = (idx + 1) % cards.size(); showAnswer = false; show();
    }
    private void prev() {
        if (cards.isEmpty()) return;
        idx = (idx - 1 + cards.size()) % cards.size(); showAnswer = false; show();
    }
    private void deleteCard() {
        if (cards.isEmpty()) return;
        cards.remove(idx);
        if (idx >= cards.size() && !cards.isEmpty()) idx = cards.size() - 1;
        showAnswer = false; show();
    }
}