package com.example.poker.websocket;

import com.example.poker.game.Card;
import com.example.poker.game.HandEvaluator;
import com.example.poker.game.PlayerSession;
import com.example.poker.game.PokerTable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Component
public class PokerWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PokerWebSocketHandler.class);

    private final PokerTable pokerTable;
    private final ObjectMapper objectMapper;

    public PokerWebSocketHandler(PokerTable pokerTable, ObjectMapper objectMapper) {
        this.pokerTable = pokerTable;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        PokerMessage msg = objectMapper.readValue(message.getPayload(), PokerMessage.class);
        switch (msg.getType()) {
            case "JOIN_TABLE" -> handleJoinTable(session, msg.getPayload());
            case "START_HAND" -> handleStartHand(session);
            default -> log.info("Received message type {} from {}", msg.getType(), session.getId());
        }
    }

    private void handleJoinTable(WebSocketSession session, Object payload) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) payload;
        Long userId = ((Number) map.get("userId")).longValue();
        String nickname = (String) map.get("nickname");
        Number chipBalanceNumber = (Number) map.getOrDefault("chipBalance", 0);

        PlayerSession ps = new PlayerSession();
        ps.setUserId(userId);
        ps.setNickname(nickname);
        ps.setChipBalance(chipBalanceNumber.longValue());
        ps.setSession(session);
        pokerTable.addPlayer(ps);

        broadcastTableState();
    }

    private void handleStartHand(WebSocketSession session) throws IOException {
        if (!pokerTable.getPlayers().containsKey(session.getId())) {
            log.warn("Session {} tried to start hand but is not at table", session.getId());
            return;
        }
        try {
            pokerTable.startSimpleHand();
        } catch (IllegalStateException e) {
            sendError(session, e.getMessage());
            return;
        }
        broadcastTableState();
        broadcastShowdown();
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        PokerMessage error = new PokerMessage();
        error.setType("ERROR");
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        error.setPayload(payload);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
    }

    private void broadcastTableState() throws IOException {
        List<Map<String, Object>> players = pokerTable.getAllPlayers().stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", p.getUserId());
            m.put("nickname", p.getNickname());
            m.put("chipBalance", p.getChipBalance());
            return m;
        }).toList();
        List<String> community = pokerTable.getCommunityCards().stream().map(Card::toString).toList();

        for (PlayerSession target : pokerTable.getAllPlayers()) {
            if (!target.getSession().isOpen()) {
                continue;
            }
            Map<String, Object> tableState = new HashMap<>();
            tableState.put("players", players);
            tableState.put("communityCards", community);
            tableState.put("pot", pokerTable.getPot());

            List<Card> selfCards = pokerTable.getPlayerHoleCards()
                    .getOrDefault(target.getUserId(), List.of());
            tableState.put("selfHoleCards", selfCards.stream().map(Card::toString).toList());

            PokerMessage out = new PokerMessage();
            out.setType("TABLE_STATE");
            out.setPayload(tableState);
            String json = objectMapper.writeValueAsString(out);
            target.getSession().sendMessage(new TextMessage(json));
        }
    }

    private void broadcastShowdown() throws IOException {
        List<Card> community = pokerTable.getCommunityCards();
        Map<Long, List<Card>> holes = pokerTable.getPlayerHoleCards();
        List<Map<String, Object>> results = new ArrayList<>();

        for (PlayerSession ps : pokerTable.getAllPlayers()) {
            List<Card> hc = holes.getOrDefault(ps.getUserId(), List.of());
            String rank = HandEvaluator.evaluate(hc, community);
            Map<String, Object> r = new HashMap<>();
            r.put("userId", ps.getUserId());
            r.put("nickname", ps.getNickname());
            r.put("handRank", rank);
            r.put("holeCards", hc.stream().map(Card::toString).toList());
            results.add(r);
        }

        PokerMessage msg = new PokerMessage();
        msg.setType("SHOWDOWN");
        Map<String, Object> payload = new HashMap<>();
        payload.put("communityCards", community.stream().map(Card::toString).toList());
        payload.put("results", results);
        msg.setPayload(payload);
        String json = objectMapper.writeValueAsString(msg);

        for (PlayerSession p : pokerTable.getAllPlayers()) {
            if (p.getSession().isOpen()) {
                p.getSession().sendMessage(new TextMessage(json));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket closed: {}", session.getId());
        pokerTable.removePlayer(session.getId());
        broadcastTableState();
    }
}

