package com.example.cleverty;

public class Deck {
    private String deckName;
    private int cardCount;
    // You can add more fields here later, like a deck ID or creation date

    // IMPORTANT: A default, no-argument constructor is required for Firebase
    public Deck() {
    }

    public Deck(String deckName, int cardCount) {
        this.deckName = deckName;
        this.cardCount = cardCount;
    }

    // --- Getters and Setters ---
    // These are also required for Firebase to work correctly
    public String getDeckName() {
        return deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }
}
