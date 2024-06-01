package com.example.consumer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.consumer.mapper.UserMapper;
import com.example.consumer.pojo.po.UserPO;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, UserPO> {

}