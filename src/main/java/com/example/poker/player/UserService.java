package com.example.poker.player;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Transactional
    public LoginResponse loginOrRegister(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        String trimmed = nickname.trim();
        UserEntity user = userMapper.selectOne(
                Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getNickname, trimmed)
        );
        if (user == null) {
            user = new UserEntity();
            user.setNickname(trimmed);
            user.setChipBalance(10000L);
            userMapper.insert(user);
        }

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setChipBalance(user.getChipBalance());
        response.setToken(UUID.randomUUID().toString());
        return response;
    }

    public static class LoginResponse {
        private Long userId;
        private String nickname;
        private Long chipBalance;
        private String token;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public Long getChipBalance() {
            return chipBalance;
        }

        public void setChipBalance(Long chipBalance) {
            this.chipBalance = chipBalance;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}

