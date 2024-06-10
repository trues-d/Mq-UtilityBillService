package com.example.consumer.pojo.dto;

import lombok.Data;

@Data
public class RongDaDormitoryDetailItemDTO {
    private String id;
    private String orderNo;
    private double payMoney;
    private double totalMoney;
    private String payType;
    private String payNo;
    private String createTime;
    private String payStatusStr;
    private String subType;
    private String prodName;
    private String payTime;
    private String remark;
    private String logo;
    private double feeMoney;
    private String week;
    private String dayDate;
    private String month;
    private int isShowRefund;
    private int outTradeStatus;
    private double orderRealMoney;
    private CenterOrderStatisticsVO centerOrderStatisticsVO;

    @Data
    public static class CenterOrderStatisticsVO {
        private String months;
        private String totalTranMoney;
        private String totalRealMoney;
        private int totalCount;
    }
}
