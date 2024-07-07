package com.example.consumer.controller;

import cn.hutool.core.util.StrUtil;
import com.example.consumer.exception.BizException;
import com.example.consumer.pojo.dto.UserLoginDTO;
import com.example.consumer.pojo.vo.LoginTokenVO;
import com.example.consumer.service.IUserService;
import com.example.consumer.utils.WebResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/utilityBill/login")
@Slf4j
@CrossOrigin
public class LoginController {
    @Resource
    private IUserService userService;


    /**
     * 账号密码登录
     */
    @PostMapping("/account")
    public WebResponseUtil<LoginTokenVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        String token = userService.userLoginJudge(userLoginDTO);
        LoginTokenVO loginTokenVO = new LoginTokenVO();
        loginTokenVO.setToken("");
        if (StrUtil.isNotBlank(token)) {
            loginTokenVO.setToken(token);
                return WebResponseUtil.Success(200,loginTokenVO,"验证成功");
        } else {
            return WebResponseUtil.Success(202, loginTokenVO, "账号密码错误");
        }
    }

    @GetMapping("/testTtt")
    public WebResponseUtil<Void> test(){
        System.out.println("hello");
        return WebResponseUtil.Success();
    }


}
