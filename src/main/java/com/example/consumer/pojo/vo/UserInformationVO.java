package com.example.consumer.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserInformationVO {
    /**
     * 用户名
     */
    private String userName ;

    private String universityName ;
    /**
     * 宿舍楼
     */
    private String dormitoryBuildingName;
    /**
     * 房间号
     */
    private String dormitoryRoomName;

    /**
     * userBillUtility 的dto  用于后续直接查找
     */
    private String utilityBillUserPOJSONStr;

    /**
     * 是否订阅
     */

    private Boolean isSubscribe;

    /**
     * 用户的uuid
     */

    private String userUuid;

    /**
     * 当前用户剩余水电费
     */
    private String utilityBill;

    private List<DormitoryDetailListVO> detailInfo;

    private String email;
}
