package com.example.consumer.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class UserPO {
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField(value = "user_name")
    private String userName;

    @TableField(value = "uuid")
    private String uuid;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("is_delete")
    private Boolean isDelete;

}
