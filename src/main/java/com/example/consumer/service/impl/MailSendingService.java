package com.example.consumer.service.impl;

import com.example.consumer.pojo.dto.MailDTO;
import com.example.consumer.pojo.entity.MailEnum;
import com.example.consumer.pojo.entity.UtilityBillEnum;
import com.example.consumer.service.IMailSendingService;
import com.example.consumer.utils.MailContextUtil;
import com.example.consumer.utils.MailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailSendingService implements IMailSendingService {

    private final MailUtil mailUtil;
    private final MailContextUtil mailContextUtil;

    @Override
    public void sendSimpleMailFormQQMail(String recipient ,String bill){
        MailDTO mailDTO = new MailDTO();
        // 接收者邮箱
        mailDTO.setRecipient(recipient);
        mailDTO.setSubject(MailEnum.MailSubjectTest.getContent());
        mailDTO.setContent(mailContextUtil.getFullMailContextMessage(UtilityBillEnum.class,bill));
        mailUtil.sendSimpleMail(mailDTO);
    }

    @Override
    public void sendHtmlMailFormQQMail(String recipient, String userName, String uuid) {
        MailDTO mailDTO = new MailDTO();
        // 接收者邮箱
        mailDTO.setRecipient(recipient);
        mailDTO.setSubject(MailEnum.MailSignUpSubject.getContent());
        mailDTO.setUserName(userName);
        mailDTO.setUuid(uuid);
        mailUtil.sendHtmlMail(mailDTO);
    }
}
