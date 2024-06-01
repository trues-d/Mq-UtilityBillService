package com.example.consumer.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.time.Duration;

@Data
@Configuration
public class JwtProperties {
    @Value("${mailSendingService.jwt.location}")
    private Resource location;  //密钥库位置

    @Value("${mailSendingService.jwt.password}")
    private String password;  //密钥库的密码
    @Value("${mailSendingService.jwt.alias}")
    private String alias;  //作者

    public static final  Duration tokenTTL = Duration.ofMinutes(2L);  //传入时间 消失
}
