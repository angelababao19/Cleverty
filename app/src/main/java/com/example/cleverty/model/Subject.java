package com.example.cleverty.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Subject implements Serializable {

    private String title;
    // CRITICAL: This MUST be ArrayList to avoid casting errors later
    private ArrayList<FlashCard> cards;
    private boolean isPinned;

    public Subject(String title) {
        this.title = title;
        this.cards = new ArrayList<>();
        this.isPinned = false; // Always initialize as not pinned
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

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
}
