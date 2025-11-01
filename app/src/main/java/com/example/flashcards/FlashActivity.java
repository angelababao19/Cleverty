package com.example.flashcards;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;
import com.example.flashcards.model.FlashCard;
import com.example.flashcards.model.Library;
import com.example.flashcards.model.Subject;
import java.util.List;
import java.util.Collections;

public class FlashActivity extends AppCompatActivity {

    private TextView cardText;
    private TextView pageCounter;
    private List<FlashCard> cards;
    private int idx = 0;
    private boolean showAnswer = false;
    private GestureDetector gd;
    private Subject currentSubject;

    /* long-press helpers */
    private final Handler longPressHandler = new Handler();
    private float downX, downY;
    private final Runnable longPressRunnable = this::showEditDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        cardText    = findViewById(R.id.cardText);
        pageCounter = findViewById(R.id.pageCounter);
        ImageButton del      = findViewById(R.id.imageButton2);
        ImageButton addBtn   = findViewById(R.id.addButton);
        ImageButton shuffleBtn = findViewById(R.id.shuffleButton);

        String title = getIntent().getStringExtra("SUBJECT_TITLE");
        for (Subject s : Library.getInstance().getSubjects()) {
            if (s.getTitle().equals(title)) {
                currentSubject = s;
                cards = s.getCards();
                break;
            }
        }
        if (currentSubject == null) {
            Toast.makeText(this, "Subject not found", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        if (cards == null || cards.isEmpty()) {
            cardText.setText("No cards in this subject");
            del.setEnabled(false); del.setAlpha(0.5f);
        } else {
            del.setOnClickListener(v -> deleteCard());
        }

        addBtn.setOnClickListener(v -> showAddCardDialog());
        shuffleBtn.setOnClickListener(v -> shuffleCards());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        /* swipe detector */
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
        });

        /* ONE touch dispatcher: swipe → tap → long-press */
        findViewById(R.id.rootLayout).setOnTouchListener((v, event) -> {
            boolean handled = gd.onTouchEvent(event);      // 1. swipe first
            if (handled) return true;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX(); downY = event.getY();
                    longPressHandler.postDelayed(longPressRunnable, 500); // 500 ms long-press threshold
                    return true;

                case MotionEvent.ACTION_UP:
                    longPressHandler.removeCallbacks(longPressRunnable);
                    float upX = event.getX(), upY = event.getY();
                    if (Math.abs(upX - downX) < 20 && Math.abs(upY - downY) < 20) {
                        flip();                                    // 2. simple tap → flip
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(event.getX() - downX) > 20 || Math.abs(event.getY() - downY) > 20) {
                        longPressHandler.removeCallbacks(longPressRunnable); // finger moved → cancel long-press
                    }
                    break;
            }
            return false;
        });

        show(); updatePageCounter();
    }

    /* ---------- LONG-PRESS EDIT ---------- */
    private void showEditDialog() {
        if (cards == null || cards.isEmpty()) return;
        FlashCard current = cards.get(idx);

        EditText qEt = new EditText(this), aEt = new EditText(this);
        qEt.setHint("Question");   qEt.setText(current.getQuestion());
        aEt.setHint("Answer");     aEt.setText(current.getAnswer());

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(50, 20, 50, 20);
        lay.addView(qEt); lay.addView(aEt);

        new AlertDialog.Builder(this)
                .setTitle("Edit card")
                .setView(lay)
                .setPositiveButton("Save", (d, i) -> {
                    String newQ = qEt.getText().toString().trim();
                    String newA = aEt.getText().toString().trim();
                    if (!newQ.isEmpty() && !newA.isEmpty()) {
                        current.setQuestion(newQ);
                        current.setAnswer(newA);
                        show();              // refresh screen
                        updatePageCounter(); // keep counter correct
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Both fields required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ---------- SHUFFLE ENTIRE DECK ---------- */
    private void shuffleCards() {
        if (cards == null || cards.size() < 2) return;
        Collections.shuffle(cards);
        idx = 0; showAnswer = false; show(); updatePageCounter();
    }

    /* ---------- ADD CARD ---------- */
    private void showAddCardDialog() {
        EditText qEt = new EditText(this), aEt = new EditText(this);
        qEt.setHint("Question"); aEt.setHint("Answer");
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setPadding(50, 20, 50, 20);
        lay.addView(qEt); lay.addView(aEt);
        new AlertDialog.Builder(this)
                .setTitle("Add new card")
                .setView(lay)
                .setPositiveButton("Save", (d, i) -> {
                    String q = qEt.getText().toString().trim();
                    String a = aEt.getText().toString().trim();
                    if (!q.isEmpty() && !a.isEmpty()) {
                        currentSubject.addCard(new FlashCard(q, a));
                        cards = currentSubject.getCards();
                        idx = cards.size() - 1;
                        showAnswer = false; show(); updatePageCounter();
                    } else {
                        Toast.makeText(this, "Both fields required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ---------- PAGE COUNTER ---------- */
    private void updatePageCounter() {
        if (cards == null || cards.isEmpty()) {
            pageCounter.setText("0 / 0"); return;
        }
        pageCounter.setText((idx + 1) + " / " + cards.size());
    }

    /* ---------- ORIGINAL HELPERS ---------- */
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
        showAnswer = !showAnswer; show(); updatePageCounter();
    }
    private void next() {
        if (cards.isEmpty()) return;
        idx = (idx + 1) % cards.size(); showAnswer = false; show(); updatePageCounter();
    }
    private void prev() {
        if (cards.isEmpty()) return;
        idx = (idx - 1 + cards.size()) % cards.size(); showAnswer = false; show(); updatePageCounter();
    }
    private void deleteCard() {
        if (cards.isEmpty()) return;
        cards.remove(idx);
        if (idx >= cards.size() && !cards.isEmpty()) idx = cards.size() - 1;
        showAnswer = false; show(); updatePageCounter();
    }
}