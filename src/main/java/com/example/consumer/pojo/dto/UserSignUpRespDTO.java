package com.example.consumer.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpRespDTO {

    /**
     * 返回结果信息
     */
    private String signUpMsg ;

    /**
     * 校验结果 1已注册 2未注册
     */

    private Integer verifyCode;

    /**
     * 通知信息
     */

    private String informMsg;

    /**
     * 注册用户的uuid
     */

    private String userUuid;


}
