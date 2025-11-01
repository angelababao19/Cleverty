package com.example.flashcards.model;

public class FlashCard {
    private String question;
    private String answer;

    public FlashCard(String question, String answer) {
        this.question = question;
        this.answer   = answer;
    }
    public String getQuestion() { return question; }
    public String getAnswer()   { return answer;   }
    public void setQuestion(String q) { this.question = q; }
    public void setAnswer(String a)   { this.answer = a;   }
}