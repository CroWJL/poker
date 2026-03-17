package com.example.poker.player;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@TableName("users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("nickname")
    private String nickname;

    @TableField("chip_balance")
    private Long chipBalance = 10000L;

    @TableField("password")
    private String password;
}

