package com.example.consumer.pojo.dto;

import lombok.Data;

@Data
public class SubscribeModifyDTO {
    /**
     * userBillUtility 的对象
     */

    private String utilityBillUserJson;

    /**
     * user Uudid
     */

    private String userUuid;

    /**
     * 是否要订阅
     */
    private Boolean ifSubscribe;
}
