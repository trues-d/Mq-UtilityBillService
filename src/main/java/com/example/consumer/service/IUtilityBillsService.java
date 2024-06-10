package com.example.consumer.service;

import com.example.consumer.pojo.dto.SubscribeModifyDTO;
import com.example.consumer.pojo.dto.UtilityBillDTO;
import com.example.consumer.pojo.vo.DormitoryDetailListVO;
import com.example.consumer.pojo.vo.UserInformationVO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface IUtilityBillsService {
    String getUtilityBill(String userRecipient);
    String getUtilityBill(UtilityBillDTO utilityBillDTO);
    Boolean sendBill(String recipient);
    void queryAllUserSendMessage();

    Future<String> asyncGetUtilityBill(String email);

    UserInformationVO getUserInformation(String userId);

    void modifySubscribe(SubscribeModifyDTO subscribeModifyDTO );

    void sendEmailForMentionCookieNeedToUpdate();

    Future<List<DormitoryDetailListVO>> getDormitoryUtilityBillDetailAsyncTask(String userUuid);
    Map<String,String > getHeaders(String email);

}
