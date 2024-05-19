package com.example.consumer.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserSignUpEnum {


    USER_HAS_SIGNUP(1,"用户信息已注册"),
    USER_NEVER_SIGNUP(2,"用户信息未注册");

    private final Integer value;
    private final String SignUpMsg;

}
