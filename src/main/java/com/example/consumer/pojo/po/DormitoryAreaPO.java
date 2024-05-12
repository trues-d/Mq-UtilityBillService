package com.example.consumer.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dormitory_area")
public class DormitoryAreaPO {

    @TableId
    private String uuid;

    @TableField("name")
    private String name;
    @TableField("university_code")
    private String universityCode;
}
