package com.example.cleverty;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cleverty.model.FlashCard;
import com.example.cleverty.model.Library;
import com.example.cleverty.model.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlashActivity extends AppCompatActivity {

    private static final int REVIEW_REQUEST_CODE = 101;
    private static final int DRAG_THRESHOLD = 150;
    private static final long ANIM_DURATION = 250;

    private TextView cardText, pageCounter, emptyText;
    private LinearLayout progressLines;
    private FrameLayout cardContainer;
    private View swipeWrapper;

    private Subject currentSubject;
    private List<FlashCard> cards;
    private int idx = 0;
    private boolean showAnswer = false;
    private boolean noCards = false;

    private int correctCount = 0;
    private int wrongCount = 0;
    private final List<Boolean> answerHistory = new ArrayList<>();

    private float downX, downY;
    private boolean isClick;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        // --- View Binding ---
        cardText = findViewById(R.id.cardText);
        pageCounter = findViewById(R.id.pageCounter);
        progressLines = findViewById(R.id.progressLines);
        cardContainer = findViewById(R.id.cardContainer);
        swipeWrapper = findViewById(R.id.swipeWrapper);
        emptyText = findViewById(R.id.emptyText);
        ImageButton del = findViewById(R.id.imageButton2);
        ImageButton editBtn = findViewById(R.id.editButton); // Find the new edit button
        ImageButton shuffleBtn = findViewById(R.id.shuffleButton);
        ImageButton prevBtn = findViewById(R.id.prevButton);

        // --- Load Initial Deck ---
        String title = getIntent().getStringExtra("SUBJECT_TITLE");
        for (Subject s : Library.getInstance().getSubjects()) {
            if (s.getTitle().equals(title)) {
                currentSubject = s;
                cards = new ArrayList<>(s.getCards());
                break;
            }
        }
        if (currentSubject == null || cards == null || cards.isEmpty()) {
            Toast.makeText(this, "No cards in this deck.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Listeners ---
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        shuffleBtn.setOnClickListener(v -> {
            if (noCards) return;
            Collections.shuffle(cards);
            restartSession();
        });
        del.setOnClickListener(v -> deleteCurrentCard());
        editBtn.setOnClickListener(v -> showEditCardDialog()); // Set listener for the edit button
        prevBtn.setOnClickListener(v -> prev());
        setupTouchListener();

        // --- Start the First Session ---
        restartSession();
    }

    /**
     * NEW METHOD: Displays a dialog to edit the current card's question and answer.
     */
    private void showEditCardDialog() {
        if (noCards || cards.isEmpty()) {
            Toast.makeText(this, "There are no cards to edit.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate the custom layout for the dialog.
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_card, null);
        final EditText editQuestion = dialogView.findViewById(R.id.editQuestion);
        final EditText editAnswer = dialogView.findViewById(R.id.editAnswer);

        // Get the current card and pre-fill the EditTexts with its text.
        FlashCard currentCard = cards.get(idx);
        editQuestion.setText(currentCard.getQuestion());
        editAnswer.setText(currentCard.getAnswer());

        // Create and show the dialog.
        new AlertDialog.Builder(this)
                .setTitle("Edit Card")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newQuestion = editQuestion.getText().toString().trim();
                    String newAnswer = editAnswer.getText().toString().trim();

                    if (!newQuestion.isEmpty() && !newAnswer.isEmpty()) {
                        // Update the FlashCard object with the new text.
                        currentCard.setQuestion(newQuestion);
                        currentCard.setAnswer(newAnswer);

                        // Refresh the currently displayed card to show the changes.
                        showCard();
                        Toast.makeText(this, "Card updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- The rest of your FlashActivity.java code remains the same ---
    // (onActivityResult, restartSession, next, prev, etc.)
    // ... paste the rest of your working FlashActivity code here ...
    // The following is provided for completeness.

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REVIEW_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<FlashCard> reviewCards = (ArrayList<FlashCard>) data.getSerializableExtra("REVIEW_CARDS");
                if (reviewCards != null && !reviewCards.isEmpty()) {
                    this.cards = reviewCards;
                    restartSession();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        }
    }

    private void restartSession() {
        idx = 0;
        showAnswer = false;
        correctCount = 0;
        wrongCount = 0;
        answerHistory.clear();
        if (cards == null || cards.isEmpty()) {
            showEmptyState();
        } else {
            noCards = false;
            cardContainer.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
            showCard();
            updatePageCounter();
            buildProgressLines();
            updateProgressLines();
        }
    }

    private void next(boolean wasCorrect) {
        if (noCards) return;
        if (wasCorrect) correctCount++; else wrongCount++;
        answerHistory.add(wasCorrect);

        if (idx == cards.size() - 1) {
            Intent intent = new Intent(this, CongratulationsActivity.class);
            intent.putExtra("CORRECT_COUNT", correctCount);
            intent.putExtra("WRONG_COUNT", wrongCount);
            intent.putExtra("ALL_CARDS", (ArrayList<FlashCard>) cards);
            intent.putExtra("ANSWER_HISTORY", (ArrayList<Boolean>) answerHistory);
            startActivityForResult(intent, REVIEW_REQUEST_CODE);
            return;
        }
        idx++;
        showAnswer = false;
        showCard();
        updatePageCounter();
        updateProgressLines();
    }

    private void prev() {
        if (noCards || idx == 0 || answerHistory.isEmpty()) return;
        boolean lastAnswerWasCorrect = answerHistory.remove(answerHistory.size() - 1);
        if (lastAnswerWasCorrect) correctCount--; else wrongCount--;
        idx--;
        showAnswer = false;
        showCard();
        updatePageCounter();
        updateProgressLines();
    }

    private void setupTouchListener() {
        cardText.setOnTouchListener((v, event) -> {
            final int TAP_SLOP = 20;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();
                    isClick = true;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (isClick && (Math.abs(event.getRawX() - downX) > TAP_SLOP || Math.abs(event.getRawY() - downY) > TAP_SLOP)) {
                        isClick = false;
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isClick) {
                        toggleCard();
                    } else {
                        float dx = event.getRawX() - downX;
                        if (Math.abs(dx) > DRAG_THRESHOLD) {
                            animateCardExit(dx);
                        } else {
                            springBack();
                        }
                    }
                    return true;
            }
            return false;
        });
    }

    private void animateCardExit(float dx) {
        if (noCards) return;
        float exitX = dx > 0 ? 1000f : -1000f;
        swipeWrapper.animate()
                .translationX(exitX)
                .alpha(0f)
                .setDuration(ANIM_DURATION)
                .withEndAction(() -> {
                    swipeWrapper.setTranslationX(0f);
                    swipeWrapper.setAlpha(1f);
                    next(dx > 0); // true if swipe right (correct)
                })
                .start();
    }

    private void toggleCard() { if (noCards) return; showAnswer = !showAnswer; showCard(); }
    private void springBack() { swipeWrapper.animate().translationX(0f).alpha(1f).setDuration(ANIM_DURATION).start(); }
    private void showCard() { if (noCards) return; cardText.setText(showAnswer ? cards.get(idx).getAnswer() : cards.get(idx).getQuestion());}
    private void updatePageCounter() { if (noCards) pageCounter.setText("0 / 0"); else pageCounter.setText((idx + 1) + " / " + cards.size());}

    private void buildProgressLines() {
        progressLines.removeAllViews();
        if (noCards) return;
        for (int i = 0; i < cards.size(); i++) {
            View line = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            lp.setMargins(2, 0, 2, 0);
            line.setLayoutParams(lp);
            line.setBackgroundResource(R.drawable.line_gray);
            progressLines.addView(line);
        }
    }
    private void updateProgressLines() {
        if (noCards) return;
        for (int i = 0; i < progressLines.getChildCount(); i++) {
            View line = progressLines.getChildAt(i);
            if (i < answerHistory.size()) {
                line.setBackgroundResource(answerHistory.get(i) ? R.drawable.line_white : R.drawable.line_red);
            } else {
                line.setBackgroundResource(R.drawable.line_gray);
            }
        }
    }
    private void showEmptyState() {
        noCards = true;
        cardContainer.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        updatePageCounter();
        progressLines.removeAllViews();
    }
    private void deleteCurrentCard() {
        if (noCards) return;
        currentSubject.getCards().remove(cards.get(idx));
        cards.remove(idx);
        if (idx >= cards.size() && idx > 0) {
            idx = cards.size() - 1;
        }
        restartSession();
    }
}
