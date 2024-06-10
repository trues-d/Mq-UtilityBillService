package com.example.consumer.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class RongDaDormitoryDetail {
    private Integer statusCode;
    private Boolean success;
    private List<RongDaDormitoryDetailItemDTO> rows;
    private String total;
 }
