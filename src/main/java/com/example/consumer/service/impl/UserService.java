package com.example.consumer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.consumer.config.JwtProperties;
import com.example.consumer.convert.UtilityBillConvert;
import com.example.consumer.dao.UserDao;
import com.example.consumer.mapper.UserMapper;
import com.example.consumer.pojo.dto.UserLoginDTO;
import com.example.consumer.pojo.po.UserPO;
import com.example.consumer.service.IUserService;
import com.example.consumer.utils.JwtTool;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class UserService extends ServiceImpl<UserMapper, UserPO> implements IUserService {

    @Resource
    private UtilityBillConvert utilityBillConvert;

    @Resource
    private JwtTool jwtTool;

    @Resource
    private UserDao userDao;

    @Override
    public String userLoginJudge(UserLoginDTO userLoginDTO) {
        String userEmail = userLoginDTO.getEmail().trim();
        String userPassword = userLoginDTO.getPassword().trim();
        userLoginDTO.setEmail(userEmail);
        userLoginDTO.setPassword(userPassword);

        UserPO userPO = utilityBillConvert.UserLoginDTOToUserPO(userLoginDTO);
        UserPO ifHasUser = userDao.ifUserEmailAndPasswordCorrect(userPO);

        if(Objects.isNull(ifHasUser)){
            return "";
        }
        return jwtTool.createToken(ifHasUser.getUuid(), JwtProperties.tokenTTL);
    }
}