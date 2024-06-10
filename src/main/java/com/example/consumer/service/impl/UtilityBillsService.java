package com.example.consumer.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.example.consumer.dao.UserDao;
import com.example.consumer.feign.RongDaFeignClient;
import com.example.consumer.mapper.UtilityBillMapper;
import com.example.consumer.mapper.UtilityBillUserMapper;
import com.example.consumer.pojo.dto.*;
import com.example.consumer.pojo.entity.PostRequestEnum;
import com.example.consumer.pojo.po.*;
import com.example.consumer.pojo.vo.DormitoryDetailListVO;
import com.example.consumer.pojo.vo.UserInformationVO;
import com.example.consumer.service.IUtilityBillsService;
import com.example.consumer.utils.HttpUtil;
import com.example.consumer.utils.UserContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UtilityBillsService implements IUtilityBillsService {
    //    @Resource //依赖注入 与@Autowired一致 @Resource默认按byName自动注入
    @Resource
    private  HttpUtil httpUtil;
    @Resource
    private  UtilityBillUserMapper utilityBillUserMapper;
    @Resource
    private  UtilityBillMapper utilityBillMapper;
    @Resource
    private  UtilityBillUserService utilityBillUserService;
    @Resource
    private  DormitoryCodeService dormitoryCodeService;
    @Resource
    private  UniversityCodeService universityCodeService;
    @Resource
    private  DormitoryAreaService dormitoryAreaService;
    @Resource
    private  RabbitTemplate rabbitTemplate;
    private static final String utilityBillUrl = "https://application.xiaofubao.com/app/electric/queryISIMSRoomSurplus";
    @Resource
    private  JedisPool jedisPool;

    private final Float utilityBillThreshold = 25.0f;

    @Resource
    private UserDao userDao;
    @Value("mailSendingService.sudoEmail")
    private  String sudoEmail;

    @Resource
    private RongDaFeignClient rongDaFeignClient;


    /**
     * 向数据库查询发送邮箱的人的宿舍电费信息，然后将查询到的电费信息通过ZZGEDA的QQMail 发送给userRecipient这个邮箱
     *
     * @param userRecipient: 发送邮箱的邮箱号码
     * @return [java.lang.String]
     */
    @Override
    public String getUtilityBill(String userRecipient) {
        String bill;
        UtilityBillDTO utilityBillDTO;
        // 如果不用https会发生重定型 然后需要手动重定向发送请求

        UtilityBillUserDTO utilityBillUserDTOExceptMail = utilityBillUserMapper.getUtilityBillUserExceptMail(userRecipient);
        // 为空则返回
        if (Objects.isNull(utilityBillUserDTOExceptMail)) {
            return "";
        } else {
            utilityBillDTO = new UtilityBillDTO();
        }

        utilityBillDTO.setAreaId(utilityBillUserDTOExceptMail.getUniversityCodeId());
        utilityBillDTO.setBuildingCode(utilityBillUserDTOExceptMail.getDormitoryId());
        utilityBillDTO.setFloorCode(utilityBillUserDTOExceptMail.getDormitoryRoomId() / 100);
        utilityBillDTO.setRoomCode(utilityBillUserDTOExceptMail.getDormitoryRoomId());
        utilityBillDTO.setYmId("");
        utilityBillDTO.setPlatform("");

        try {
            // 基于反射将utilityBillDTO处理成Map对象
            JsonNode jsonNode = new ObjectMapper().
                    readTree(httpUtil.doPost(utilityBillUrl, this.getFormBody(utilityBillDTO), this.getHeaders(userRecipient))).
                    get("data").get("soc");
            bill = jsonNode.asText();
            log.info(String.format("用户：%s的宿舍电量还剩%s", userRecipient, bill));
        } catch (Exception e) {
            log.error(e.toString());
            return "";
        }
        return bill;
    }


    @Override
    public String getUtilityBill(UtilityBillDTO utilityBillDTO) {
        return null;
    }

    /**
     * getUtilityBill获得用户宿舍的剩余水电之后 将查询到的水电费bill传输给mq
     * 通过mq实现异步的邮件发送
     *
     * @param recipient: 接收方的邮箱地址
     */

    @Override
    public Boolean sendBill(String recipient) {
        String dormitoryBill = this.getUtilityBill(recipient);
        if (!dormitoryBill.isEmpty()) {
            // lambda 传入一个自定义方法 这种使用就可以算作是回调函数了
            rabbitTemplate.convertAndSend("mail.direct", "mail", new MailSendingMqDTO(recipient, dormitoryBill), (message -> {
                message.getMessageProperties().setMessageId(recipient);
                return message;
            }));
            log.info("====>  mq异步消息发送成功");
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void sendEmailForMentionCookieNeedToUpdate(){
        rabbitTemplate.convertAndSend("mail.direct", "mail", new MailSendingMqDTO(sudoEmail, "赶紧更新邮件服务的cookie"), (message -> {
            message.getMessageProperties().setMessageId(sudoEmail);
            return message;
        }));
    }

    @Async
    public Future<String> asyncGetUtilityBill(String userUuid){
        log.info("异步查询用户宿舍水电费task ====> 开始");
        UserPO userByUuid = userDao.getUserByUuid(userUuid);
        String email = userByUuid.getEmail();
        String utilityBill = this.getUtilityBill(email);
        log.info("异步查询用户宿舍水电费task <==== 结束");
        return  new AsyncResult<>(utilityBill);
    }

    // 定时任务 每天下午14点执行
    @Override
    @Scheduled(cron = "0 43 13 * * ?")
    public void queryAllUserSendMessage() {
        // 查询所有用户 和 查询所有用户的unique宿舍号 减少请求发送
        String bill;

        List<UtilityBillUserDTOLocationDTO> allUtilityBillUser = utilityBillUserMapper.getAllUtilityBillUser();
        List<UtilityBillUserDTOLocationDTO> dormitoryRoomIdsGroupBy = utilityBillUserMapper.getDormitoryRoomIdsGroupBy();
        Map<String, String> headerSchedule = getHeaderSchedule();
        StringBuilder dormitoryLocationKey = new StringBuilder();
        UtilityBillDTO utilityBillDTO = new UtilityBillDTO();
        utilityBillDTO.setYmId("");
        utilityBillDTO.setPlatform("");

        // 从线程池中申请资源  TODO:其实没有必要使用redis 直接使用Map即可 但考虑对查询数据进行持久化 使用redis
        // Jedis中有AutoCloseable的实现 try可以使用自动的资源管理 而不需要再写finally关闭资源
        // CloseableHttpClient==>Closeable==>AutoCloseable
        try (Jedis redisResource = jedisPool.getResource()) {
            // 循环获得宿舍号的剩余电费 并将学校号_宿舍号为key 将电费bill为value 存入redis
            for (UtilityBillUserDTOLocationDTO dormitoryItem : dormitoryRoomIdsGroupBy) {
                log.info(dormitoryItem.toString());
                // 转换成DTO对象
                utilityBillDTO.setAreaId(dormitoryItem.getUniversityCodeId());
                utilityBillDTO.setBuildingCode(dormitoryItem.getDormitoryId());
                utilityBillDTO.setFloorCode(dormitoryItem.getDormitoryRoomId() / 100);
                utilityBillDTO.setRoomCode(dormitoryItem.getDormitoryRoomId());
                // redis设置key
                dormitoryLocationKey.append(dormitoryItem.getUniversityCodeId()).
                        append("_").
                        append(dormitoryItem.getDormitoryRoomId());
                // 修改header
                headerSchedule.put(PostRequestEnum.Cookie.getValue(), dormitoryItem.getCookie());

                try {
                    // 基于反射将utilityBillDTO处理成Map对象 并发送http请求
                    JsonNode jsonNode = new ObjectMapper().
                            readTree(httpUtil.doPost(utilityBillUrl, getFormBody(utilityBillDTO), headerSchedule)).
                            get("data").get("soc");
                    bill = jsonNode.asText();
                    log.info(String.format("%s_%s 剩余电量:%s", dormitoryItem.getUniversityCodeId(), dormitoryItem.getDormitoryRoomId(), bill));
                    // redis插入数据  TODO:后面改成倒计时的 或者最后删除掉 但也可以不用做处理 只有用户注销的时候 才删除指定的Key
                    redisResource.set(dormitoryLocationKey.toString(), bill);
                } catch (Exception e) {
                    log.error(e.toString());
                } finally {
                    dormitoryLocationKey.setLength(0);
                }
            }

            log.info("https请求发送成功 以下是发送qqMail");
            // 遍历学生信息 向redis中查询上面的key (学校号_宿舍号) 获得bill数据 异步发送qq邮箱
            for (UtilityBillUserDTOLocationDTO userItem : allUtilityBillUser) {
                log.info("=====> 待发送邮件信息");
                log.info(userItem.toString());
                dormitoryLocationKey.append(userItem.getUniversityCodeId()).
                        append("_").
                        append(userItem.getDormitoryRoomId());
                // redis读取数据
                bill = redisResource.get(dormitoryLocationKey.toString());
                if (!bill.isEmpty()) {
                    //如果小于水电费阈值：50就发送短信，否则不发
                    if (Float.parseFloat(bill) < this.utilityBillThreshold) {
                        // lambda 传入一个自定义方法 这种使用就可以算作是回调函数了
                        rabbitTemplate.convertAndSend("mail.direct", "mail", new MailSendingMqDTO(userItem.getMail(), bill), (message -> {
                            message.getMessageProperties().setMessageId(userItem.getMail());
                            return message;
                        }));
                        log.info("<===== Mq异步消息发送成功");
                    } else {
                        log.info(String.format("<===== 用户%s无须发送", userItem.getUserName()));
                    }
                }
                // 重置 StringBuilder
                dormitoryLocationKey.setLength(0);
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        // 关闭线程资源

    }

    //    @Scheduled(cron = "*/5 * * * * *")
    public void scheduleTask() {
        System.out.println("test test test test test test test test ");
        System.out.println(Thread.currentThread().getName());
    }


    public Map<String, Object> getFormBody(UtilityBillDTO utilityBillDTO) throws RuntimeException {
        Map<String, Object> formBody = new HashMap<>();

        // 设置请求体
        // getFiled只能获取public公共属性 对于私有属性的获取只能通过getDeclaredFields
        for (Field item : utilityBillDTO.getClass().getDeclaredFields()) {
            try {
                // 属性配置允许访问私有属性
                item.setAccessible(true);
                formBody.put(item.getName(), item.get(utilityBillDTO));
                item.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return formBody;
    }

    @Override
    public Map<String, String> getHeaders(String userRecipient) throws RuntimeException {
        Map<String, String> headers = new HashMap<>();
        // 设置请求头
        headers.put(PostRequestEnum.Content_Type.getValue(), PostRequestEnum.ContentTypeXWWWForm.getValue());
        headers.put(PostRequestEnum.Cookie.getValue(), utilityBillMapper.getUtilityBillCookieByUniversityCode(userRecipient));
        return headers;
    }

    public Map<String, String> getHeaderSchedule() throws RuntimeException {
        Map<String, String> headers = new HashMap<>();
        // 设置请求头
        headers.put(PostRequestEnum.Content_Type.getValue(), PostRequestEnum.ContentTypeXWWWForm.getValue());
        headers.put(PostRequestEnum.Cookie.getValue(), "");
        return headers;
    }


    @Override
    public UserInformationVO getUserInformation(String userUuid) {

        UserPO userByUuid = userDao.getUserByUuid(userUuid);
        String email = userByUuid.getEmail();
        String userName = userByUuid.getUserName();
        UtilityBillUserDTOPO userDorInformation = utilityBillUserService.getById(email);


        String universityCodeId = userDorInformation.getUniversityCodeId();
        Integer dormitoryId = userDorInformation.getDormitoryId();
        Integer dormitoryRoomId = userDorInformation.getDormitoryRoomId();
        dormitoryRoomId = dormitoryRoomId % (dormitoryId * 1000);

        // 校区拼接出 浙江工业大学-屏峰
        UniversityCodePO universityAndArea = universityCodeService.getById(universityCodeId);
        String result_university = universityAndArea.getUniversityName().concat(StrUtil.isNotBlank(universityAndArea.getUniversityRegion()) ? '-' + universityAndArea.getUniversityRegion():"");

        // 正则化匹配出 10号楼
        DormitoryCodePO dormitoryCodePOById = dormitoryCodeService.getById(String.valueOf(dormitoryId));
        String dormitoryBuildingName = dormitoryCodePOById.getName();
        String dormitoryAreaUuid = dormitoryCodePOById.getDormitoryAreaUuid();


        int buildingIndex = dormitoryBuildingName.indexOf("号");
        buildingIndex -= 2;
        String substring = dormitoryBuildingName.substring(buildingIndex);
        String buildingName = substring.startsWith("0") ? substring.substring(1):substring;


        // 宿舍区名字 家和东苑
        DormitoryAreaPO dormitoryAreaPO = dormitoryAreaService.getById(dormitoryAreaUuid);
        String dormitoryAreaName = dormitoryAreaPO.getName();
        dormitoryAreaName = dormitoryAreaName.concat("-" + buildingName);

        UserInformationVO userInformationVO = new UserInformationVO();
        userInformationVO.setUserName(userName);
        userInformationVO.setUniversityName(result_university);
        userInformationVO.setDormitoryBuildingName(dormitoryAreaName);
        userInformationVO.setDormitoryRoomName(String.valueOf(dormitoryRoomId));
        userInformationVO.setUtilityBillUserPOJSONStr(JSON.toJSONString(userDorInformation));
        userInformationVO.setIsSubscribe(userDorInformation.getIfDeleted()==0);
        userInformationVO.setUserUuid(UserContext.getUser());
        userInformationVO.setEmail(email);

        log.info("用户信息查询完毕");
        return userInformationVO;
    }


    @Override
    public void modifySubscribe(SubscribeModifyDTO subscribeModifyDTO) {
        UtilityBillUserDTOPO utilityBillUserPO = JSON.parseObject(subscribeModifyDTO.getUtilityBillUserJson(), UtilityBillUserDTOPO.class);
        Boolean ifWanToSubscribe = subscribeModifyDTO.getIfSubscribe();
        utilityBillUserPO.setIfDeleted(Boolean.TRUE.equals(ifWanToSubscribe) ? 0:1);
        userDao.updateUtilityBillUserSubscribeStatus(utilityBillUserPO);
    }


    @Async
    @Override
    public Future<List<DormitoryDetailListVO>> getDormitoryUtilityBillDetailAsyncTask(String userUuid) {
        RongDaDormitoryDetail dormitoryDetail = rongDaFeignClient.getDormitoryDetail(new FeignUserUuidDTO(userUuid));
        List<DormitoryDetailListVO> collect = dormitoryDetail.getRows().stream().map(item -> {
            DormitoryDetailListVO dormitoryDetailListVO = new DormitoryDetailListVO();
            dormitoryDetailListVO.setWeek(item.getWeek());
            dormitoryDetailListVO.setPayTime(item.getPayTime());
            dormitoryDetailListVO.setPayMoney(item.getPayMoney());
            return dormitoryDetailListVO;
        }).collect(Collectors.toList());

        return new  AsyncResult<>(collect);
    }
}
