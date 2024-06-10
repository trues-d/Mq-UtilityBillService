package com.example.consumer.feign;


import com.example.consumer.config.feign.RongDaFeignConfiguration;
import com.example.consumer.pojo.dto.FeignUserUuidDTO;
import com.example.consumer.pojo.dto.RongDaDetailDTO;
import com.example.consumer.pojo.dto.RongDaDormitoryDetail;
import com.example.consumer.pojo.dto.UtilityBillDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "RongDaFeignClient",
        url = "${mailSendingService.rongDaClientHost}",
        configuration = RongDaFeignConfiguration.class
)
public interface RongDaFeignClient {

    @PostMapping("${mailSendingService.rongDaDormitoryDetail}")
    RongDaDormitoryDetail getDormitoryDetail(@RequestBody RongDaDetailDTO feignUserUuidDTO);


}
