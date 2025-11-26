package com.example.cleverty.model;

import java.util.ArrayList;
import java.util.List;

public class CardDeck {
    private static CardDeck INSTANCE;
    private final List<FlashCard> cards = new ArrayList<>();

    private CardDeck() {
        // Pre-load three sample cards
        cards.add(new FlashCard("What is the capital of Spain?", "Madrid"));
        cards.add(new FlashCard("2³ = ?", "8"));
        cards.add(new FlashCard("Who wrote Romeo & Juliet?", "William Shakespeare"));
    }
    public static CardDeck getInstance() {
        if (INSTANCE == null) INSTANCE = new CardDeck();
        return INSTANCE;
    }
    public List<FlashCard> getCards() { return cards; }
    public void addCard(FlashCard card) { cards.add(card); }
}