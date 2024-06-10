package com.example.consumer.controller;

import cn.hutool.core.util.StrUtil;
import com.example.consumer.pojo.dto.SubscribeModifyDTO;
import com.example.consumer.pojo.vo.DormitoryDetailListVO;
import com.example.consumer.pojo.vo.LoginTokenVO;
import com.example.consumer.pojo.vo.ModifySubscribeVO;
import com.example.consumer.pojo.vo.UserInformationVO;
import com.example.consumer.service.IUtilityBillsService;
import com.example.consumer.utils.JwtTool;
import com.example.consumer.utils.UserContext;
import com.example.consumer.utils.WebResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@RestController
@Slf4j
@RequestMapping("/utilityBill/service")
@CrossOrigin
public class UtilityBillController {
    @Resource
    private IUtilityBillsService utilityBillsService;
    @Resource
    public JwtTool jwtTool;

    /**
     * 针对已经注册的用户，传入用户邮箱，即可查询用户宿舍的水电费情况
     * 并发送邮件信息给用户
     *
     * @param recipient: 用户接收方的邮件
     * @return [java.lang.String]
     */

    @GetMapping("/getBill")
    public WebResponseUtil<Integer> getUtilityBill(@RequestParam(required = false, value = "email") String recipient) {
        return utilityBillsService.sendBill(recipient)?WebResponseUtil.Success(2001):WebResponseUtil.Failed(1001,"查无此人");
    }

    @PostMapping("/getUserInformation")
    public WebResponseUtil<UserInformationVO> getUserInformation(@RequestBody LoginTokenVO loginTokenVO) {
        String userUuid = UserContext.getUser();
        if (StrUtil.isBlank(userUuid)) {
            if (StrUtil.isBlank(loginTokenVO.getToken())) {
                return WebResponseUtil.error(10110, "用户信息为空");
            }
            userUuid = jwtTool.parseToken(loginTokenVO.getToken());
            if (StrUtil.isNotBlank(userUuid)) {
                UserContext.removeUser();
                UserContext.setUser(userUuid);
            }
        }

        Future<String> utilityBillASyncTack = utilityBillsService.asyncGetUtilityBill(userUuid);
        Future<List<DormitoryDetailListVO>> dormitoryUtilityBillDetailAsyncTask = utilityBillsService.getDormitoryUtilityBillDetailAsyncTask(UserContext.getUser());
        UserInformationVO userInformation = utilityBillsService.getUserInformation(userUuid);
        try {
            String utilityBill = utilityBillASyncTack.get();
            userInformation.setUtilityBill(StrUtil.isNotBlank(utilityBill)?utilityBill:"");
            List<DormitoryDetailListVO> utilityBillDetail = dormitoryUtilityBillDetailAsyncTask.get();
            userInformation.setDetailInfo(utilityBillDetail);
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage(),e);
            e.printStackTrace();
            if(StrUtil.isBlank(userInformation.getUtilityBill())){
                userInformation.setUtilityBill("");
            }
        }

        return WebResponseUtil.Success(userInformation);
    }

    @PostMapping("/subscribeModify")
    public WebResponseUtil<ModifySubscribeVO> subscribeModify(@RequestBody SubscribeModifyDTO subscribeModifyDTO) {
        ModifySubscribeVO modifySubscribeVO = new ModifySubscribeVO();
        try{
            utilityBillsService.modifySubscribe(subscribeModifyDTO);
            modifySubscribeVO.setSuccess(Boolean.TRUE);
        }catch (Exception exception){
            log.error(exception.getMessage(),exception);
            modifySubscribeVO.setSuccess(Boolean.FALSE);
            // 如果报错发送邮件提醒我自己要更新cookie了
            utilityBillsService.sendEmailForMentionCookieNeedToUpdate();
        }

        return WebResponseUtil.Success(modifySubscribeVO);
    }

    @GetMapping("/testMapping")
    public WebResponseUtil<Void> getTokenTest() {
        return WebResponseUtil.Success();
    }

}
