package com.example.consumer.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class UserVerifyProperties {

    @Value("${mailSendingService.userVerifyUrl}")
    private String verifyUrl;

    @Value("${mailSendingService.sendRedirect}")
    private String sendRedirect;

    @Value("${mailSendingService.backend.ip}")
    private String backendIp;
    @Value("${mailSendingService.backend.port}")
    private String backendPort;

    @Value("${mailSendingService.frontend.ip}")
    private String frontendIp;
    @Value("${mailSendingService.frontend.port}")
    private String frontendPort;
}
