package com.example.poker.game;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class PokerTable {

    private final Map<String, PlayerSession> players = new ConcurrentHashMap<>();

    private final Map<Long, List<Card>> playerHoleCards = new HashMap<>();
    private List<Card> communityCards = new ArrayList<>();
    private long pot = 0L;
    private boolean handRunning = false;

    public void addPlayer(PlayerSession playerSession) {
        players.put(playerSession.getSession().getId(), playerSession);
    }

    public void removePlayer(String sessionId) {
        PlayerSession ps = players.remove(sessionId);
        if (ps != null) {
            playerHoleCards.remove(ps.getUserId());
        }
    }

    public Collection<PlayerSession> getAllPlayers() {
        return players.values();
    }

    public void startSimpleHand() {
        if (players.size() < 2) {
            throw new IllegalStateException("至少需要两名玩家");
        }
        Deck deck = new Deck();
        communityCards.clear();
        playerHoleCards.clear();
        pot = 0L;
        handRunning = true;

        // 每个玩家发两张手牌
        for (PlayerSession ps : players.values()) {
            List<Card> holes = new ArrayList<>();
            holes.add(deck.draw());
            holes.add(deck.draw());
            playerHoleCards.put(ps.getUserId(), holes);
        }

        // 发 5 张公共牌
        for (int i = 0; i < 5; i++) {
            communityCards.add(deck.draw());
        }
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public Map<Long, List<Card>> getPlayerHoleCards() {
        return playerHoleCards;
    }
}

