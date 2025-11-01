package com.example.flashcards;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;
import com.example.flashcards.model.FlashCard;
import com.example.flashcards.model.Library;
import com.example.flashcards.model.Subject;
import java.util.*;

public class FlashActivity extends AppCompatActivity {

    /* UI */
    private TextView cardText, pageCounter;
    private View cardSurface;               // the card we drag
    private Button nextBtn;                 // only visible after answer
    private LinearLayout bottomIcons;       // hide while dragging

    /* data */
    private List<FlashCard> cards;
    private int idx = 0;
    private boolean showAnswer = false;
    private Subject currentSubject;

    /* drag & score */
    private float downX, downY;
    private int correct = 0, failed = 0;
    private final Handler handler = new Handler();

    /* constants */
    private static final int DRAG_THRESHOLD = 150; // px
    private static final long ANIM_DURATION = 250; // ms

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        cardText    = findViewById(R.id.cardText);
        pageCounter = findViewById(R.id.pageCounter);
        cardSurface = findViewById(R.id.cardSurface);
        bottomIcons = findViewById(R.id.bottomIcons);
        nextBtn     = findViewById(R.id.nextButton);      // add this Button in XML

        /* load subject */
        String title = getIntent().getStringExtra("SUBJECT_TITLE");
        for (Subject s : Library.getInstance().getSubjects()) {
            if (s.getTitle().equals(title)) {
                currentSubject = s;
                cards = new ArrayList<>(s.getCards()); // copy so we can shuffle
                break;
            }
        }
        if (currentSubject == null || cards.isEmpty()) {
            Toast.makeText(this, "No cards", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        /* top-bar buttons */
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.shuffleButton).setOnClickListener(v -> shuffleAndRestart());
        findViewById(R.id.imageButton2).setOnClickListener(v -> deleteCurrentCard());

        /* drag listener on the card */
        cardSurface.setOnTouchListener(this::onCardTouch);

        /* long-press still works */
        cardText.setOnLongClickListener(v -> {
            showEditDialog();
            return true;
        });

        showCard();
        updateCounter();
    }

    /* ===================== DRAG LOGIC ===================== */
    @SuppressLint("ClickableViewAccessibility")
    private boolean onCardTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                bottomIcons.setVisibility(View.INVISIBLE);
                return true;

            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getRawX() - downX;
                float deltaY = event.getRawY() - downY;
                cardSurface.setTranslationX(deltaX);
                cardSurface.setTranslationY(deltaY);
                cardSurface.setAlpha(1 - Math.abs(deltaX) / 500f);
                return true;

            case MotionEvent.ACTION_UP:
                float dx = event.getRawX() - downX;
                if (Math.abs(dx) > DRAG_THRESHOLD) {
                    if (dx > 0) answerCorrect(); else answerFailed();
                } else {
                    springBack();           // user cancelled
                }
                return true;
        }
        return false;
    }

    /* ===================== ANSWER HANDLERS ===================== */
    private void answerCorrect() {
        correct++;
        animateCardExit(true);
    }

    private void answerFailed() {
        failed++;
        animateCardExit(false);
    }

    private void animateCardExit(boolean toRight) {
        float targetX = toRight ? 1000f : -1000f;
        cardSurface.animate()
                .translationX(targetX)
                .alpha(0f)
                .setDuration(ANIM_DURATION)
                .withEndAction(this::nextCardOrFinish)
                .start();
    }

    private void springBack() {
        cardSurface.animate()
                .translationX(0f)
                .translationY(0f)
                .alpha(1f)
                .setDuration(ANIM_DURATION)
                .start();
        bottomIcons.setVisibility(View.VISIBLE);
    }

    /* ===================== NAVIGATION ===================== */
    private void nextCardOrFinish() {
        idx++;
        if (idx >= cards.size()) {
            showResult();
            return;
        }
        cardSurface.setTranslationX(0f);
        cardSurface.setAlpha(1f);
        showCard();
        updateCounter();
        bottomIcons.setVisibility(View.VISIBLE);
    }

    private void showResult() {
        String msg = (failed == 0) ? "Perfect! 🎉" : "You're doing great! Keep studying!";
        new AlertDialog.Builder(this)
                .setTitle("Finished")
                .setMessage(msg + "\nCorrect: " + correct + "  Failed: " + failed)
                .setPositiveButton("OK", (d, i) -> finish())
                .setCancelable(false)
                .show();
    }

    /* ===================== HELPERS ===================== */
    private void showCard() {
        if (cards.isEmpty()) return;
        FlashCard c = cards.get(idx);
        cardText.setText(showAnswer ? c.getAnswer() : c.getQuestion());
        showAnswer = false; // reset for next card
    }

    private void updateCounter() {
        pageCounter.setText((idx + 1) + " / " + cards.size());
    }

    private void shuffleAndRestart() {
        Collections.shuffle(cards);
        idx = 0; correct = 0; failed = 0;
        showCard(); updateCounter();
        springBack();
    }

    private void deleteCurrentCard() {
        if (cards.isEmpty()) return;
        cards.remove(idx);
        if (idx >= cards.size()) idx = cards.size() - 1;
        springBack(); showCard(); updateCounter();
    }

    /* ===================== EDIT CARD ===================== */
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
                        showCard();
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Both fields required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}