package com.example.consumer.pojo.dto;

import lombok.Data;

@Data
public class FloorDTO {
    /**
     * 楼层 01
     */
    private String floor;

    /**
     * 房间号 14
     */
    private String room;

    /**
     * 宿舍方便的最终编码 0114
     */
    private String floorRoom;

    private String floorRoomShow;

    private String children;

}
