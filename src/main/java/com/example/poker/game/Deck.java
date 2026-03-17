package com.example.poker.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card> cards = new ArrayList<>();
    private int index = 0;

    public Deck() {
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};
        String[] suits = {"♠", "♥", "♦", "♣"};
        for (String r : ranks) {
            for (String s : suits) {
                cards.add(new Card(r, s));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
        index = 0;
    }

    public Card draw() {
        if (index >= cards.size()) {
            throw new IllegalStateException("No more cards");
        }
        return cards.get(index++);
    }
}

