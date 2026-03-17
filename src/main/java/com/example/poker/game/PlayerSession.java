package com.example.poker.game;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class PlayerSession {
    private Long userId;
    private String nickname;
    private long chipBalance;
    private WebSocketSession session;
}

