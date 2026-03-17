package com.example.poker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.poker.player")
public class PokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokerApplication.class, args);
    }
}

