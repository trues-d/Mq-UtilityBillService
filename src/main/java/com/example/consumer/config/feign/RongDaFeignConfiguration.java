package com.example.consumer.config.feign;


import com.alibaba.fastjson.JSONObject;
import com.example.consumer.pojo.dto.RongDaDetailDTO;
import com.example.consumer.service.IUtilityBillsService;
import feign.Request;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
public class RongDaFeignConfiguration {
    // 解决循环依赖 指定bean初始的顺序
    // 使用接口 而不是实现类
    // 如果直接注入实现类 那么注入的是一个代理对象 如果是接口的话就注入一个新的实例 这是一个新的线程
    @Lazy
    @Resource
    private IUtilityBillsService utilityBillsService;

    /**
     * 设定拦截器 拦截fien 添加请求头 包括token和content-type
     */

    @Bean
    public RequestInterceptor utilityBillInterceptor() {
        return requestTemplate -> {
            byte[] body = requestTemplate.body();
            RongDaDetailDTO userUuidDTO = JSONObject.parseObject(body, RongDaDetailDTO.class);
            Map<String, String> headers = utilityBillsService.getHeaders(userUuidDTO.getEmail());
            headers.forEach((key, value) -> {
                if ("Content-Type".equals(key)) {
                    requestTemplate.header("Content-Type", "application/json;charset=utf-8");
                } else {
                    requestTemplate.header(key, value);
                }
            });
        };
    }

    @Bean
    Request.Options yuTongFeignOptions() {
        return new Request.Options(10*60 * 1000, 10*60 * 1000);
    }

}
