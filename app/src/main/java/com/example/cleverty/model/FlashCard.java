package com.example.cleverty.model;

import java.io.Serializable; // 1. IMPORT THIS

/**
 * Represents a single flashcard with a question and an answer.
 * Must implement Serializable to be passed between activities in an Intent.
 */
public class FlashCard implements Serializable { // 2. ADD "implements Serializable"

    private String question;
    private String answer;

    public FlashCard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
