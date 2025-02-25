package com.example.consumer.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UtilityBillEnum {

    /**
     * UtilityBillContext 电费账单内容模板 需要添加查询的电费bill
     *
     */

    Context("亲爱的用户：您好！\n" +
            "\n" +
            "\t这是一封来自"+MailEnum.MailSendingUserName.getContent()+"的邮件。\n" +
            "\t后台检测任务发现：您房间的剩余电量剩余 %s度，如果电量不足请及时充电。\n"+
            "\t联系人："+MailEnum.MailSendingUserName.getContent()+" 联系电话："+MailEnum.MailSendingPhoneNumber.getContent()+"\n");


    private final String content;



}
