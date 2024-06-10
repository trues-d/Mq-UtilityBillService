package com.example.consumer.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginDTO {
    @NotBlank(message = "用户邮箱不允许为空")
    private String email;
    @NotBlank(message = "用户密码不允许为空")
    private String password;
}
