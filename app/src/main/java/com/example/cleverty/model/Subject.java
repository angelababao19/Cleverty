package com.example.cleverty.model;
import java.io.Serializable; // 1. IMPORT THIS
import java.util.ArrayList;

/**
 * Represents a subject or deck, containing a list of FlashCard objects.
 * Must implement Serializable to be passed between activities in an Intent.
 */
public class Subject implements Serializable { // 2. ADD "implements Serializable"

    private String title;
    private ArrayList<FlashCard> cards;

    public Subject(String title) {
        this.title = title;
        this.cards = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<FlashCard> getCards() {
        return cards;
    }

    public void addCard(FlashCard card) {
        this.cards.add(card);
    }
}
