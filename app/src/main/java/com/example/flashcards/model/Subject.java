package com.example.flashcards.model;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    private String title;
    private List<FlashCard> cards = new ArrayList<>();

    public Subject(String title) { this.title = title; }
    public String getTitle() { return title; }
    public List<FlashCard> getCards() { return cards; }
    public void addCard(FlashCard card) { cards.add(card); }
}