package com.example.consumer.dao;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.consumer.mapper.UserMapper;
import com.example.consumer.mapper.UtilityBillUserMapper;
import com.example.consumer.pojo.po.UserPO;
import com.example.consumer.pojo.po.UtilityBillUserDTOPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class UserDao {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UtilityBillUserMapper utilityBillUserMapper;

    public UserPO ifUserEmailAndPasswordCorrect(UserPO userPO){
        LambdaQueryWrapper<UserPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserPO::getIsDelete,Boolean.FALSE)
                .eq(UserPO::getEmail,userPO.getEmail())
                .eq(UserPO::getPassword,userPO.getPassword());
        return userMapper.selectOne(wrapper);
    }

    public UserPO getUserByUuid(String userUuid){
        LambdaQueryWrapper<UserPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserPO::getIsDelete,Boolean.FALSE)
                .eq(UserPO::getUuid,userUuid);
        return userMapper.selectOne(wrapper);
    }
    public UserPO getByEmail(String email){
        LambdaQueryWrapper<UserPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserPO::getIsDelete,Boolean.FALSE)
                .eq(UserPO::getEmail,email);
        return userMapper.selectOne(wrapper);
    }

    public Integer updateUtilityBillUserSubscribeStatus(UtilityBillUserDTOPO utilityBillUserPO){
        LambdaUpdateWrapper<UtilityBillUserDTOPO> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(UtilityBillUserDTOPO::getMail,utilityBillUserPO.getMail())
                .eq(UtilityBillUserDTOPO::getUserName,utilityBillUserPO.getUserName())
                .eq(UtilityBillUserDTOPO::getDormitoryRoomId,utilityBillUserPO.getDormitoryRoomId());
        UtilityBillUserDTOPO utilityBillUserDTOPO = new UtilityBillUserDTOPO();
        utilityBillUserDTOPO.setIfDeleted(utilityBillUserPO.getIfDeleted());
        return utilityBillUserMapper.update(utilityBillUserDTOPO,wrapper);
    }
}
