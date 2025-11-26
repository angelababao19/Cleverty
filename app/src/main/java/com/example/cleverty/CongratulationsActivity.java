package com.example.cleverty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cleverty.model.FlashCard;
import java.util.ArrayList;

public class CongratulationsActivity extends AppCompatActivity {

    private ArrayList<FlashCard> wrongCardsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulations);

        // Find all views from the layout, including the new TextView
        TextView perfectTitleTextView = findViewById(R.id.perfectTitleTextView); // The "Perfect!" title
        TextView titleTextView = findViewById(R.id.titleTextView); // The main title
        TextView correctText = findViewById(R.id.correctText);
        TextView wrongText = findViewById(R.id.wrongText);
        LinearLayout finalProgressLines = findViewById(R.id.finalProgressLines);
        Button goBackButton = findViewById(R.id.tryAgainButton);
        Button reviewWrongsButton = findViewById(R.id.button2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Retrieve data from FlashActivity
            int correct = extras.getInt("CORRECT_COUNT", 0);
            int wrong = extras.getInt("WRONG_COUNT", 0);
            ArrayList<FlashCard> allCards = (ArrayList<FlashCard>) extras.getSerializable("ALL_CARDS");
            ArrayList<Boolean> history = (ArrayList<Boolean>) extras.getSerializable("ANSWER_HISTORY");

            // ==========================================================
            //  START: This is the updated title logic
            // ==========================================================
            if (wrong == 0 && correct > 0) {
                // If the user got 0 wrong answers (a perfect score)
                perfectTitleTextView.setVisibility(View.VISIBLE); // Show "Perfect!"
                titleTextView.setText("Congratulations!");         // Set the second line text
            } else if (correct == 0 && wrong > 0) {
                // If the user got 0 correct answers
                perfectTitleTextView.setVisibility(View.GONE);     // Hide "Perfect!"
                titleTextView.setText("Keep Practicing!");         // Set the main title
            } else {
                // For any other score (a mix of correct and wrong)
                perfectTitleTextView.setVisibility(View.GONE);     // Hide "Perfect!"
                titleTextView.setText("Congratulations!");         // Set the main title
            }
            // ========================================================
            //  END: End of the updated title logic
            // ========================================================

            // Set the result text fields
            correctText.setText("Correct: " + correct);
            wrongText.setText("Wrong: " + wrong);

            // Build the final progress bar and filter out the wrong cards
            if (history != null && allCards != null && history.size() == allCards.size()) {
                buildFinalProgressLines(finalProgressLines, history);
                for (int i = 0; i < allCards.size(); i++) {
                    if (!history.get(i)) { // If the answer was wrong...
                        wrongCardsList.add(allCards.get(i)); // ...add the card to our list.
                    }
                }
            }
        }

        // Only show the "Review" button if there are wrong answers to review.
        reviewWrongsButton.setVisibility(wrongCardsList.isEmpty() ? View.GONE : View.VISIBLE);

        // --- Button Listeners (no changes here) ---
        goBackButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        reviewWrongsButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("REVIEW_CARDS", wrongCardsList);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * Creates and adds colored views to a LinearLayout to represent the user's answers.
     */
    private void buildFinalProgressLines(LinearLayout container, ArrayList<Boolean> history) {
        if (container == null) return;
        container.removeAllViews();
        for (boolean wasCorrect : history) {
            View line = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            lp.setMargins(2, 0, 2, 0);
            line.setLayoutParams(lp);
            int lineDrawable = wasCorrect ? R.drawable.line_white : R.drawable.line_red;
            line.setBackgroundResource(lineDrawable);
            container.addView(line);
        }
    }
}
