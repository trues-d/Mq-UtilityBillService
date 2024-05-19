package com.example.consumer.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpDTO {

    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "请输入有效的邮件地址")
    private String email;

    @NotBlank(message = "用户名不能为空")
    private String userName;
    @NotBlank(message = "请选择学校-校区")
    private String universityCodeId;
    @NotBlank(message = "请选择宿舍楼")
    private String dormitoryId;
    @NotBlank
    @Length(min = 4,max = 4,message = "请选择宿舍号")
    private String dormitoryRoomId;
}
