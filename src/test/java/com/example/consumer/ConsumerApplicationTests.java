package com.example.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.consumer.pojo.Mail;

import com.example.consumer.pojo.dto.UtilityBillUserDTO;
import com.example.consumer.service.impl.UtilityBillsService;
import com.example.consumer.utils.MailUtil;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.Resource;
import java.util.*;


@SpringBootTest
@Slf4j
//@RequiredArgsConstructor
class ConsumerApplicationTests {

    @Resource //依赖注入 与@Autowired一致 @Resource默认按byName自动注入
    private MailUtil mailUtil;

    @Autowired
    private UtilityBillsService utilityBillsService;
//    private static final String cookie = "shiroJID=341b57d5-d882-4c9b-acc7-443f81d0b6a4";

    //接收人
    private static final String recipient = "leon.bwchen@foxmail.com";

    @Autowired
    private JedisPool jedisPool;

    @Test
    void MailSending(String bills) {
        Mail mail = new Mail();
//        int code = (int) ((Math.random() * 9 + 1) * 100000);
        mail.setRecipient(recipient);
        mail.setSubject("张赞发来的测试邮件");
        mail.setContent("亲爱的用户：您好！\n" +
                "\n" +
                "\t这是一封来自ZZGEDA_张赞的邮件，请不用理会这封电子邮件。\n" +
                "\t但是：您房间电费剩余 " + bills + "度，如果电力不足请及时充电\n" +
                "\t咨询人：ZZGEDA_张赞 联系电话：19902900670\n"
        );
        mailUtil.sendSimpleMail(mail);
    }

    @Test
    void queryAllUserSendMessage() {
        utilityBillsService.queryAllUserSendMessage();
    }


    @Test
    void scheduleTask() {
        utilityBillsService.scheduleTask();
    }

    @Test
    void LongParse() {
        float l = Float.parseFloat("78.8");
        if (l < 50.0f) {
            System.out.println("iopdpdp");
        } else {
            System.out.println("llsosopp");
        }
    }

    @Test
    void putObjectIntoRedis() {
        UtilityBillUserDTO utilityBillUserDTO = new UtilityBillUserDTO();
        utilityBillUserDTO.setUserName("ZZGEDA");
        utilityBillUserDTO.setDormitoryId(189200);
        String s = JSONArray.toJSONString(Collections.singletonList(utilityBillUserDTO));
        Jedis resource = jedisPool.getResource();
        String set = resource.set("user:2", s);
        String s1 = resource.get("user:2");
        List<UtilityBillUserDTO> utilityBillUserDTOS = JSON.parseArray(s1, UtilityBillUserDTO.class);
        if (Objects.isNull(utilityBillUserDTOS)) {
            System.out.println("huihuihiuhui");
        } else {
            for (UtilityBillUserDTO ui : utilityBillUserDTOS) {
                System.out.println(ui);
            }
        }

        HashMap<byte[], byte[]> hashMap = new HashMap<>();
        byte[] keyBytes = "userDTO:4".getBytes();
        Integer i = new Integer(18);
        // 整性转字节数组
        byte[] src = new byte[4];
        src[3] = (byte) ((i >> 24) & 0xFF);
        src[2] = (byte) ((i >> 16) & 0xFF);
        src[1] = (byte) ((i >> 8) & 0xFF);
        src[0] = (byte) (i & 0xFF);


        UtilityBillUserDTO utilityBillUserDTO1 = new UtilityBillUserDTO();

        hashMap.put(keyBytes, JSONObject.toJSONBytes(utilityBillUserDTO));
        hashMap.put("name".getBytes(), "uiooo".getBytes());
        hashMap.put("age".getBytes(), src);  //但是存进去还是一个string 的结构

        byte[] bytes = new byte[1024];
        resource.hset("user:4".getBytes(), hashMap);  //redis 将byte对象转换成一个字符串  原因是我上面用了JSONObject.toJSONBytes
        String hgetRes = resource.hget("user:4", "userDTO:4");
        UtilityBillUserDTO utilityBillUserDTO2 = JSONObject.parseObject(hgetRes, UtilityBillUserDTO.class);
        System.out.println(utilityBillUserDTO2.toString());


        resource.close();


    }

    @Test
    void consumerMessageFromRedis() {

        // lambda函数 new Runnable
        Thread thread = new Thread(() -> {
            Jedis resource = jedisPool.getResource();
            resource.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    System.out.printf("channel:%s , message:%s %n",channel,message);
                }
            }, "channel-1");
        });
        thread.start();


        System.out.println("hello 异步执行");
        try {
            Thread.sleep(1000*20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}



