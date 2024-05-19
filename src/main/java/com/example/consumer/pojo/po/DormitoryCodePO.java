package com.example.consumer.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.jeffreyning.mybatisplus.anno.MppMultiId;
import lombok.Data;

@Data
@TableName("dormitory_code")
public class DormitoryCodePO {

    @MppMultiId
    @TableField("code_id")
    private Long codeId;

    @MppMultiId
    @TableField("university_code_id")
    private String universityCodeId;
    @TableField(value = "name")
    private String name;
    @TableField(value = "dormitory_area_uuid")
    private String dormitoryAreaUuid;

    @TableField(value = "uuid")
    private String uuid;
}
